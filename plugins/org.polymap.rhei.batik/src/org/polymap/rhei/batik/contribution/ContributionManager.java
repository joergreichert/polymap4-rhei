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
package org.polymap.rhei.batik.contribution;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Streams;
import org.polymap.core.runtime.session.SessionSingleton;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 * Registry for contribution providers and API to contribute to UI. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ContributionManager
        extends SessionSingleton {

    private static Log log = LogFactory.getLog( ContributionManager.class );
    
    public static ContributionManager instance() {
        return instance( ContributionManager.class );
    }
    
    private static List<Supplier<IContributionFactory>> staticSuppliers = new CopyOnWriteArrayList();
    
    
    public static boolean addStaticSupplier( Supplier<IContributionFactory> supplier ) {
        return staticSuppliers.add( supplier );
    }
    
    
    public static boolean removeStaticSupplier( Supplier<IContributionFactory> supplier ) {
        return staticSuppliers.remove( supplier );
    }
    
    
    // instance *******************************************

    private static List<Supplier<IContributionFactory>> suppliers = new CopyOnWriteArrayList();
    
    
    public void contributeFab( IPanel panel ) {
        factories().forEach( factory -> factory.fillFab( newSite( panel ) ) );
    }

    
    protected Iterable<IContributionFactory> factories() {
        IAppContext context = BatikApplication.instance().getContext();
        
        return Streams.iterable(
                Stream.concat( staticSuppliers.stream(), suppliers.stream() )
                .map( supplier -> {
                    IContributionFactory factory = supplier.get();
                    context.propagate( factory );
                    return factory;
                }));
    }

    
    protected IContributionSite newSite( IPanel panel ) {
        return new IContributionSite() {
            @Override
            public IPanel getPanel() {
                return panel;
            }
            @Override
            public IPanelSite getPanelSite() {
                return panel.getSite();
            }
            @Override
            public IAppContext getContext() {
                return BatikApplication.instance().getContext();
            }
            @Override
            public <T extends IPanelToolkit> T toolkit() {
                return (T)getPanelSite().toolkit();
            }
            
        };
    }
    
    
}
