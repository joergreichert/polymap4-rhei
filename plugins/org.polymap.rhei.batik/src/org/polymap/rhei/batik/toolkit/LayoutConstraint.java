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

import org.polymap.rhei.batik.layout.cp.IConstraint;
import org.polymap.rhei.batik.layout.cp.PercentScore;
import org.polymap.rhei.batik.layout.cp.Prioritized;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutSolution;

/**
 * 
 */
public abstract class LayoutConstraint
        extends Prioritized
        implements IConstraint<LayoutSolution,PercentScore> {
    
    public LayoutConstraint( int priority ) {
        super( priority );
    }
    
}


