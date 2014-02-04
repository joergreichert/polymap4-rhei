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

import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.Ordering;

import org.polymap.rhei.batik.layout.cp.ISolver.ScoredSolution;

/**
 * Factory for priority queues. Creates bound or unbound queues that have elements
 * ordered in reverse natural order.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SolutionQueueBuilder {

    private static Log log = LogFactory.getLog( SolutionQueueBuilder.class );
    
    
    /**
     *
     * @param maxSize -1 signals that an unbound queue should be created. 
     * @return Newly created solution queue.
     */
    public static <T extends ScoredSolution> Queue<T> create( int maxSize ) {
        if (maxSize < 0) {
            return new PriorityQueue<T>( 1024, Ordering.natural().reverse() );
        }
        else {
            return MinMaxPriorityQueue
                    .orderedBy( Ordering.natural().reverse() )
                    .maximumSize( maxSize ).create();
        }        
    }
    
    
    /**
     * Unbound priority queue. Allows multiple elements with same score. 
     */
    static class ListBoundSolutionQueue<T extends ScoredSolution>
            extends LinkedList<T> {
    
        private int         maxSize;
        
        public ListBoundSolutionQueue( int maxSize ) {
            this.maxSize = maxSize;
        }
    
        @Override
        public boolean add( T elm ) {
            int index = Collections.binarySearch( this, elm );
            if (index >= 0) {
                super.add( index, elm );
            } else {
                super.add( ~index, elm );                
            }
            if (size() == maxSize) {
                poll();
            }
            return true;
        }
    }

}
