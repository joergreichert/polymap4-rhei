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
package org.polymap.rhei.batik.engine;

import org.polymap.core.runtime.config.Configurable;

import org.polymap.rhei.batik.IPanelSite.PanelStatus;
import org.polymap.rhei.batik.app.IAppManager;

/**
 * Provides a particular operation that modifies the stack of panels and/or the
 * {@link PanelStatus} of the panels of an {@link IAppManager}.
 * <p/>
 * Separate operations are easy to re/combine to different algorithms. So, instead of
 * poluting the AppManager instances with a lot of methods to change the panels every
 * operations is implemented in one class.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class PanelOp
        extends Configurable {

    protected DefaultAppManager     manager;
    
    public abstract void execute();
    
}
