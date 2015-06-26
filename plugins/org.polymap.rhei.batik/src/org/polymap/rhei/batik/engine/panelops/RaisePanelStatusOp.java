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
package org.polymap.rhei.batik.engine.panelops;

import static org.polymap.rhei.batik.IPanelSite.PanelStatus.CREATED;
import static org.polymap.rhei.batik.IPanelSite.PanelStatus.FOCUSED;
import static org.polymap.rhei.batik.IPanelSite.PanelStatus.INITIALIZED;
import static org.polymap.rhei.batik.IPanelSite.PanelStatus.VISIBLE;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite.PanelStatus;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RaisePanelStatusOp
        extends PanelOp {

    private static Log log = LogFactory.getLog( RaisePanelStatusOp.class );
    
    @Mandatory
    public Config<RaisePanelStatusOp,IPanel>      panel;
    
    @Mandatory
    public Config<RaisePanelStatusOp,PanelStatus> targetStatus;


    @Override
    public Object execute( IPanelOpSite site ) {
        // initialize
        if (panel.get().getSite().getPanelStatus() == CREATED && targetStatus.get().ge( INITIALIZED )) {
            panel.get().init();
            site.updatePanelStatus( panel.get(), INITIALIZED );
        }
        // make visible
        if (panel.get().getSite().getPanelStatus() == INITIALIZED && targetStatus.get().ge( VISIBLE )) {
            site.updatePanelStatus( panel.get(), VISIBLE );
        }
        // make active
        if (panel.get().getSite().getPanelStatus() == VISIBLE && targetStatus.get().ge( FOCUSED )) {
            site.updatePanelStatus( panel.get(), FOCUSED );
        }
        return null;
    }
    
}
