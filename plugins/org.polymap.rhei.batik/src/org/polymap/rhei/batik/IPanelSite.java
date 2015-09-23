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

import java.util.Arrays;

import org.eclipse.swt.graphics.Image;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.LayoutSupplier;

/**
 * Provides the interface between an {@link IPanel} client code and the Batik
 * framework.
 * <p/>
 * This API will be <b>deprecated</b> in future version. Use {@link PanelSite} API
 * instead.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IPanelSite {

    /** Signals that an action or item should have 'submit' style. */
    public static final String      SUBMIT = "__submit__";

    /**
     * The lifecycle status of an {@link IPanel}.
     */
    public static enum PanelStatus {
        CREATED,
        INITIALIZED,
        VISIBLE,
        FOCUSED;
        
        public boolean isOnOf( PanelStatus... a ) {
            return Arrays.asList( a ).contains( this );
        }

        /** Greater or equal */
        public boolean ge( PanelStatus other ) {
            return compareTo( other ) >= 0;
        }
    }
    
    public PanelStatus getPanelStatus();
    
    public Memento getMemento();
    
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

    public IPanelToolkit toolkit();
    
    /**
     * Sets the title of the page. Null specifies that the panel does not show up in
     * the panel navigator bar.
     * 
     * @param title The title of the page, or null.
     * @return this;
     */
    public IPanelSite setTitle( String title );

    /**
     * Sets the title and tooltip of the page.
     * 
     * @param title The title of the page, or null.
     * @param tooltip The tooltip, or null;
     * @return this;
     */
    public IPanelSite setTooltip( String tooltip );

    /**
     * 
     *
     * @param icon
     * @return this
     */
    public IPanelSite setIcon( Image icon );

    public void layout( boolean changed );

    
    /**
     * Layout preferences should be used by client code in order to fit panel layout
     * into the layout of the application.
     */
    public LayoutSupplier getLayoutPreference();

    public void setPreferredWidth( int width );


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
