/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.polymap.rhei.form.IFormToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LabelFormField
        implements IFormField {

    private static Log log = LogFactory.getLog( LabelFormField.class );
    
    private IFormFieldSite          site;
    
    private Label                   label;

    private Object                  loadedValue;


    public void init( IFormFieldSite _site ) {
        this.site = _site;
    }

    
    public void dispose() {
        label.dispose();
    }


    @Override
    public Control createControl( Composite parent, IFormToolkit toolkit ) {
        label = toolkit.createLabel( parent, "", SWT.WRAP );
        return label;
    }


    @Override
    public IFormField setEnabled( boolean enabled ) {
        return this;
    }


    @Override
    public void store() throws Exception {
    }


    @Override
    public void load() throws Exception {
        loadedValue = site.getFieldValue();
        label.setText( loadedValue != null ? loadedValue.toString() : "" );
    }


    @Override
    public IFormField setValue( Object value ) {
        label.setText( value != null ? (String)value : "" );
        return this;
    }
    
}
