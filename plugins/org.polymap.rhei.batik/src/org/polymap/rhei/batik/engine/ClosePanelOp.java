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

import org.polymap.core.runtime.config.Check;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.config.Property;

import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelPath;

/**
 * Closes the specified panel. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ClosePanelOp
        extends PanelOp {

    private static Log log = LogFactory.getLog( ClosePanelOp.class );
    
    @Mandatory
    @Check(validator=PanelExists.class)
    public Property<PanelPath>      panelPath;

    
    @Override
    public void execute() {
        DefaultAppContext context = (DefaultAppContext)manager.getContext();
        IPanel panel = context.getPanel( panelPath.get() );
        try {
            panel.dispose();
        }
        catch (Exception e) {
            log.warn( "", e );
        }
        
        context.removePanel( panelPath.get() );
        manager.panelSites.remove( panelPath );
        manager.updatePanelStatus( panel, null );
    }
    
}
