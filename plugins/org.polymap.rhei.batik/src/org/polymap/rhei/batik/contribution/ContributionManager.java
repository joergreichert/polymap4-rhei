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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MapMaker;

import org.polymap.core.runtime.session.SessionSingleton;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ContributionManager
        extends SessionSingleton {

    private static Log log = LogFactory.getLog( ContributionManager.class );
    
    public static ContributionManager instance() {
        return instance( ContributionManager.class );
    }
    
    private static Set<Class<? extends IContributionFactory>> staticFactories = Collections.newSetFromMap( 
            new MapMaker().concurrencyLevel( 2 ).weakKeys().makeMap() );
    
    
    public static boolean addContribution( Class<? extends IContributionFactory> factory ) {
        return staticFactories.add( factory );
    }
    
    
    public boolean removeContribution( Class<IContributionFactory> factory ) {
        return staticFactories.remove( factory );        
    }

    
    // instance *******************************************
    
    public void contributeFab( IPanel panel ) {
        for (IContributionFactory factory : factories()) {
            factory.fillFab( newSite( panel ) );
        }
    }

    
    protected Iterable<IContributionFactory> factories() {
        IAppContext context = BatikApplication.instance().getContext();
        List<IContributionFactory> result = new ArrayList();
        for (Class<? extends IContributionFactory> cl : staticFactories) {
            try {
                IContributionFactory factory = cl.newInstance();
                context.propagate( factory );
                result.add( factory );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        return result;
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
