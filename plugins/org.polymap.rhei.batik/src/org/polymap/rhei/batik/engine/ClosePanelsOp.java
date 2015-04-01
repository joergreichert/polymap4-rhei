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
package org.polymap.rhei.batik.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.config.Property;

import org.polymap.rhei.batik.PanelPath;

/**
 * Closes all panels down to the given target panel. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ClosePanelsOp
        extends PanelOp {

    private static Log log = LogFactory.getLog( ClosePanelsOp.class );
    
    @Mandatory
    public Property<PanelPath>      panelPath;

    
    @Override
    public void execute() {
        int pathSize = panelPath.get().size();
        manager.getContext()
                .findPanels( panel -> panel.getSite().getPath().size() > pathSize )
                .forEach( panel -> closePanel( panel.getSite().getPath() ) );
    }
    
    
    protected void closePanel( PanelPath _panelPath ) {
        manager.runOp( ClosePanelOp.class, op -> op.panelPath.set( _panelPath ) );    
    }
    
}
