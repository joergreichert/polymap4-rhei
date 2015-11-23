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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.polymap.rhei.form.IFormToolkit;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public abstract class AbstractDelegatingFormField<T>
        implements IFormField {

    private IFormFieldSite site;

    private Button         button;

    private T              currentValue;

    private Object         loadedValue;

    private boolean        deferredEnabled = true;


    @Override
    public void init( IFormFieldSite site ) {
        this.site = site;

    }


    @Override
    public void dispose() {
        button.dispose();
    }


    @Override
    public Control createControl( Composite parent, IFormToolkit toolkit ) {
        button = toolkit.createButton( parent, getInitialLabel(), SWT.PUSH );
        button.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                handleSelectionEvent( site, e );
            }

        } );
        button.setEnabled( deferredEnabled );
        return button;
    }


    protected void handleSelectionEvent( IFormFieldSite site, org.eclipse.swt.events.SelectionEvent e ) {
        site.fireEvent( AbstractDelegatingFormField.this, IFormFieldListener.VALUE_CHANGE, currentValue );
    }


    protected String getInitialLabel() {
        return "Configure";
    }


    protected T getCurrentValue() {
        return currentValue;
    }


    protected void setCurrentValue( T value ) {
        this.currentValue = value;
    }


    @Override
    public IFormField setEnabled( boolean enabled ) {
        if (button != null && !button.isDisposed()) {
            button.setEnabled( enabled );
            styleButton();
        }
        else {
            deferredEnabled = enabled;
        }
        return this;
    }


    private void styleButton() {
        if (!button.isEnabled()) {
            button.setText( getDisabledButtonText( button ) );
            button.setBackground( getDisabledButtonBackground( button ) );
        }
        else {
            button.setText( getButtonText( button ) );
            button.setBackground( getButtonBackground( button ) );
        }
    }


    protected String getButtonText( Button button ) {
        return "Configure...";
    }


    protected String getDisabledButtonText( Button button ) {
        return "Configure...";
    }


    protected Color getButtonBackground( Button button ) {
        return null;
    }


    protected Color getDisabledButtonBackground( Button button ) {
        return null;
    }


    @Override
    public void store() throws Exception {
        site.setFieldValue( currentValue );
    }


    @Override
    public void load() throws Exception {
        assert button != null : "Control is null, call createControl() first.";

        loadedValue = site.getFieldValue();

        processLoadedValue( loadedValue );

        updateButton();
    }


    protected abstract void processLoadedValue( Object loadedValue );


    protected void updateButton() {
        if (button != null) {
            styleButton();
        }
    }
}
