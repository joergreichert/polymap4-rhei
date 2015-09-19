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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Rectangle;

import org.polymap.rhei.batik.engine.cp.PercentScore;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutElement;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutSolution;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PriorityOnTopGoalTest {

    private static Log log = LogFactory.getLog( PriorityOnTopGoalTest.class );


    @Test
    public void stable() {
        LayoutSolution solution = new ConstraintLayout.LayoutSolution( new Rectangle( 0, 0, 100, 100), 0, 0, 0 );
        solution.columns.add( new TestLayoutColumn(
                new TestLayoutElement( "first", 0, 100, new PriorityConstraint( 10 ) ),
                new TestLayoutElement( "second", 100, 100, new PriorityConstraint( 0 ) ) ) );
        
        PriorityOnTopGoal goal = new PriorityOnTopGoal( 0 );
        assertEquals( new PercentScore( 100 ), goal.score( solution ) );
        
        LayoutSolution optimized = goal.optimize( solution );
        assertNull( optimized );
        assertEquals( 1, solution.columns.size() );
        assertEquals( "first" , ((TestLayoutElement)solution.columns.get( 0 ).get( 0 )).title );
        assertEquals( "second" , ((TestLayoutElement)solution.columns.get( 0 ).get( 1 )).title );
        assertEquals( new PercentScore( 100 ), goal.score( solution ) );
    }
    

    @Test
    public void simpleSwap() {
        LayoutSolution solution = new ConstraintLayout.LayoutSolution( new Rectangle( 0, 0, 100, 100), 0, 0, 0 );
        solution.columns.add( new TestLayoutColumn(
                new TestLayoutElement( "first", 0, 100, new PriorityConstraint( 0 ) ),
                new TestLayoutElement( "second", 100, 100, new PriorityConstraint( 10 ) ) ) );
        
        PriorityOnTopGoal goal = new PriorityOnTopGoal( 0 );
        assertEquals( new PercentScore( 0 ), goal.score( solution ) );

        LayoutSolution optimized = goal.optimize( solution );
        assertNotNull( optimized );
        assertEquals( 1, solution.columns.size() );
        assertEquals( "second" , ((TestLayoutElement)solution.columns.get( 0 ).get( 0 )).title );
        assertEquals( "first" , ((TestLayoutElement)solution.columns.get( 0 ).get( 1 )).title );
        assertEquals( new PercentScore( 100 ), goal.score( solution ) );
    }
    

    @Test
    public void twoColumnsStable() {
        LayoutSolution solution = new ConstraintLayout.LayoutSolution( new Rectangle( 0, 0, 100, 100), 0, 0, 0 );
        solution.columns.add( new TestLayoutColumn(
                new TestLayoutElement( "1-1", 0, 100, new PriorityConstraint( 10 ) ),
                new TestLayoutElement( "1-2", 100, 100, new PriorityConstraint( 0 ) ) ) );
        solution.columns.add( new TestLayoutColumn(
                new TestLayoutElement( "2-1", 0, 100, new PriorityConstraint( 10 ) ),
                new TestLayoutElement( "2-2", 100, 100, new PriorityConstraint( 0 ) ) ) );
        
        PriorityOnTopGoal goal = new PriorityOnTopGoal( 0 );
        assertEquals( new PercentScore( 100 ), goal.score( solution ) );

        LayoutSolution optimized = goal.optimize( solution );
        assertNull( optimized );
        assertEquals( 2, solution.columns.size() );
        assertEquals( "1-1" , ((TestLayoutElement)solution.columns.get( 0 ).get( 0 )).title );
        assertEquals( "1-2" , ((TestLayoutElement)solution.columns.get( 0 ).get( 1 )).title );
        assertEquals( new PercentScore( 100 ), goal.score( solution ) );
    }
    

    @Test
    public void twoColumnsSwap() {
        LayoutSolution solution = new ConstraintLayout.LayoutSolution( new Rectangle( 0, 0, 100, 100), 0, 0, 0 );
        solution.columns.add( new TestLayoutColumn(
                new TestLayoutElement( "1-1", 0, 100, new PriorityConstraint( 0 ) ),
                new TestLayoutElement( "1-2", 100, 100, new PriorityConstraint( 20 ) ) ) );
        solution.columns.add( new TestLayoutColumn(
                new TestLayoutElement( "2-1", 0, 100, new PriorityConstraint( 10 ) ),
                new TestLayoutElement( "2-2", 100, 100, new PriorityConstraint( 0 ) ) ) );
        
        PriorityOnTopGoal goal = new PriorityOnTopGoal( 0 );
        assertEquals( new PercentScore( 33 ), goal.score( solution ) );

        LayoutSolution optimized = goal.optimize( solution );
        assertNotNull( optimized );
        assertEquals( 2, solution.columns.size() );
        assertEquals( "2-1" , ((TestLayoutElement)solution.columns.get( 0 ).get( 0 )).title );
        assertEquals( "1-2" , ((TestLayoutElement)solution.columns.get( 0 ).get( 1 )).title );
        assertEquals( new PercentScore( 67 ), goal.score( solution ) );

        optimized = goal.optimize( solution );
        assertNotNull( optimized );
        assertEquals( 2, solution.columns.size() );
        assertEquals( "2-1" , ((TestLayoutElement)solution.columns.get( 0 ).get( 0 )).title );
        assertEquals( "1-1" , ((TestLayoutElement)solution.columns.get( 0 ).get( 1 )).title );
        assertEquals( new PercentScore( 67 ), goal.score( solution ) );

        optimized = goal.optimize( solution );
        assertNotNull( optimized );
        assertEquals( 2, solution.columns.size() );
        assertEquals( "1-2" , ((TestLayoutElement)solution.columns.get( 0 ).get( 0 )).title );
        assertEquals( "1-1" , ((TestLayoutElement)solution.columns.get( 0 ).get( 1 )).title );
        assertEquals( new PercentScore( 100 ), goal.score( solution ) );

        optimized = goal.optimize( solution );
        assertNull( optimized );
    }

    
    @Test
    public void sort() {
        LayoutSolution solution = new ConstraintLayout.LayoutSolution( new Rectangle( 0, 0, 100, 100), 0, 0, 0 );
        solution.columns.add( new TestLayoutColumn(
                new TestLayoutElement( "1-1", 0, 100, new PriorityConstraint( 0 ) ),
                new TestLayoutElement( "1-2", 100, 100, new PriorityConstraint( 10 ) ) ) );
        solution.columns.add( new TestLayoutColumn(
                new TestLayoutElement( "2-1", 0, 100, new PriorityConstraint( 0 ) ),
                new TestLayoutElement( "2-2", 110, 100, new PriorityConstraint( 10 ) ) ) );
        
        PriorityOnTopGoal goal = new PriorityOnTopGoal( 0 );
        List<LayoutElement> sorted = goal.sort( solution );

        assertEquals( 4, sorted.size() );
        assertEquals( "1-1" , ((TestLayoutElement)sorted.get( 0 )).title );
        assertEquals( "2-1" , ((TestLayoutElement)sorted.get( 1 )).title );
        assertEquals( "1-2" , ((TestLayoutElement)sorted.get( 2 )).title );
        assertEquals( "2-2" , ((TestLayoutElement)sorted.get( 3 )).title );
    }
    
}
