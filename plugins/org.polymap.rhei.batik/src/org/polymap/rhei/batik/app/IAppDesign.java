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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 * A design creates the layout of the main window and teh {@link IPanelToolkit}
 * to be used to create UI elments.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IAppDesign
        extends AutoCloseable {

    public static final String      CSS_PANELS = "atlas-panels";
    public static final String      CSS_ACTIONS = "atlas-actions";
    public static final String      CSS_HEADER = "atlas-header";
    public static final String      CSS_SHELL = "atlas-shell";

    public void init();

    @Override
    public void close();
    
    public IPanelToolkit getToolkit();
    
    public Shell createMainWindow( Display display );

    public void delayedRefresh();

}
