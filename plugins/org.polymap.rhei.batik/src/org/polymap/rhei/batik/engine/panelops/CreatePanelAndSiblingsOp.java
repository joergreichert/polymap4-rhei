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
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CreatePanelAndSiblingsOp
        extends PanelOp {

    private static Log log = LogFactory.getLog( CreatePanelAndSiblingsOp.class );

    @Mandatory
    public Config<CreatePanelAndSiblingsOp,PanelPath>   prefix;

    @Mandatory
    public Config<CreatePanelAndSiblingsOp,PanelIdentifier> panelId;


    @Override
    public Object execute( IPanelOpSite site ) {
        throw new RuntimeException( "check the code!" );
//        DefaultAppContext context = (DefaultAppContext)manager.getContext();
//        
//        // create, filter, init, add panels
//        BatikFactory.instance().allPanelExtensionPoints()
//                // sort in order to initialize main panel context first
//                .sorted( Collections.reverseOrder( Comparator.comparing( ep -> ep.stackPriority ) ) )
//                // initialize, then filter
//                .filter( ep -> {
//                        new PanelContextInjector( ep.panel, context ).run();
//                        PanelPath path = prefix.get().append( ep.panel.id() );
//                        IPanelSite panelSite = site.getOrCreatePanelSite( path, ep.stackPriority );
//                        ep.panel.setSite( panelSite, context );
//                        if (ep.panel.getSite() == null) {
//                            throw new IllegalStateException( "Panel.getSite() == null after setSite()!");
//                        }
//                        return ep.panel.id().equals( panelId.get() ) || ep.panel.wantsToBeShown(); 
//                })
//                .filter( Predicates.notNull() )
//                .map( ep -> ep.panel )
//                .forEach( panel -> {
//                        manager.addPanel( panel, prefix.get().append( panel.id() ) );
//                        manager.updatePanelStatus( panel, PanelStatus.CREATED );
//                });
    }
    
}
