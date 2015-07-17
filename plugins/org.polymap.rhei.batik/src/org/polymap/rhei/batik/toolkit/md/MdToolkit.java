/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.batik.toolkit.md;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.toolkit.DefaultToolkit;

/**
 * Material design toolkit.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MdToolkit
        extends DefaultToolkit {

    private static Log log = LogFactory.getLog( MdToolkit.class );

    
    public MdToolkit( PanelPath panelPath ) {
        super( panelPath );
    }
    
    
    /**
     * Creates a Floating Action Button.
     * 
     * @see <a
     *      href="http://www.google.com/design/spec/components/buttons-floating-action-button.html">Material
     *      Design</a>.
     */
    @SuppressWarnings("javadoc")
    public Button createFab( Composite parent ) {
        throw new RuntimeException( "not yet..." );
    }

    
    public Composite createCard( Composite parent ) {
        throw new RuntimeException( "not yet..." );        
    }
    
    
//    public Tree createTree( Composite parent ) {
//        
//    }
    
}
