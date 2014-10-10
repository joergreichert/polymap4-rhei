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

import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.batik.toolkit.ConstraintLayout;

/**
 * The panel is the main visual component of the Atlas UI. It typically provides a
 * map view, an editor, wizard or a dashboard.
 * <p/>
 * A panel is identified by its path and name. The path defines the place in the
 * hierarchy of panel.
 *
 * @see Context
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IPanel {

    /** See {@link IPanelSite#LAYOUT_SPACING_KEY}. */
    public static final String      LAYOUT_SPACING_KEY = IPanelSite.LAYOUT_SPACING_KEY;
    /** See {@link IPanelSite#LAYOUT_MARGINS_KEY}. */
    public static final String      LAYOUT_MARGINS_KEY = IPanelSite.LAYOUT_MARGINS_KEY;

    
    /**
     * Initializes the panel and checks if it is valid for the given site and
     * context.
     * <p/>
     * This method is *not* usually called when the panel is activated. The init
     * method might be called even if the panel is never activated and displayed
     * actually. Register for {@link PanelChangeEvent}s to get notified when the
     * panel is activated.
     * 
     * @param site
     * @param context
     * @return True if the panel is valid for the given site and context.
     */
    public boolean init( IPanelSite site, IAppContext context );

    public void dispose();

    public PanelIdentifier id();

    
    /**
     * Creates the UI elements of this panel.
     * <p/>
     * {@link ConstraintLayout} is set for the <code>panelBody</code> by default.
     * Margin width/height are set according the space available in the panel. This
     * can be changed as needed.
     * 
     * @param panelBody The parent of the UI elements to create.
     */
    public void createContents( Composite panelBody );

    public IPanelSite getSite();
}
