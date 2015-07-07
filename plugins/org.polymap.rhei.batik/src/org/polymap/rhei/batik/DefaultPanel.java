/*
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.batik;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides handling of {@link IPanelSite} and {@link IAppContext} variables.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( DefaultPanel.class );
    
    private IPanelSite          site;

    private IAppContext         context;


    @Override
    public void setSite( IPanelSite site, IAppContext context ) {
        assert this.site == null : "site is not null";
        assert this.context == null : "context is not null";
        this.site = site;
        this.context = context;
    }


    /** 
     * This default implementation always returns <code>false</code>. 
     */
    @Override
    public boolean wantsToBeShown() {
        return false;
    }


    /**
     * This default implementation does nothing.
     */
    @Override
    public void init() {
    }


    /**
     * This default implementation returns the value of the first found static member
     * of type {@link PanelIdentifier} of this class.
     * 
     * @throws IllegalStateException If no such member is found.
     */
    @Override
    public PanelIdentifier id() {
        for (Field f : getClass().getDeclaredFields()) {
            if (Modifier.isStatic( f.getModifiers() )
                    && PanelIdentifier.class.isAssignableFrom( f.getType() )) {
                f.setAccessible( true );
                try {
                    return (PanelIdentifier)f.get( null );
                }
                catch (Exception e) {
                    // must never happen !?
                }
            }
        }
        throw new IllegalStateException( "This class does not provide a static member of type PanelIdentifier!" );
    }


    /**
     * This default implementation does nothing.
     */
    @Override
    public void dispose() {
    }


    @Override
    public IPanelSite getSite() {
        return site;
    }


    public IAppContext getContext() {
        return context;
    }

}
