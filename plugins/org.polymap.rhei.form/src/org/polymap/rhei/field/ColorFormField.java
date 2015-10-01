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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.polymap.rhei.form.IFormToolkit;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ColorFormField
        implements IFormField {

    private IFormFieldSite site;

    private Button         button;

    private RGB            rgb;

    private Object         loadedValue;

    private boolean        deferredEnabled = true;


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
        button = toolkit.createButton( parent, "choose...", SWT.PUSH );
        button.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                final Display display = parent.getDisplay();
                final ColorDialog colorDialog = new ColorDialog( display.getActiveShell() );
                RGB newRgb = colorDialog.open();
                if(newRgb != null) {
                    rgb = newRgb;
                    button.setBackground( new Color( Display.getDefault(), rgb ) );
                }
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
        site.setFieldValue( rgb );
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

        rgb = loadedValue instanceof RGB ? (RGB)loadedValue : null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.rhei.field.IFormField#setValue(java.lang.Object)
     */
    @Override
    public IFormField setValue( Object value ) {
        if (value instanceof RGB) {
            rgb = (RGB)value;
        }
        return this;
    }
}
