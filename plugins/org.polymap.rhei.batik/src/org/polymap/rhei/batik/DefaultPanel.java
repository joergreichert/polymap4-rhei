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

import java.util.Optional;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.core.runtime.IStatus;

import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.LayoutSupplier;

/**
 * Provides handling of {@link IPanelSite} and {@link IAppContext} variables.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( DefaultPanel.class );
    
    private PanelSite           site;
    
    private IPanelSite          site2;
    
    private IAppContext         context;


    @Override
    public void setSite( PanelSite site, IAppContext context ) {
        assert this.site == null : "site is not null";
        assert this.context == null : "context is not null";
        this.site = site;
        this.site2 = new PanelSiteImpl();
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

    
    /**
     * The site interface of this panel.
     */
    @Override
    public PanelSite site() {
        return site;
    }
    
    
    /**
     * This will be <b>deprecated</b> in future version use {@link #site()} instead.
     */
    public IPanelSite getSite() {
        return site2;
    }


    public IAppContext getContext() {
        return context;
    }

    
    /**
     * The parent of this panel, or empty if panel is on level 1. 
     */
    protected Optional<IPanel> parentPanel() {
        return Optional.ofNullable( getContext().getPanel( getSite().getPath().removeLast( 1 ) ) );
    }

    
    /**
     *
     */
    protected class PanelSiteImpl
            implements IPanelSite {

        @Override
        public Memento getMemento() {
            return site.memento();
        }

        @Override
        public PanelStatus getPanelStatus() {
            return site.panelStatus();
        }

        @Override
        public PanelPath getPath() {
            return site.path();
        }

        @Override
        public void setStatus( IStatus status ) {
            site.status.set( status );
        }

        @Override
        public IPanelSite setTitle( String title ) {
            site.title.set( title );
            return this;
        }

        @Override
        public IPanelSite setTooltip( String tooltip ) {
            site.tooltip.set( tooltip );
            return this;
        }

        @Override
        public IPanelSite setIcon( Image icon ) {
            site.icon.set( icon );
            return this;
        }

        @Override
        public LayoutSupplier getLayoutPreference() {
            return site.layoutPreferences();
        }

        @Override
        public void setPreferredWidth( int preferredWidth ) {
            site.preferredWidth.set( preferredWidth );
        }

        @Override
        public IPanelToolkit toolkit() {
            return site.toolkit();
        }

        @Override
        public void layout( boolean changed ) {
            site.layout( changed );
        }

    }

}
