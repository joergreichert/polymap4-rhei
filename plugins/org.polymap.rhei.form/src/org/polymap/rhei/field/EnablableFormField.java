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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.polymap.rhei.form.IFormToolkit;

/**
 * This form field has got a checkbox in front of the actual field making this
 * enabled or disabled.
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class EnablableFormField
        implements IFormField {

    private static Log        log = LogFactory.getLog( EnablableFormField.class );

    // instance *******************************************

    protected IFormFieldSite  site;

    private CheckboxFormField checkboxField;

    private IFormField        actualField;

    protected Object          checkboxValue, actualValue;

    private Object[]          loadedValue;

    private Button checkboxControl;

    private Control actualFieldControl;


    public EnablableFormField( CheckboxFormField checkboxField, IFormField actualField ) {
        super();
        this.checkboxField = checkboxField;
        this.actualField = actualField;
    }


    public void init( IFormFieldSite _site ) {
        this.site = _site;

        // field1
        checkboxField.init( new DelegateSite( site ) {

            public Object getFieldValue() throws Exception {
                return loadedValue != null ? loadedValue[0] : null;
            }


            public void setFieldValue( Object value ) throws Exception {
                throw new RuntimeException( "not yet implemented." );
            }
        } );

        // field2
        actualField.init( new DelegateSite( site ) {

            public Object getFieldValue() throws Exception {
                return loadedValue != null ? loadedValue[1] : null;
            }


            public void setFieldValue( Object value ) throws Exception {
                actualValue = value;
            }
        } );
    }


    public void dispose() {
        if (checkboxField != null) {
            checkboxField.dispose();
            checkboxField = null;
        }
        if (actualField != null) {
            actualField.dispose();
            actualField = null;
        }
    }


    public Control createControl( Composite parent, IFormToolkit toolkit ) {
        Composite contents = toolkit.createComposite( parent );
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        contents.setLayout( layout );

        checkboxControl = (Button) checkboxField.createControl( contents, toolkit );
        actualFieldControl = actualField.createControl( contents, toolkit );
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        actualFieldControl.setLayoutData( gridData );
        
        actualField.setEnabled( checkboxControl.getSelection() );
        checkboxControl.addSelectionListener( new SelectionAdapter() {
            
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected( SelectionEvent e ) {
                actualField.setEnabled( checkboxControl.getSelection() );
            }
        } );

        contents.pack( true );
        return contents;
    }


    public IFormField setEnabled( boolean enabled ) {
        if (checkboxControl != null && !checkboxControl.isDisposed()) {
            checkboxControl.setEnabled( enabled );
            actualFieldControl.setEnabled( checkboxControl.getSelection() );
        }
        return this;
    }


    public IFormField setValue( Object value ) {
        throw new RuntimeException( "Not yet implemented." );
    }


    public void load() throws Exception {
        assert checkboxField != null || actualField != null : "Control is null, call createControl() first.";

        // FIXME makes DateField be dirty?
        if (site.getFieldValue() == null) {
            loadedValue = null;
            checkboxField.load();
            actualField.load();
        }
        else if (site.getFieldValue() instanceof Object[]) {
            loadedValue = (Object[])site.getFieldValue();
            checkboxField.load();
            actualField.load();
        }
        else {
            log.warn( "Unknown value type: " + site.getFieldValue() );
        }
    }


    public void store() throws Exception {
        if (checkboxValue instanceof Boolean && (Boolean)checkboxValue) {
            site.setFieldValue( actualValue );
        }
    }


    public void fireEvent( Object eventSrc, int eventCode, Object newValue ) {
        log.debug( "fireEvent(): ev=" + eventCode + ", newValue=" + newValue );

        if (eventCode == IFormFieldListener.VALUE_CHANGE && eventSrc == checkboxField) {
            checkboxValue = newValue;
        }
        if (eventCode == IFormFieldListener.VALUE_CHANGE && eventSrc == actualField) {
            actualValue = newValue;
        }

        Object value = checkboxValue != null || actualValue != null ? new Object[] { checkboxValue, actualValue } : null;
        site.fireEvent( this, eventCode, value );
    }


    /**
     * 
     */
    abstract class DelegateSite
            implements IFormFieldSite {

        private IFormFieldSite delegate;


        public DelegateSite( IFormFieldSite delegate ) {
            this.delegate = delegate;
        }


        public void addChangeListener( IFormFieldListener l ) {
            delegate.addChangeListener( l );
        }


        public void fireEvent( Object source, int eventCode, Object newValue ) {
            EnablableFormField.this.fireEvent( source, eventCode, newValue );
        }


        public String getErrorMessage() {
            return delegate.getErrorMessage();
        }


        public String getFieldName() {
            return delegate.getFieldName();
        }


        public IFormToolkit getToolkit() {
            return delegate.getToolkit();
        }


        public boolean isDirty() {
            return delegate.isDirty();
        }


        public boolean isValid() {
            return delegate.isValid();
        }


        public void removeChangeListener( IFormFieldListener l ) {
            delegate.removeChangeListener( l );
        }


        public void setErrorMessage( String msg ) {
            delegate.setErrorMessage( msg );
        }

    }
}