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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 * The interface an {@link IPanel} implementation can use to interact with the Batik
 * UI framework.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IPanelSite {

    /** Constant to be used as param for {@link #getLayoutPreference(String)}. */
    public static final String      LAYOUT_SPACING_KEY = "spacing";
    /** Constant to be used as param for {@link #getLayoutPreference(String)}. */
    public static final String      LAYOUT_MARGINS_KEY = "margins";

    /**
     * The whole path of the panel including the name of the panel as last segment.
     */
    public PanelPath getPath();

    /**
     * Changes the status of the panel. {@link Status#OK_STATUS} signals that the
     * panel has valid state. If status is not valid then the given message is
     * displayed.
     *
     * @param status The current status of the panel. {@link Status#OK_STATUS}
     *        signals that the panel has valid state.
     */
    public void setStatus( IStatus status );

    public IStatus getStatus();
    
    public void addToolbarAction( IAction action );

    public void addToolbarItem( IContributionItem item );

    public void addSidekick();

    public IPanelToolkit toolkit();

    public void setTitle( String string );

    public String getTitle();

    public void layout( boolean changed );

    /**
     * Get the layout preferences for the given key. Possible keys include:
     * <ul>
     * <li>{@link #LAYOUT_SPACING_KEY}</li>
     * <li>{@link #LAYOUT_MARGINS_KEY}</li>
     * </ul>
     *
     * @param key
     * @return The value for the given key, or null.
     */
    public <T> T getLayoutPreference( String key );
    
//    /**
//     * Registers the given {@link EventHandler event handler}.
//     *
//     * @see EventHandler
//     * @see EventManager
//     * @param handler
//     */
//    public void addEventHandler( Object handler );
//
//    public void removeEventHandler( Object handler );

}
