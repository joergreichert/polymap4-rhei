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
package org.polymap.rhei.batik.engine.panelops;

import java.util.List;
import java.util.function.Predicate;

import org.polymap.core.runtime.config.Configurable;

import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.IPanelSite.PanelStatus;
import org.polymap.rhei.batik.PanelPath;
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
public abstract class PanelOp<R>
        extends Configurable {

    public abstract R execute( IPanelOpSite site );
    
    
    /**
     * This interface allows the operation to manipulate the panels of the
     * {@link IAppManager} it is working for.
     */
    public interface IPanelOpSite {
        
        public <V> V runOp( PanelOp op );
        
        public IPanel getPanel( PanelPath panelPath );
        
//        public void addPanel( PanelPath path, IPanel panel );
//        
//        public void removePanel( PanelPath path );

        public List<IPanel> findPanels( Predicate<IPanel> filter );

        public IPanelSite getOrCreatePanelSite( PanelPath path, int stackPriority );

        public void updatePanelStatus( IPanel panel, PanelStatus panelStatus );

    }
    
}
