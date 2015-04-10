/*
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jface.action.IAction;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.IPanelSite.PanelStatus;

/**
 * An app context is shared by all {@link IPanel} instances in the same panel
 * hierachy.
 * <p/>
 * Properties of the context can be injected into an {@link IPanel} by declaring a
 * {@link Context} with appropriate type and scope.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IAppContext {

    /**
     * The name of the currently logged in user. The username is displayed in the the
     * header of the application or any other status area of the application layout.
     * 
     * @param username
     */
    public void setUserName( String username );
    
    public void addPreferencesAction( IAction action );
    
    
    /**
     * Opens the panel with the given <code>panelId</code> as a child of the given
     * <code>panelPath</code>. Any children of the given parent panel are closed
     * before opening the new child.
     *
     * @param panelPath The path of the parent panel to open the new panel for.
     * @param panelId The id of the panel to open
     * @return Null if the given panels was not found.
     */
    public IPanel openPanel( PanelPath panelPath, PanelIdentifier panelId );

    /**
     * Closes the panel with the given path. This panel must be the current top/active
     * panel.
     * 
     * @param panelPath
     */
    public void closePanel( PanelPath panelPath );

    public IPanel getPanel( PanelPath panelPath );

    /**
     * All direct children of the given path.
     *
     * @see Panels
     * @param path
     */
    public List<IPanel> findPanels( Predicate<IPanel> filter );

    
    /**
     * Registers the given {@link EventHandler annotated event handler} for
     * {@link PanelChangeEvent}s.
     * <p/>
     * Note that the status of the panel and the value delivered by the
     * {@link PanelChangeEvent} might not be the same. The {@link PanelStatus} is
     * raised step by step, an event handler receives several events for that.
     * <b>But</b> when the first event is handled the panel might already have the
     * target status.
     * <p/>
     * This delegates to the global {@link EventManager}.
     *
     * @see EventHandler
     * @see EventManager
     * @param annotated The annotated event handler instane.
     */
    public void addListener( Object annotated, EventFilter<PanelChangeEvent>... filters );

    public void removeListener( Object handler );

    
    /**
     * Propagates this context by injecting {@link Context} instances into
     * the given target instance.
     * 
     * @param panel
     */
    public <T> T propagate( T target );
    
}
