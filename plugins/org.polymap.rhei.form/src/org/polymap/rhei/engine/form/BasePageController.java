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
package org.polymap.rhei.engine.form;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.runtime.config.Check;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.config.NumberRangeValidator;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
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

    protected Map<String,FC>                fields = new HashMap( 64 );
    
    /** The current model values of the fields. */
    protected Map<String,Object>            values = new HashMap( 64 );

    private volatile boolean                blockEvents;
    
    /** Default is calculated dependent on display width between 110 and 150. */
    @Mandatory
    @Check( value=NumberRangeValidator.class, args={"0","1000"} )
    public Config2<BasePageController,Integer> labelWidth;

    
    public BasePageController() {
        ConfigurationFactory.inject( this );
        
        // label width
        double displayWidth = UIUtils.sessionDisplay().getBounds().width;
        // minimum 110
        double width = 110;
        // plus 10px per 100 pixel display width
        if (displayWidth > 1000) {
            width += (displayWidth - 1000) * 0.1;
        }
        // but not more than 160 :)
        width = Math.min( 150, width );
        log.info( "labelWidth: " + width );
        labelWidth.set( (int)width );
    }
    

    protected abstract Object getEditor();


    /**
     * Loads all fields by calling {@link IFormField#load()}.
     */
    public void doLoad( IProgressMonitor monitor ) throws Exception {
        for (FC field : fields.values()) {
            field.load();
        }
    }
    
    
    public synchronized void dispose() {
        fields.values().forEach( field -> field.dispose() );
        fields.clear();
    }

    
    public void addFieldListener( IFormFieldListener l ) {
        EventManager.instance().subscribe( l, ev -> !blockEvents && ((FormFieldEvent)ev).getEditor() == getEditor() );
    }
    

    public void removeFieldListener( IFormFieldListener l ) {
        EventManager.instance().unsubscribe( l );
    }

    
    /*
     * Called from page provider client code.
     */
    @Override
    public void fireEvent( Object source, String fieldName, int eventCode, Object newFieldValue, Object newModelValue ) {
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
            FormFieldEvent ev = new FormFieldEvent( getEditor(), source, 
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
    
    /**
     * The field for the given name or {@link RuntimeException}.
     */
    protected FC field( String fieldName ) {
        FC field = fields.get( fieldName );
        if (field != null) {
            return field;
        }
        else {
            throw new RuntimeException( "No such field: " + fieldName );
        }
    }

    
    @Override
    public void setFieldValue( String fieldName, Object value ) {
        field( fieldName ).setFormFieldValue( value );
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
        field( fieldName ).setEnabled( enabled );            
    }


    @Override
    public void reload( IProgressMonitor monitor ) throws Exception {
        doLoad( monitor != null ? monitor : new NullProgressMonitor() );            
    }
    
}