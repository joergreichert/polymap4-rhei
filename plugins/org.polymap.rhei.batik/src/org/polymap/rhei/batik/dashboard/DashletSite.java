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
package org.polymap.rhei.batik.dashboard;

import java.util.List;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Defaults;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.PropertyChangeSupport;

import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.LayoutConstraint;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DashletSite
        extends Configurable {

    @Defaults
    @Concern(PropertyChangeSupport.class)
    public Config<String>                 title;

    @Mandatory
    @Defaults
    public Config<Boolean>                isBoxStyle;
    
    @Mandatory
    @Defaults
    public Config<List<LayoutConstraint>> constraints;

    @Defaults
    public Config<Boolean>                isExpandable;
    
    public abstract IPanelSite panelSite();
    
    public abstract IPanelToolkit toolkit();
    
}
