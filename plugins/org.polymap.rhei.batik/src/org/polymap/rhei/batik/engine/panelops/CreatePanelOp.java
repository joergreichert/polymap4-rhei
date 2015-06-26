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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Mandatory;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.app.IAppManager;
import org.polymap.rhei.batik.engine.BatikFactory;
import org.polymap.rhei.batik.engine.BatikFactory.PanelExtensionPoint;
import org.polymap.rhei.batik.engine.DefaultAppContext;
import org.polymap.rhei.batik.engine.PanelContextInjector;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CreatePanelOp
        extends PanelOp<IPanel> {

    private static Log log = LogFactory.getLog( CreatePanelOp.class );

    @Mandatory
    public Config<CreatePanelOp,PanelPath>      parentPath;

    @Mandatory
    public Config<CreatePanelOp,PanelIdentifier> panelId;


    @Override
    public IPanel execute( IPanelOpSite site ) {
        IAppManager manager = BatikApplication.instance().getAppManager();
        DefaultAppContext context = (DefaultAppContext)manager.getContext();
        
        // create, filter, init, add panels
        PanelExtensionPoint ep = BatikFactory.instance().allPanelExtensionPoints()
                .filter( _ep -> _ep.panel.id().equals( panelId.get() ) )
                .findFirst().orElseThrow( () -> new IllegalStateException( "No such panel: " + panelId.get() ) );
                
        new PanelContextInjector( ep.panel, context ).run();
        PanelPath path = parentPath.get().append( ep.panel.id() );
        IPanelSite panelSite = site.getOrCreatePanelSite( path, ep.stackPriority );
        ep.panel.setSite( panelSite, context );
        if (ep.panel.getSite() == null) {
            throw new IllegalStateException( "Panel.getSite() == null after setSite()!");
        }
        return ep.panel;
    }
    
}
