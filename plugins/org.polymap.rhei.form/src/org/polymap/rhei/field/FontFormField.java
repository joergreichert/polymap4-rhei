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
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.polymap.rhei.form.IFormToolkit;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class FontFormField
        implements IFormField {

    /**
     * 
     */
    private static final String INITIAL_LABEL   = "Choose...";

    private IFormFieldSite      site;

    private Button              button;

    private FontData            fontData;

    private RGB                 rgb;

    private Object              loadedValue;

    private boolean             deferredEnabled = true;

    private static final RGB    DISABLED_COLOR  = new RGB( 150, 150, 150 );


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
                site.fireEvent( FontFormField.this, IFormFieldListener.VALUE_CHANGE, new Object[] { fontData, rgb } );
                // final Display display = parent.getDisplay();
                // final FontDialog fontDialog = new FontDialog(
                // display.getActiveShell() );
                // fontDialog.setFontList( new FontData[] { fontData } );
                // fontDialog.setRGB( rgb );
                // FontData newFontData = fontDialog.open();
                // if (newFontData != null) {
                // fontData = newFontData;
                // button.setText( fontData.getName() + ", " + fontData.getHeight()
                // );
                // rgb = fontDialog.getRGB();
                // button.setBackground( new Color( Display.getDefault(), rgb ) );
                // }
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
            if (!enabled) {
                button.setBackground( new Color( button.getDisplay(), DISABLED_COLOR.red, DISABLED_COLOR.green,
                        DISABLED_COLOR.blue ) );
                button.setText( INITIAL_LABEL );
            }
            else {
                updateButton();
            }
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
        site.setFieldValue( fontData );
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

        if (loadedValue instanceof Object[]) {
            Object[] array = (Object[])loadedValue;
            if (array.length > 0) {
                fontData = (FontData)array[0];
            }
            if (array.length > 1) {
                rgb = (RGB)array[1];
            }
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.rhei.field.IFormField#setValue(java.lang.Object)
     */
    @Override
    public IFormField setValue( Object value ) {
        if (value instanceof Object[]) {
            Object[] array = (Object[])value;
            if (array.length == 2) {
                fontData = ((FontData[])array[0])[0];
                rgb = (RGB)array[1];
            }
            updateButton();
        }
        return this;
    }


    private void updateButton() {
        if (button != null && button.isEnabled()) {
            if (fontData != null) {
                button.setText( fontData.getName() + ", " + fontData.getHeight() );
                if (rgb != null) {
                    button.setBackground( new Color( Display.getDefault(), rgb ) );
                }
                else {
                    button.setBackground( null );
                }
            }
        }
    }
}
