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

import java.util.ArrayList;
import java.util.List;

import org.polymap.rhei.batik.toolkit.LayoutConstraint;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultDashlet
        implements IDashlet {

    protected DashletSite           dashletSite;
    
    private List<LayoutConstraint>  constraints = new ArrayList();

    
    @Override
    public void init( DashletSite site ) {
        this.dashletSite = site;
        constraints.forEach( c -> site.constraints.get().add( c ) );
    }

    
    @Override
    public void dispose() {
    }


    protected DashletSite getSite() {
        return dashletSite;
    }
    
    
    public DefaultDashlet addConstraint( LayoutConstraint constraint ) {
        constraints.add( constraint );
        if (dashletSite != null) {
            dashletSite.constraints.get().add( constraint );
        }
        return this;
    }
}
