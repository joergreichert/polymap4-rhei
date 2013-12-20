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

import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Timer;

/**
 * 
 * <p>
 * This optimizer does find also invalid solutions that fail on one or more given
 * constraints.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BestFirstOptimizer
        extends DefaultSolver {

    private static Log log = LogFactory.getLog( BestFirstOptimizer.class );

    private int                     timeoutMillis;

    /** The surrogates of the seen solutions so far. */
    private Set<String>             seen = new HashSet();
    
    private Queue<ExScoredSolution> queue;

    private Queue<ExScoredSolution> terminals;

    private int                     loops;
    
    
    /**
     * 
     * @param timeoutMillis Max time in milliseconds to spent in optimizer. -1
     *        specifies no limit.
     * @param maxQueueSize The max number of elements hold in the queue of solutions.
     *        -1 specifies that queues should be unbound.
     */
    public BestFirstOptimizer( int timeoutMillis, int maxQueueSize ) {
        this.timeoutMillis = timeoutMillis;
        this.queue = SolutionQueueBuilder.create( maxQueueSize );
        this.terminals = SolutionQueueBuilder.create( maxQueueSize );
    }

    /**
     * Provides runtime information after {@link #solve(ISolution)} has been run.
     * @return The number of 'seen' solutions.
     */
    public Set<String> getSeen() {
        return seen;
    }
    
    /**
     * Provides runtime information after {@link #solve(ISolution)} has been run.
     * @return The remaining queue.
     */
    public Queue<? extends ScoredSolution> getQueue() {
        return queue;
    }
    
    /**
     * Provides runtime information after {@link #solve(ISolution)} has been run.
     * @return The found solutions that cannot optimized any further.
     */
    public Queue<? extends ScoredSolution> getTerminals() {
        return terminals;
    }
    
    /**
     * Provides runtime information after {@link #solve(ISolution)} has been run.
     * @return The number of optimization steps.
     */
    public int getLoops() {
        return loops;
    }

    @Override
    public ScoredSolution solve( ISolution start ) {
        //assert goals().size() > 0;
        
        Timer timer = new Timer();
        List<IOptimizationGoal> goals = Prioritized.sort( goals() );
//        List<IConstraint> constraints = constraints();

        // start solution
        queue.add( new ExScoredSolution( start, PercentScore.NULL ) );
        
        //
        loops = 0; 
        while (!queue.isEmpty() && 
                (timeoutMillis < 0 || timer.elapsedTime() < timeoutMillis)) {
            loops ++;
            // current best solution
            ExScoredSolution best = queue.peek();
            
            // find next optimization step
            ISolution optimized = null;
            String surrogate = null;
            while (optimized == null && best.goalsIndex < goals.size()) {
                IOptimizationGoal goal = goals.get( best.goalsIndex );

                optimized = goal.optimize( best.solution.clone() );
                surrogate = optimized != null ? optimized.surrogate() : null;
                if (optimized != null && seen.contains( surrogate )) {
                    optimized = null;
                }
                best.goalsIndex ++;
            }
            // no optimization found -> terminal
            if (optimized == null) {
                queue.remove();
                terminals.add( best );
            }
            // score optimized solution -> queue
            else {
                IScore optimizedScore = null;
                for (IOptimizationGoal goal : goals) {
                    IScore s = goal.score( optimized );
                    if (s == IScore.INVALID) {
                        optimizedScore = IScore.INVALID;
                        break;
                    }
                    else {
                        optimizedScore = optimizedScore != null ? optimizedScore.add( s ) : s;
                    }
                }
//                for (IConstraint constraint : constraints) {
//                    IScore s = constraint.score( optimized );
//                    optimizedScore = optimizedScore.add( s );
//                }
                if (optimizedScore != IScore.INVALID) {
                    queue.add( new ExScoredSolution( optimized, optimizedScore ) );
                }
                seen.add( surrogate );
            }            
        }
        
        ExScoredSolution result = terminals.isEmpty() ? queue.peek() : terminals.peek();
        if (timer.elapsedTime() > 10) {
            log.info( "queue=" + queue.size() + ", terminals=" + terminals.size() 
                    + ", maxScore=" + result.score
                    + ", loops=" + loops + ", seen=" + seen.size()
                    + " (" + timer.elapsedTime() + "ms)" );
        }
        return result;
    }


    /**
     * 
     */
    class ExScoredSolution
            extends ScoredSolution {
        
        public int      goalsIndex = 0;
    
        public ExScoredSolution( ISolution solution, IScore score ) {
            super( solution, score );
        }
    }
    
}
