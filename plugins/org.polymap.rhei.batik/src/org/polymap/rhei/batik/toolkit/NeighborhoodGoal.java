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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.batik.engine.cp.IOptimizationGoal;
import org.polymap.rhei.batik.engine.cp.PercentScore;
import org.polymap.rhei.batik.engine.cp.Prioritized;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutColumn;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutElement;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutSolution;
import org.polymap.rhei.batik.toolkit.NeighborhoodConstraint.Neighborhood;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class NeighborhoodGoal
        extends Prioritized
        implements IOptimizationGoal<LayoutSolution> {

    private static Log log = LogFactory.getLog( NeighborhoodGoal.class );

    
    public NeighborhoodGoal( int priority ) {
        super( priority );
    }

    
    @Override
    public LayoutSolution optimize( LayoutSolution solution ) {
        // no idea; just score given solutions
        return null;
    }

    
    @Override
    public PercentScore score( LayoutSolution solution ) {
        int total = 0;
        int satisfied = 0;

        for (LayoutColumn column : solution.columns) {
            for (int i=0; i<column.size(); i++) {
                
                LayoutElement elm = column.get( i );
                NeighborhoodConstraint constraint = elm.constraint( NeighborhoodConstraint.class, null );
                
                if (constraint != null) {
                    total ++;
                    if (constraint.getType() == Neighborhood.TOP) {
                        if (i+1 < column.size()) {
                            LayoutElement next = column.get( i+1 );
                            satisfied += next.control == constraint.getControl() ? 1 : 0;
                        }
                    }
                    else if (constraint.getType() == Neighborhood.BOTTOM) {
                        if (i >= 1) {
                            LayoutElement next = column.get( i-1 );
                            satisfied += next.control == constraint.getControl() ? 1 : 0;
                        }
                    }
                    else {
                        throw new RuntimeException( "Unhandled neighborhood type:" + constraint.getType() );
                    }
                }
            }
        }
        return total > 0 ? new PercentScore( 100 / total * satisfied) : PercentScore.NULL;
    }
    
}