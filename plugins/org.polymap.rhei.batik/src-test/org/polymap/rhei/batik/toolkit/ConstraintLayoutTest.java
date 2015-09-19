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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Rectangle;

import org.polymap.core.runtime.Timer;

import org.polymap.rhei.batik.engine.cp.BestFirstOptimizer;
import org.polymap.rhei.batik.engine.cp.ISolver.ScoredSolution;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutSolution;
import org.polymap.rhei.batik.toolkit.NeighborhoodConstraint.Neighborhood;

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

    
    @Test
    public void benchmark1() {
        LayoutSolution start = new ConstraintLayout.LayoutSolution( new Rectangle( 0, 0, 200, 400), 0, 0, 0 );
        TestLayoutElement elm;
        start.columns.add( new TestLayoutColumn(
                new TestLayoutElement( "prio-0",  0, 10, prio( 0 ), minWidth( 100 ) ),
          elm = new TestLayoutElement( "pri0-10", 0, 20, prio( 10 ), minWidth( 100 ) ),
                new TestLayoutElement( "prio-0",  0, 30, prio( 0 ) ),
                new TestLayoutElement( "prio-5",  0, 40, prio( 5 ), neighbor( elm, Neighborhood.BOTTOM ) ),
                new TestLayoutElement( "prio-10", 0, 50, prio( 10 ) ),
                new TestLayoutElement( "prio-11", 0, 60, prio( 11 ) ),
                new TestLayoutElement( "prio-12", 0, 70, prio( 12 ) ),
                new TestLayoutElement( "prio-10", 0, 80, prio( 10 ) ) ) );
        start.justifyElements();
        
        // unbound queue configuration from ConstraintLayout
        BestFirstOptimizer solver = new BestFirstOptimizer( -1, -1 );
        solver.addGoal( new PriorityOnTopGoal( 0 ) );
        solver.addGoal( new MinOverallHeightGoal( 0 ) );
        solver.addGoal( new NeighborhoodGoal( 0 ) );

        Timer timer = new Timer();
        ScoredSolution solution = solver.solve( start );
//        log.info( "queue=" + solver.getQueue().size() + ", terminals=" + solver.getTerminals().size() 
//                + ", maxScore=" + solution.score
//                + ", loops=" + solver.getLoops() + ", seen=" + solver.getSeen().size()
//                + " (" + timer.elapsedTime() + "ms)" );
        log.info( "Solutions: " + solution.solution );
        log.info( "(unbound) loops/sec : " + (int)(1000d * solver.getLoops() / timer.elapsedTime()) );

        // bound queue consiguration
        solver = new BestFirstOptimizer( -1, 1000 );
        solver.addGoal( new PriorityOnTopGoal( 0 ) );
        solver.addGoal( new MinOverallHeightGoal( 0 ) );
        solver.addGoal( new NeighborhoodGoal( 0 ) );

        timer.start();
        solution = solver.solve( start );
//        log.info( "queue=" + solver.getQueue().size() + ", terminals=" + solver.getTerminals().size() 
//                + ", maxScore=" + solution.score
//                + ", loops=" + solver.getLoops() + ", seen=" + solver.getSeen().size()
//                + " (" + timer.elapsedTime() + "ms)" );
        log.info( "Solutions: " + solution.solution );
        log.info( "(bound) loops/sec : " + (int)(1000d * solver.getLoops() / timer.elapsedTime()) );
    }

    
    protected PriorityConstraint prio( int prio ) {
        return new PriorityConstraint( prio );
    }
    
    protected MinWidthConstraint minWidth( int value ) {
        return new MinWidthConstraint( value, 1 );
    }
    
    protected NeighborhoodConstraint neighbor( Object control, Neighborhood type ) {
        return new NeighborhoodConstraint( control, type, 1 );
    }
}
