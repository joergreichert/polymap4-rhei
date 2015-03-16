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

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 * The primary interface between an {@link IPanel} and the Batik framework.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IPanelSite {

    /** Constant to be used as param for {@link #getLayoutPreference(String)}. */
    public static final String      LAYOUT_SPACING_KEY = "spacing";
    /** Constant to be used as param for {@link #getLayoutPreference(String)}. */
    public static final String      LAYOUT_MARGINS_KEY = "margins";
    /** Signals that an action or item should have 'submit' style. */
    public static final String      SUBMIT = "__submit__";

    /**
     * The whole path of the panel including the name of the panel as last segment.
     */
    public PanelPath getPath();

    /**
     * Changes the status of the panel. {@link Status#OK_STATUS} signals that the
     * panel has valid state. If status is not valid then the given message is
     * displayed.
     * <p/>
     * Use status severity as follows:
     * <ul>
     * <li>{@link Status#OK_STATUS} : Everything is ok. No message.</li>
     * <li>{@link IStatus#OK} : An action has been complete successfully. Message gets displayed.</li>
     * <li>{@link IStatus#INFO} : ...</li>
     * <li>{@link IStatus#WARNING} : The user's attention is needed.</li>
     * <li>{@link IStatus#ERROR} : An error/exception occured. An</li>
     * </ul>
     *
     * @param status The current status of the panel. {@link Status#OK_STATUS}
     *        signals that the panel has valid state.
     */
    public void setStatus( IStatus status );

    public IStatus getStatus();
    
    
    /**
     * Adds the given action to the toolbar of this panel.
     * <p/>
     * The description of the action can be set to {@link #SUBMIT} in order to hint
     * the layout engine to style the GUI element accordingly.
     * 
     * @param action
     */
    public void addToolbarAction( IAction action );

    public void addToolbarItem( IContributionItem item );

    public void addSidekick();

    public IPanelToolkit toolkit();

    
    /**
     * Sets the title of the page. Null specifies that the panel does not show up in
     * the panel navigator bar.
     * 
     * @param title The title of the page, or null.
     */
    public void setTitle( String title );

    public String getTitle();

    void setIcon( Image icon );

    public Image getIcon();

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
    public int getLayoutPreference( String key );
    
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
