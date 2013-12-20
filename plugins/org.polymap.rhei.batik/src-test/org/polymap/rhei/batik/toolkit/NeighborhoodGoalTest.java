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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Rectangle;

import org.polymap.rhei.batik.internal.cp.PercentScore;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutSolution;
import org.polymap.rhei.batik.toolkit.NeighborhoodConstraint.Neighborhood;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NeighborhoodGoalTest {

    private static Log log = LogFactory.getLog( NeighborhoodGoalTest.class );


    //@Test
    public void top() {
        LayoutSolution solution = new ConstraintLayout.LayoutSolution( new Rectangle( 0, 0, 100, 100), 0, 0, 0 );
        solution.columns.add( new TestLayoutColumn(
                new TestLayoutElement( "first", 0, 100, new NeighborhoodConstraint( "second", Neighborhood.TOP, 1 ) ),
                new TestLayoutElement( "second", 100, 100 ) ) );
        solution.justifyElements();
        
        NeighborhoodGoal goal = new NeighborhoodGoal( 1 );
        assertEquals( new PercentScore( 100 ), goal.score( solution ) );
    }


    //@Test
    public void bottom() {
        LayoutSolution solution = new ConstraintLayout.LayoutSolution( new Rectangle( 0, 0, 100, 100), 0, 0, 0 );
        solution.columns.add( new TestLayoutColumn(
                new TestLayoutElement( "first", 0, 100 ),
                new TestLayoutElement( "second", 100, 100, new NeighborhoodConstraint( "first", Neighborhood.BOTTOM, 1 ) ) ) );
        solution.justifyElements();
        
        NeighborhoodGoal goal = new NeighborhoodGoal( 1 );
        assertEquals( new PercentScore( 100 ), goal.score( solution ) );
    }

}
