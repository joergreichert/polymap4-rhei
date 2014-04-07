/* 
 * polymap.org
 * Copyright 2010-2012, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.internal.form;

import java.util.HashMap;
import java.util.Map;

import org.opengis.feature.Property;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NullValidator;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.internal.DefaultFormFieldDecorator;
import org.polymap.rhei.internal.DefaultFormFieldLabeler;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class AbstractFormEditorPageContainer
        implements IFormEditorPageSite {
    
    private Object                          editor;
    
    protected IFormEditorPage               page;
    
    private Map<String,FormFieldComposite>  fields = new HashMap( 64 );
    
    private Map<String,Object>              values = new HashMap( 64 );

    private volatile boolean                blockEvents;
    
    private int                             labelWidth = 100;

    
    public AbstractFormEditorPageContainer( Object editor, IFormEditorPage page, String id, String title ) {
        this.editor = editor;
        this.page = page;
    }
    
    
    public synchronized void dispose() {
        if (page != null && page instanceof IFormEditorPage2) {
            ((IFormEditorPage2)page).dispose();
        }
        for (FormFieldComposite field : fields.values()) {
            field.dispose();
        }
        fields.clear();
    }

    
    public int getLabelWidth() {
        return labelWidth;
    }
    
    public void setLabelWidth( int labelWidth ) {
        this.labelWidth = labelWidth;
    }


    public void addFieldListener( IFormFieldListener l ) {
        EventManager.instance().subscribe( l, new EventFilter<FormFieldEvent>() {
            public boolean apply( FormFieldEvent ev ) {
                return !blockEvents && ev.getEditor() == editor;
            }
        });
    }
    

    public void removeFieldListener( IFormFieldListener l ) {
        EventManager.instance().unsubscribe( l );
    }

    
    /*
     * Called from page provider client code.
     */
    public void fireEvent( Object source, String fieldName, int eventCode, Object newValue ) {
        if (newValue == null) {
            values.remove( fieldName );
        } else {
            values.put( fieldName, newValue );
        }
        
        if (!blockEvents) {
            // syncPublish() helps to avoid to much UICallbacks browser which slows
            // down form performance
            FormFieldEvent ev = new FormFieldEvent( editor, source, 
                    fieldName, fields.get( fieldName ).getField(), eventCode, null, newValue );
            EventManager.instance().syncPublish( ev );

//            FormFieldEvent ev = new FormFieldEvent( editor, source, fieldName, null, eventCode, null, newValue );
//            EventManager.instance().publish( ev );
        }
    }
    

    @Override
    public boolean isDirty() {
        if (page instanceof IFormEditorPage2) {
            if (((IFormEditorPage2)page).isDirty()) {
                return true;
            }
        }
        for (FormFieldComposite field : fields.values()) {
            if (field.isDirty()) {
                return true;
            }
        }
        return false;
    }
    
    
    @Override
    public boolean isValid() {
        if (page instanceof IFormEditorPage2) {
            if (!((IFormEditorPage2)page).isValid()) {
                return false;
            }
        }
        for (FormFieldComposite field : fields.values()) {
            if (!field.isValid()) {
                return false;
            }
        }
        return true;
    }
    
    
    public Map<Property,Object> doSubmit( IProgressMonitor monitor )
    throws Exception {
        Map<Property,Object> result = new HashMap();
        
        for (FormFieldComposite field : fields.values()) {
            if (field.isDirty()) {
                Object newValue = field.store();
                Object old = result.put( field.getProperty(), newValue );
                if (old != null) {
                    throw new RuntimeException( "Submitted value already exists for property: " + field.getProperty() );
                }
            }
        }

        // after form fields in order to allow subclassed Property instances
        // to be notified of submit
        if (page instanceof IFormEditorPage2) {
            ((IFormEditorPage2)page).doSubmit( monitor );
        }

        return result;
    }

    
    public void doLoad( IProgressMonitor monitor )
    throws Exception {
        if (page instanceof IFormEditorPage2) {
            ((IFormEditorPage2)page).doLoad( monitor );
        }

        try {
            // do not dispatch events while loading
//            blockEvents = true;

            for (FormFieldComposite field : fields.values()) {
                field.load();
            }
        }
        finally {
            blockEvents = false;
        }
    }

    
    // IFormEditorPageSite ****************************
    
    public Composite newFormField( Composite parent, Property prop, IFormField field, IFormFieldValidator validator ) {
        return newFormField( parent, prop, field, validator, null );
    }


    public Composite newFormField( Composite parent, Property prop, IFormField field, IFormFieldValidator validator, String label ) {
        FormFieldComposite result = new FormFieldComposite( editor, this, getToolkit(), prop, field,
                new DefaultFormFieldLabeler( labelWidth, label ), new DefaultFormFieldDecorator(), 
                validator != null ? validator : new NullValidator() );
        fields.put( result.getFieldName(), result );
        
        return result.createComposite( parent != null ? parent : getPageBody(), SWT.NONE );
    }

    
    public void setFieldValue( String fieldName, Object value ) {
        FormFieldComposite field = fields.get( fieldName );
        if (field != null) {
            field.setFormFieldValue( value );            
        }
        else {
            throw new RuntimeException( "No such field: " + fieldName );
        }
    }

    
    @Override
    public <T> T getFieldValue( String fieldName ) {
        return (T)values.get( fieldName );
    }


    public void setFieldEnabled( String fieldName, boolean enabled ) {
        FormFieldComposite field = fields.get( fieldName );
        if (field != null) {
            field.setEnabled( enabled );            
        }
        else {
            throw new RuntimeException( "No such field: " + fieldName );
        }
    }

    
    public void reloadEditor()
    throws Exception {
        doLoad( new NullProgressMonitor() );
    }

    
    public void submitEditor()
    throws Exception {
        doSubmit( new NullProgressMonitor() );
    }

    
    public void clearFields() {
        throw new RuntimeException( "not yet implemented." );
//        dispose();
//        
//        // dispose any left sections and stuff
//        for (Control child : getPageBody().getChildren()) {
//            if (!child.isDisposed()) {
//                child.dispose();
//            }
//        }
    }

}