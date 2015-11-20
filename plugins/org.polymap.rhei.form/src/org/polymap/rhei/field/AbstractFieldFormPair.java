/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.rhei.field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.polymap.rhei.form.IFormToolkit;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public abstract class AbstractFieldFormPair
        implements IFormField, IEventAware {

    private static Log       log = LogFactory.getLog( AbstractFieldFormPair.class );

    // instance *******************************************

    protected IFormFieldSite site;

    protected IFormField     field1;

    protected IFormField     field2;

    protected Object         newValue1, newValue2;

    protected Object[]       loadedValue;


    public AbstractFieldFormPair( IFormField field1, IFormField field2 ) {
        super();
        this.field1 = field1;
        this.field2 = field2;
    }


    public void init( IFormFieldSite _site ) {
        this.site = _site;

        // field1
        field1.init( new DelegateSite( site, this ) {

            public Object getFieldValue() throws Exception {
                return loadedValue != null ? loadedValue[0] : null;
            }


            public void setFieldValue( Object value ) throws Exception {
                newValue1 = value;
            }
        } );

        // field2
        field2.init( new DelegateSite( site, this ) {

            public Object getFieldValue() throws Exception {
                return loadedValue != null ? loadedValue[1] : null;
            }


            public void setFieldValue( Object value ) throws Exception {
                newValue2 = value;
            }
        } );
    }


    public void dispose() {
        if (field1 != null) {
            field1.dispose();
            field1 = null;
        }
        if (field2 != null) {
            field2.dispose();
            field2 = null;
        }
    }


    public abstract Control createControl( Composite parent, IFormToolkit toolkit );


    public IFormField setEnabled( boolean enabled ) {
        field1.setEnabled( enabled );
        field2.setEnabled( enabled );
        return this;
    }


    public IFormField setValue( Object value ) {
        throw new RuntimeException( "Not yet implemented." );
    }


    public void load() throws Exception {
        assert field1 != null || field2 != null : "Control is null, call createControl() first.";

        // FIXME makes DateField be dirty?
        if (site.getFieldValue() == null) {
            loadedValue = null;
            field1.load();
            field2.load();
        }
        else if (isExpectedFieldValueType()) {
            handleExpectedFieldValueType();
        }
        else {
            log.warn( "Unknown value type: " + site.getFieldValue() );
        }
    }


    protected boolean isExpectedFieldValueType() throws Exception {
        return site.getFieldValue() instanceof Object[];
    }

    
    protected void handleExpectedFieldValueType() throws Exception {
        loadedValue = (Object[])site.getFieldValue();
        field1.load();
        field2.load();
    }

    
    public void store() throws Exception {
        site.setFieldValue( new Object[] { newValue1, newValue2 } );
    }


    public void fireEvent( Object eventSrc, int eventCode, Object newValue ) {
        log.debug( "fireEvent(): ev=" + eventCode + ", newValue=" + newValue );

        assignValues( eventSrc, eventCode, newValue );
        postProcessValues();

        Object value = newValue1 != null || newValue2 != null ? new Object[] { newValue1, newValue2 } : null;
        site.fireEvent( this, eventCode, value );
    }


    private void assignValues( Object eventSrc, int eventCode, Object newValue ) {
        if (eventCode == IFormFieldListener.VALUE_CHANGE && eventSrc == field1) {
            newValue1 = newValue;
            // newValue2 = newValue2 == null ? newValue1 : newValue2;
        }
        if (eventCode == IFormFieldListener.VALUE_CHANGE && eventSrc == field2) {
            newValue2 = newValue;
            // newValue1 = newValue1 == null ? newValue2 : newValue1;
        }
    }


    protected abstract void postProcessValues();
}
