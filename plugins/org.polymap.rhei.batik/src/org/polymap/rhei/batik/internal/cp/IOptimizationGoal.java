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
package org.polymap.rhei.batik.internal.cp;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IOptimizationGoal<S extends ISolution, SC extends IScore> {

    /**
     * Try to optimze the given solution. The given instance is changed in-place.
     *
     * @param solution The solution to optimze.
     * @return The optimized solution, or null if this goal does not find a better solution.
     */
    public abstract S optimize( S solution );
    
    public abstract SC score( S solution );

    
    /**
     * 
     */
    public interface Step {
    
        public abstract void revoke();
        
    }
    
}
