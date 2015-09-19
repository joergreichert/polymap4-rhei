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

import org.polymap.rhei.batik.engine.cp.PercentScore;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutSolution;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MinOverallHeightGoalTest {

    private static Log log = LogFactory.getLog( MinOverallHeightGoalTest.class );


    @Test
    public void twoElements() {
        LayoutSolution solution = new ConstraintLayout.LayoutSolution( new Rectangle( 0, 0, 100, 100), 0, 0, 0 );
        solution.columns.add( new TestLayoutColumn(
                new TestLayoutElement( "first", 0, 100, new PriorityConstraint( 10 ) ),
                new TestLayoutElement( "second", 100, 100, new PriorityConstraint( 0 ) ) ) );
        solution.justifyElements();
        
        MinOverallHeightGoal goal = new MinOverallHeightGoal( 1 );
        assertEquals( new PercentScore( 0 ), goal.score( solution ) );
        
        LayoutSolution optimized = goal.optimize( solution );
        assertNotNull( optimized );
//        assertEquals( 1, solution.columns.size() );
//        assertEquals( "first" , ((TestLayoutElement)solution.columns.get( 0 ).get( 0 )).title );
//        assertEquals( "second" , ((TestLayoutElement)solution.columns.get( 0 ).get( 1 )).title );
        assertEquals( new PercentScore( 50 ), goal.score( solution ) );

        optimized = goal.optimize( solution );
        assertNull( optimized );
    }


    @Test
    public void threeElements() {
        LayoutSolution solution = new ConstraintLayout.LayoutSolution( new Rectangle( 0, 0, 100, 100), 0, 0, 0 );
        solution.columns.add( new TestLayoutColumn(
                new TestLayoutElement( "10", 0, 10, new PriorityConstraint( 10 ) ),
                new TestLayoutElement( "20", 10, 20, new PriorityConstraint( 0 ) ),
                new TestLayoutElement( "30", 30, 30, new PriorityConstraint( 0 ) ) ) );
        solution.justifyElements();
        
        MinOverallHeightGoal goal = new MinOverallHeightGoal( 1 );
        int i = 0;
        System.out.println( "Solution: " + goal.score( solution ) + " - " + solution );
        for (; solution != null && i<10; i++) {
            solution = goal.optimize( solution );
            System.out.println( "        : " + (solution != null ? goal.score( solution ) : "fertig") + " - " + solution );
        }
        assertTrue( "Expected i<10: " + i, i<10 );
    }

}
