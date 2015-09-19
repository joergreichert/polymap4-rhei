/* 
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.batik.toolkit;

import org.polymap.rhei.batik.engine.cp.PercentScore;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutSolution;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NeighborhoodConstraint
        extends LayoutConstraint {
    
    public enum Neighborhood {
        TOP, BOTTOM;
    }
    
    private Object       control;
    
    private Neighborhood    type;


    public NeighborhoodConstraint( Object control, Neighborhood type, int prio ) {
        super( prio );
        this.control = control;
        this.type = type;
    }

    public Object getControl() {
        return control;
    }
    
    public Neighborhood getType() {
        return type;
    }

    @Override
    public PercentScore score( LayoutSolution solution ) {
        // this is soft constraint; it is enforced by the optimizer goal
        return PercentScore.NULL;
    }
    
}