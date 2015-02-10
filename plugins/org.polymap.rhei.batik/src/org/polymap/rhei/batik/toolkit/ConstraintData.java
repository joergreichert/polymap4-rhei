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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.batik.cp.ISolver;


/**
 * Layout data to be used for child widgets of {@link ILayoutContainer}s such
 * as {@link IPanelSection}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ConstraintData {

    private static Log log = LogFactory.getLog( ConstraintData.class );
    
    protected int                   defaultWidth = -1, defaultHeight = -1;

    protected int                   currentWhint, currentHhint, currentWidth = -1, currentHeight = -1;
    
    protected Map<Class,LayoutConstraint> constraints = new HashMap( 8 );

    
    public ConstraintData( LayoutConstraint... constraints ) {
        add( constraints );
    }
    
    
    @SuppressWarnings("hiding")
    public ConstraintData add( LayoutConstraint... constraints ) {
        for (LayoutConstraint constraint : constraints) {
            LayoutConstraint prev = this.constraints.put( constraint.getClass(), constraint );
            if (prev != null) {
                throw new IllegalArgumentException( "Constraint of type already added: " +  constraint );
            }
        }
        return this;
    }
    
    
    public void fillSolver( ISolver solver ) {
        for (LayoutConstraint constraint : constraints.values()) {
            solver.addConstraint( constraint );
        }
    }

    
    public <T extends LayoutConstraint> T constraint( Class<T> type, T defaultValue ) {
        LayoutConstraint result = constraints.get( type );
        return (T)(result != null ? result : defaultValue);
    }
    
    
//    public Point computeSize( Control control, int wHint, int hHint, boolean flushCache ) {
//        if (flushCache) {
//            flushCache();
//        }
//        if (wHint == SWT.DEFAULT && hHint == SWT.DEFAULT) {
//            if (defaultWidth == -1 || defaultHeight == -1) {
//                Point size = control.computeSize( wHint, hHint, flushCache );
//                defaultWidth = size.x;
//                defaultHeight = size.y;
//            }
//            return new Point( defaultWidth, defaultHeight );
//        }
//        if (currentWidth == -1 || currentHeight == -1 || wHint != currentWhint || hHint != currentHhint) {
//            Point size = control.computeSize( wHint, hHint, flushCache );
//            currentWhint = wHint;
//            currentHhint = hHint;
//            currentWidth = size.x;
//            currentHeight = size.y;
//        }
//        return new Point( currentWidth, currentHeight );
//    }
//    
//    
//    protected void flushCache () {
//        defaultWidth = defaultHeight = -1;
//        currentWidth = currentHeight = -1;
//    }

}
