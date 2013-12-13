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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.batik.internal.cp.IOptimizationGoal;
import org.polymap.rhei.batik.internal.cp.PercentScore;
import org.polymap.rhei.batik.internal.cp.Prioritized;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutColumn;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutElement;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutSolution;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class PriorityOnTopGoal
        extends Prioritized
        implements IOptimizationGoal<LayoutSolution,PercentScore> {

    private static Log log = LogFactory.getLog( PriorityOnTopGoal.class );

//    /**
//     * Sort ascending y position of the elements.
//     */
//    public static final Ordering<LayoutElement> yOrdering = new Ordering<LayoutElement>() {
//        public int compare( LayoutElement left, LayoutElement right ) {
//            assert left.y >= 0 && right.y >= 0;
//            return left.y - right.y;
//        }
//    };

    
    public PriorityOnTopGoal( int priority ) {
        super( priority );
    }

    
    protected List<LayoutElement> sort( LayoutSolution solution ) {
        Map<Integer,LayoutElement> result = new TreeMap();
        int columnOffset = 0;
        for (LayoutColumn column : solution.columns) {
            for (LayoutElement elm : column) {
                assert elm.y >= 0;
                if (result.put( elm.y + columnOffset, elm ) != null) {
                    throw new RuntimeException( "" );
                }
            }
            columnOffset ++;
        }
        return new ArrayList( result.values() );
    }
    
    
    @Override
    public LayoutSolution optimize( LayoutSolution solution ) {
        List<LayoutElement> sortedElms = sort( solution );
        
        if (sortedElms.size() <= 1) {
            return null;
        }
        
        // find two elements where priorities are not in order
        LayoutElement elm1 = null, elm2 = null;
        for (int i=1; i<sortedElms.size(); i++) {
            LayoutElement candidate1 = sortedElms.get( i );
            LayoutElement candidate2 = sortedElms.get( i-1 );
            
            // is y actually bigger (not just equal)?
//            if (candidate1.y > candidate2.y) {
                PriorityConstraint prio1 = candidate1.constraint( PriorityConstraint.class, new PriorityConstraint( 0 ) );
                PriorityConstraint prio2 = candidate2.constraint( PriorityConstraint.class, new PriorityConstraint( 0 ) );

                if (prio1.getValue() > prio2.getValue()) {
                    elm1 = candidate1;
                    elm2 = candidate2;
                    break;
                }
//            }
        }
        
        if (elm1 == null || elm2 == null) {
            return null;
        }
        
        // find column/index of offending elements
        LayoutColumn col1 = null, col2 = null;
        int index1 = -1, index2 = -1;
        for (LayoutColumn column : solution.columns) {
            int index = 0;
            for (LayoutElement elm : column) {
                if (elm == elm1) {
                    col1 = column;
                    index1 = index;
                }
                else if (elm == elm2) {
                    col2 = column;
                    index2 = index;
                }
                index ++;
            }
        }
        
        // swap elements
        col1.set( index1, elm2 );
        col2.set( index2, elm1 );
        
        solution.justifyElements();
        return solution;
    }

    
    @Override
    public PercentScore score( LayoutSolution solution ) {
        List<LayoutElement> sortedElms = sort( solution );
        
        if (sortedElms.isEmpty()) {
            return PercentScore.NULL;
        }
        if (sortedElms.size() == 1) {
            return new PercentScore( 100 );
        }
        double elmPercent = 100d / (sortedElms.size()-1);
        double result = 0;

        LayoutElement prev = null;
        for (LayoutElement elm : sortedElms) {
            if (prev != null) {
                PriorityConstraint prevPrio = prev.constraint( PriorityConstraint.class, new PriorityConstraint( 0 ) );
                PriorityConstraint elmPrio = elm.constraint( PriorityConstraint.class, new PriorityConstraint( 0 ) );

                if (prevPrio.getValue() >= elmPrio.getValue()) {
                    result += elmPercent;
                }
            }
            prev = elm;
        }
        return new PercentScore( (int)Math.round( result ) );
    }
    
}