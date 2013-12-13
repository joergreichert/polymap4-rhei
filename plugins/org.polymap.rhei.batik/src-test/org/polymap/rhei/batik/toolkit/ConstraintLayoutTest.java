/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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

import static org.junit.Assert.*;

import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Rectangle;

import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutSolution;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ConstraintLayoutTest {

    private static Log log = LogFactory.getLog( ConstraintLayoutTest.class );


    @Test
    public void cloneSolution() {
        LayoutSolution solution = new LayoutSolution( new Rectangle( 0, 0, 100, 100 ), 1, 1, 1 );
        solution.columns.add( new TestLayoutColumn(
                new TestLayoutElement( "first", 0, 100, new PriorityConstraint( 10 ) ),
                new TestLayoutElement( "second", 100, 100, new PriorityConstraint( 0 ) ) ) );

        solution.justifyElements();
        LayoutSolution copy = solution.clone();
        
        assertNotSame( solution, copy );
        assertNotSame( solution.columns, copy.columns );
        assertNotSame( solution.columns.get( 0 ), copy.columns.get( 0 ) );
        assertEquals( solution.columns.get( 0 ).height, copy.columns.get( 0 ).height );
        assertEquals( solution.columns.get( 0 ).width, copy.columns.get( 0 ).width );
        assertEquals( solution.marginWidth, copy.marginWidth );
        assertEquals( solution.marginHeight, copy.marginHeight );
        assertEquals( solution.spacing, copy.spacing );
        assertSame( solution.clientArea, copy.clientArea );
        assertEquals( solution.surrogate(), copy.surrogate() );
    }
    
}
