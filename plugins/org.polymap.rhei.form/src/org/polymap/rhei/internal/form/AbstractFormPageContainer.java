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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.NullValidator;
import org.polymap.rhei.form.FormFieldBuilder;
import org.polymap.rhei.form.IFormPage;
import org.polymap.rhei.form.IFormPage2;
import org.polymap.rhei.form.IFormPageSite;
import org.polymap.rhei.internal.DefaultFormFieldDecorator;
import org.polymap.rhei.internal.DefaultFormFieldLabeler;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class AbstractFormPageContainer
        implements IFormPageSite {
    
    private static Log log = LogFactory.getLog( AbstractFormPageContainer.class );

    private Object                          editor;
    
    protected IFormPage               page;
    
    private Map<String,FormFieldComposite>  fields = new HashMap( 64 );
    
    private Map<String,Object>              values = new HashMap( 64 );

    private volatile boolean                blockEvents;
    
    private int                             labelWidth = 100;

    
    public AbstractFormPageContainer( Object editor, IFormPage page, String id, String title ) {
        this.editor = editor;
        this.page = page;
    }
    
    
    public synchronized void dispose() {
        if (page != null && page instanceof IFormPage2) {
            ((IFormPage2)page).dispose();
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
    @Override
    public void fireEvent( Object source, String fieldName, int eventCode, 
            Object newFieldValue, Object newModelValue ) {
        if (eventCode == IFormFieldListener.VALUE_CHANGE) {
            if (newModelValue == null) {
                values.remove( fieldName );
            } else {
                values.put( fieldName, newModelValue );
            }
        }
        
        if (!blockEvents) {
            // syncPublish() helps to avoid to much UICallbacks from browser,
            // which slow down form performance
            FormFieldEvent ev = new FormFieldEvent( editor, source, 
                    fieldName, fields.get( fieldName ).getField(), eventCode, newFieldValue, newModelValue );
            log.debug( "" + ev );
            EventManager.instance().syncPublish( ev );
        }
    }
    

    @Override
    public boolean isDirty() {
        if (page instanceof IFormPage2) {
            if (((IFormPage2)page).isDirty()) {
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
        if (page instanceof IFormPage2) {
            if (!((IFormPage2)page).isValid()) {
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
        if (page instanceof IFormPage2) {
            ((IFormPage2)page).doSubmit( monitor );
        }

        return result;
    }

    
    public void doLoad( IProgressMonitor monitor )
    throws Exception {
        if (page instanceof IFormPage2) {
            ((IFormPage2)page).doLoad( monitor );
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

    
    // IFormPageSite ****************************
    
    @Override
    public FormFieldBuilder newFormField( Property property ) {
        return new FormFieldBuilder( property ) {
            @Override
            protected Composite createFormField() {
                FormFieldComposite result = new FormFieldComposite( 
                        editor, 
                        AbstractFormPageContainer.this, 
                        getToolkit(), 
                        property.get(), 
                        field.get(),
                        new DefaultFormFieldLabeler( labelWidth, label.get() ), 
                        new DefaultFormFieldDecorator(), 
                        validator.orElse( new NullValidator() ) );
                
                fields.put( result.getFieldName(), result );

                return result.createComposite( parent.orElse( getPageBody() ), SWT.NONE );
            }
        };
    }


    @Override
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