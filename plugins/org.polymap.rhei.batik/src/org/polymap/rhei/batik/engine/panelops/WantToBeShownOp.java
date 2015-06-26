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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Predicates;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Mandatory;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.engine.BatikFactory;
import org.polymap.rhei.batik.engine.DefaultAppContext;
import org.polymap.rhei.batik.engine.DefaultAppManager;
import org.polymap.rhei.batik.engine.PanelContextInjector;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WantToBeShownOp
        extends PanelOp<List<IPanel>> {

    private static Log log = LogFactory.getLog( WantToBeShownOp.class );

    @Mandatory
    public Config<WantToBeShownOp,PanelPath>   parentPath;


    @Override
    public List<IPanel> execute( IPanelOpSite site ) {
        DefaultAppManager manager = (DefaultAppManager)BatikApplication.instance().getAppManager();
        DefaultAppContext context = (DefaultAppContext)manager.getContext();
        PanelIdentifier panelId = parentPath.get().lastSegment();
        
        // create, filter, init, add panels
        return BatikFactory.instance().allPanelExtensionPoints()
                // sort in order to initialize main panel context first
                .sorted( Collections.reverseOrder( Comparator.comparing( ep -> ep.stackPriority ) ) )
                // initialize, then filter
                .filter( ep -> {
                        new PanelContextInjector( ep.panel, context ).run();
                        PanelPath path = parentPath.get().append( ep.panel.id() );
                        IPanelSite panelSite = site.getOrCreatePanelSite( path, ep.stackPriority );
                        ep.panel.setSite( panelSite, context );
                        if (ep.panel.getSite() == null) {
                            throw new IllegalStateException( "Panel.getSite() == null after setSite()!");
                        }
                        return ep.panel.id().equals( panelId ) || ep.panel.wantsToBeShown(); 
                })
                .filter( Predicates.notNull() )
                .map( ep -> ep.panel )
                .collect( Collectors.toList() );
    }
    
}
