/* 
 * polymap.org
 * Copyright (C) 2010-2015, Falko Bräutigam. All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IBasePageSite;

/**
 * Most of the page logic of form and filter pages.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class BasePageController<FC extends BaseFieldComposite>
        implements IBasePageSite {
    
    private static Log log = LogFactory.getLog( BasePageController.class );

    protected Object                        editor;
    
    protected Map<String,FC>                fields = new HashMap( 64 );
    
    protected Map<String,Object>            values = new HashMap( 64 );

    private volatile boolean                blockEvents;
    
    protected int                           labelWidth = 100;

    
    public BasePageController( Object editor, String id, String title ) {
        this.editor = editor;
    }
    

    public synchronized void dispose() {
        fields.values().forEach( field -> field.dispose() );
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
        return fields.values().stream().anyMatch( field -> field.isDirty() );
    }
    
    
    @Override
    public boolean isValid() {
        return !fields.values().stream().anyMatch( field -> !field.isValid() );
    }
    
    
    // IBasePageSite **************************************
    
    @Override
    public void setFieldValue( String fieldName, Object value ) {
        FC field = fields.get( fieldName );
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


//    @Override
//    public Map<String,Object> getFieldValues() {
//        return (T)values.get( fieldName );
//    }


    @Override
    public void setFieldEnabled( String fieldName, boolean enabled ) {
        FC field = fields.get( fieldName );
        if (field != null) {
            field.setEnabled( enabled );            
        }
        else {
            throw new RuntimeException( "No such field: " + fieldName );
        }
    }

    
    @Override
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