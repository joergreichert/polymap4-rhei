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
 * The panel is the main visual component of the Atlas UI. It typically hosts a
 * map view, an editor, wizard or a dashboard.
 * <p/>
 * A panel is identified by its path and name. The path defines the place in the
 * hierarchy of panel.
 * <p/>
 * {@link DefaultPanel} provides default implementations of basic methods.
 *
 * @see Scope
 * @see DefaultPanel
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IPanel {

    public void setSite( IPanelSite site, IAppContext context );
    
    /**
     * This method is called before {@link #init(IPanelSite, IAppContext)} in order
     * to check if the panel wants to be displayed as sibling in the given context.
     * All context properties are initialized when the methos is called.
     * <p/>
     * This method should be lightweight and should not initialize anything, except
     * for the title and icon which is shown in the navigation bar.
     * <p/>
     * Check for <code>site.getPath().size() == 1;</code> to see if this is the
     * first/root panel.
     *
     * @param site
     * @param context
     * @return True if the panels wants to be shown.
     */
    public boolean wantsToBeShown();
    
    
    /**
     * Initializes the panel. This method is called right before the panel is
     * activated for the first time.
     * <p/>
     * This method is called just once, register for {@link PanelChangeEvent}s to get
     * notified everytime the panel is activated.
     * 
     * @param site
     * @param context
     */
    public void init();

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
