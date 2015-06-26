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
package org.polymap.rhei.batik.app;

import java.util.List;
import java.util.function.Predicate;

import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite.PanelStatus;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.Panels;

/**
 * There is one IAppManager per application. It manages the {@link IAppContext}, the
 * {@link IPanel} instances and their lifecycle and {@link PanelStatus}. The
 * IAppManager is the source of all {@link PanelChangeEvent}s that drives the entiry
 * UI and application.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IAppManager
        extends AutoCloseable {

    public void init();

    @Override
    public void close();
    
    public IAppContext getContext();

//    public IPanel openPanel( PanelIdentifier panelId );
//
////    public IPanel focusPanel( PanelIdentifier panelId );
//
//    public void hidePanel( PanelPath path );
//
//    public void closePanel( PanelPath panelPath );

    public IPanel getPanel( PanelPath panelPath );

    /**
     * All direct children of the given path.
     *
     * @see Panels
     * @param path
     */
    public List<IPanel> findPanels( Predicate<IPanel> filter );

}
