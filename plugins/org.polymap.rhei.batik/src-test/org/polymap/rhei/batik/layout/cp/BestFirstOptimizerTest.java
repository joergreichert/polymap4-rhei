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
package org.polymap.rhei.batik.layout.cp;

import static org.junit.Assert.*;

import java.util.Queue;

import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.batik.engine.cp.IScore;
import org.polymap.rhei.batik.engine.cp.ISolution;
import org.polymap.rhei.batik.engine.cp.ISolver;
import org.polymap.rhei.batik.engine.cp.PercentScore;
import org.polymap.rhei.batik.engine.cp.SolutionQueueBuilder;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BestFirstOptimizerTest {

    private static Log log = LogFactory.getLog( BestFirstOptimizerTest.class );


    @Test
    public void unboundSolutionQueue() {
        Queue<TestScoredSolution> queue = SolutionQueueBuilder.create( -1 );
        queue.add( new TestScoredSolution( PercentScore.NULL ) );
        queue.add( new TestScoredSolution( new PercentScore( 10 ) ) );
        
        assertEquals( 2, queue.size() );
//        assertEquals( PercentScore.NULL, queue.getFirst().score );
        assertEquals( new PercentScore( 10 ), queue.peek().score );

        queue.add( new TestScoredSolution( new PercentScore( 5 ) ) );
        assertEquals( 3, queue.size() );
        assertEquals( new PercentScore( 10 ), queue.peek().score );

        queue.add( new TestScoredSolution( new PercentScore( 5 ) ) );
//        assertEquals( 3, queue.size() );
//        assertEquals( new PercentScore( 5 ), queue.getFirst().score );
        assertEquals( new PercentScore( 10 ), queue.peek().score );

        queue.add( new TestScoredSolution( new PercentScore( 20 ) ) );
//        assertEquals( 3, queue.size() );
//        assertEquals( new PercentScore( 5 ), queue.getFirst().score );
        assertEquals( new PercentScore( 20 ), queue.peek().score );
    }

    
    @Test
    public void boundSolutionQueue() {
        Queue<TestScoredSolution> queue = SolutionQueueBuilder.create( 3 );
        queue.add( new TestScoredSolution( PercentScore.NULL ) );
        queue.add( new TestScoredSolution( new PercentScore( 10 ) ) );
        
        assertEquals( 2, queue.size() );
//        assertEquals( PercentScore.NULL, queue.getFirst().score );
        assertEquals( new PercentScore( 10 ), queue.peek().score );

        queue.add( new TestScoredSolution( new PercentScore( 5 ) ) );
        assertEquals( 3, queue.size() );
        assertEquals( new PercentScore( 10 ), queue.peek().score );

        queue.add( new TestScoredSolution( new PercentScore( 5 ) ) );
        assertEquals( 3, queue.size() );
//        assertEquals( new PercentScore( 5 ), queue.getFirst().score );
        assertEquals( new PercentScore( 10 ), queue.peek().score );

        queue.add( new TestScoredSolution( new PercentScore( 20 ) ) );
        assertEquals( 3, queue.size() );
//        assertEquals( new PercentScore( 5 ), queue.getFirst().score );
        assertEquals( new PercentScore( 20 ), queue.peek().score );
    }

    
    @Test
    public void percentScore() {
        PercentScore score = PercentScore.NULL;
        assertEquals( 0, score.getValue() );
        
        score = score.add( PercentScore.NULL );
        assertSame( PercentScore.NULL, score );
        assertEquals( 0, score.getValue() );

        score = score.add( new PercentScore( 50 ) );
        assertEquals( 50, score.getValue() );

        score = score.add( new PercentScore( 50 ) );
        assertEquals( 50, score.getValue() );

        score = score.add( new PercentScore( 100 ) );
        assertEquals( 75, score.getValue() );
    }
    
    
    /**
     * 
     */
    class TestScoredSolution
            extends ISolver.ScoredSolution {

        public TestScoredSolution( IScore score ) {
            super( new TestSolution(), score );
        }

    }

    
    /**
     * 
     */
    class TestSolution
            implements ISolution {

        public TestSolution clone() {
            try {
                return (TestSolution)super.clone();
            }
            catch (CloneNotSupportedException e) {
                throw new RuntimeException( e );
            }
        }
        
        @Override
        public String surrogate() {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }
        
    }
    
}
