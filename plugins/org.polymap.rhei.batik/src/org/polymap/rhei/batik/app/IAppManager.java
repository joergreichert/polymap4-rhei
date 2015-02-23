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

import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;

/**
 * ...
 * <p/>
 * The {@link IAppManager} is the source of all {@link PanelChangeEvent}s that drives
 * the entiry UI and application.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IAppManager
        extends AutoCloseable {

    public void init();

    @Override
    public void close();
    
    public IAppContext getContext();

    public void activatePanel( PanelIdentifier id );

    public IPanel getActivePanel();

    public void closePanel( PanelPath panelPath );

    public IPanel openPanel( PanelIdentifier panelId );

}
