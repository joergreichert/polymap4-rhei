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
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
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
public abstract class AbstractEnablableFormField
        implements IFormField {

    private static Log     log = LogFactory.getLog( AbstractEnablableFormField.class );

    private IFormFieldSite site;

    private Button         checkbox;

    // XXX use (proper) validator to make the translation to String
    private Object         loadedValue;


    public void init( IFormFieldSite _site ) {
        this.site = _site;
    }


    public void dispose() {
        checkbox.dispose();
    }


    public Control createCompositeWithCheckBox( Composite parent, IFormToolkit toolkit ) {
        Composite comp = toolkit.createComposite( parent );
        RowLayout layout = new RowLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.spacing = 3;
//        layout.justify = true;
        layout.center = true;
        comp.setLayout( layout );
        
        checkbox = toolkit.createButton( comp, "", SWT.CHECK );
        
        // modify listener
        checkbox.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                log.debug( "modifyEvent(): test= " + checkbox.getSelection() );
                site.fireEvent( this, IFormFieldListener.VALUE_CHANGE,
                        loadedValue == null && !checkbox.getSelection() ? null : checkbox.getSelection() );
            }
        } );
        // focus listener
        checkbox.addFocusListener( new FocusListener() {

            public void focusLost( FocusEvent event ) {
                // checkbox.setBackground( FormEditorToolkit.textBackground );
                site.fireEvent( this, IFormFieldListener.FOCUS_LOST, checkbox.getText() );
            }


            public void focusGained( FocusEvent event ) {
                // checkbox.setBackground( FormEditorToolkit.textBackgroundFocused );
                site.fireEvent( this, IFormFieldListener.FOCUS_GAINED, checkbox.getText() );
            }
        } );
        return checkbox;
    }


    public IFormField setEnabled( boolean enabled ) {
        checkbox.setEnabled( enabled );
        return this;
    }


    public IFormField setValue( Object value ) {
        if (value instanceof Boolean) {
            Boolean boolValue = (Boolean)value;
            checkbox.setSelection( boolValue );
        }
        return this;
    }


    public void load() throws Exception {
        assert checkbox != null : "Control is null, call createControl() first.";

        loadedValue = site.getFieldValue();
        if (loadedValue instanceof Boolean) {
            checkbox.setSelection( loadedValue != null && loadedValue.toString().equalsIgnoreCase( "true" ) );
        }
    }


    public void store() throws Exception {
        site.setFieldValue( checkbox.getSelection() );
    }
}
