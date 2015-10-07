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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.polymap.rhei.form.IFormToolkit;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class IconFormField
        implements IFormField {

    /**
     * 
     */
    private static final String INITIAL_LABEL   = "Choose...";

    private IFormFieldSite      site;

    private Button              button;

    private ImageDescription    imageDescription;

    private Object              loadedValue;

    private boolean             deferredEnabled = true;


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.polymap.rhei.field.IFormField#init(org.polymap.rhei.field.IFormFieldSite)
     */
    @Override
    public void init( IFormFieldSite site ) {
        this.site = site;

    }


    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.rhei.field.IFormField#dispose()
     */
    @Override
    public void dispose() {
        button.dispose();
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.polymap.rhei.field.IFormField#createControl(org.eclipse.swt.widgets.Composite
     * , org.polymap.rhei.form.IFormToolkit)
     */
    @Override
    public Control createControl( Composite parent, IFormToolkit toolkit ) {
        button = toolkit.createButton( parent, INITIAL_LABEL, SWT.PUSH );
        button.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                site.fireEvent( IconFormField.this, IFormFieldListener.VALUE_CHANGE, imageDescription );
            };
        } );
        button.setEnabled( deferredEnabled );
        return button;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.rhei.field.IFormField#setEnabled(boolean)
     */
    @Override
    public IFormField setEnabled( boolean enabled ) {
        if (button != null) {
            button.setEnabled( enabled );
        }
        else {
            deferredEnabled = enabled;
        }
        return this;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.rhei.field.IFormField#store()
     */
    @Override
    public void store() throws Exception {
        site.setFieldValue( imageDescription );
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.rhei.field.IFormField#load()
     */
    @Override
    public void load() throws Exception {
        assert button != null : "Control is null, call createControl() first.";

        loadedValue = site.getFieldValue();

        imageDescription = loadedValue instanceof ImageDescription ? (ImageDescription)loadedValue : null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.rhei.field.IFormField#setValue(java.lang.Object)
     */
    @Override
    public IFormField setValue( Object value ) {
        if (value instanceof ImageDescription) {
            this.imageDescription = (ImageDescription)value;
            updateButton();
        }
        return this;
    }


    private void updateButton() {
        if (button != null) {
            if (imageDescription != null) {

                button.setText( "" );
                button.setImage( imageDescription.getImageForSize( 16 ).createImage() );
            }
            else {
                button.setText( INITIAL_LABEL );
                button.setBackground( null );
            }
        }
    }
}
