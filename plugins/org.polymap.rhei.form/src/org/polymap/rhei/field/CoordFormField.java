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

import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.polymap.rhei.form.IFormToolkit;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class CoordFormField
        extends AbstractFieldFormPair {

    public CoordFormField( IFormField field1, IFormField field2 ) {
        super( field1, field2 );
    }


    public Control createControl( Composite parent, IFormToolkit toolkit ) {
        Composite contents = toolkit.createComposite( parent );
        RowLayout layout = new RowLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.spacing = 3;
        // layout.justify = true;
        layout.center = true;
        contents.setLayout( layout );

        toolkit.createLabel( contents, "x:" );
        field1.createControl( contents, toolkit );
        toolkit.createLabel( contents, "y:" );
        field2.createControl( contents, toolkit );

        contents.pack( true );
        return contents;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.rhei.field.AbstractFieldFormPair#postProcessValues()
     */
    @Override
    protected void postProcessValues() {
    }
}
