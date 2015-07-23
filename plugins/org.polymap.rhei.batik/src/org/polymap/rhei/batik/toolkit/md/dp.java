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
package org.polymap.rhei.batik.toolkit.md;

/**
 * "Density independence" refers to the uniform display of UI elements on screens
 * with different densities.
 * <p/>
 * Density-independent pixels (pronounced “dips”) are flexible units that scale to
 * uniform dimensions on any screen. When developing an Android application, use dp
 * to display elements uniformly on screens with different densities.
 * 
 * @see <a
 *      href="http://www.google.com/design/spec/layout/units-measurements.html">Units
 *      and measurements</a>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class dp {

    public static dp dp( int dp ) {
        return new dp( dp );
    }
    
    // instance *******************************************
    
    public int         dp;

    public dp( int dp ) {
        this.dp = dp;
    }

    public dp add( int val ) {
        this.dp += val;
        return this;
    }
    
    public dp sub( int val ) {
        this.dp -= val;
        return this;
    }
    
    /**
     * Calculates pixels for the actual display.
     *
     * @see MdAppDesign#dp(int)
     */
    public int pix() {
        return MdAppDesign.dp( dp );
    }
    
}
