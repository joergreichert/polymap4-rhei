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

import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Predicates;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.config.Property;

import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.IPanelSite.PanelStatus;
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
    public Property<PanelPath>          prefix;

    @Mandatory
    public Property<PanelIdentifier>    panelId;


    @Override
    public void execute() {
        DefaultAppContext context = (DefaultAppContext)manager.getContext();
        
        // create, filter, init, add panels
        BatikFactory.instance().allPanelExtensionPoints()
                // sort in order to initialize main panel context first
                .sorted( Collections.reverseOrder( Comparator.comparing( ep -> ep.stackPriority ) ) )
                // initialize, then filter
                .filter( ep -> {
                        new PanelContextInjector( ep.panel, context ).run();
                        PanelPath path = prefix.get().append( ep.panel.id() );
                        IPanelSite site = manager.getOrCreateSite( path, ep.stackPriority );
                        ep.panel.setSite( site, context );
                        if (ep.panel.getSite() == null) {
                            throw new IllegalStateException( "Panel.getSite() == null after setSite()!");
                        }
                        return ep.panel.id().equals( panelId.get() ) || ep.panel.wantsToBeShown(); 
                })
                .filter( Predicates.notNull() )
                .map( ep -> ep.panel )
                .forEach( panel -> {
                        context.addPanel( panel, prefix.get().append( panel.id() ) );
                        manager.updatePanelStatus( panel, PanelStatus.CREATED );
                });
    }
    
}
