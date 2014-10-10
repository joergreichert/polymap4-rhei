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

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.batik.layout.cp.IOptimizationGoal;
import org.polymap.rhei.batik.layout.cp.IScore;
import org.polymap.rhei.batik.layout.cp.PercentScore;
import org.polymap.rhei.batik.layout.cp.Prioritized;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutColumn;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutElement;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutSolution;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class MinOverallHeightGoal
        extends Prioritized
        implements IOptimizationGoal<LayoutSolution> {

    private static Log log = LogFactory.getLog( MinOverallHeightGoal.class );

    private static final Random rand = new Random();

    
    public MinOverallHeightGoal( Comparable priority ) {
        super( priority );
    }

    
    @Override
    public LayoutSolution optimize( LayoutSolution solution ) {
        // first idea: split column
        if (solution.columns.size() == 1) {
            LayoutColumn newColumn = solution.columns.get( 0 ).clone();
            newColumn.clear();
            solution.columns.add( newColumn );
        }

        // find min/max height column
        LayoutColumn minColumn = null, maxColumn = null;
        for (LayoutColumn column : solution.columns) {
            assert column.isEmpty() || column.height > 0 : "Column height==0!";
            minColumn = minColumn == null || column.height < minColumn.height ? column : minColumn;
            maxColumn = maxColumn == null || column.height >= maxColumn.height ? column : maxColumn;
        }
        assert minColumn != maxColumn : "minColumn:" + minColumn.height + ", maxColumn:" + maxColumn.height;
        assert minColumn.height <= maxColumn.height : "minColumn:" + minColumn.height + ", maxColumn:" + maxColumn.height;

        // biggest column has just 1 elm -> nothing to optimize
        if (maxColumn.size() <= 1) {
            return null;
        }
        // columns have same height
        if (maxColumn.height == minColumn.height) {
            return null;
        }

        int maxColumnIndex = rand.nextInt( maxColumn.size() );
        LayoutElement elm = maxColumn.remove( maxColumnIndex );
        int minColumnIndex = Math.min( maxColumnIndex, Math.max( minColumn.size()-1, 0 ) );
        minColumn.add( minColumnIndex, elm );

        solution.justifyElements();
        return solution;
    }

    
    @Override
    public IScore score( LayoutSolution solution ) {
        int maxColumnHeight = 0, minColumnHeight = Integer.MAX_VALUE;
        int columnWidth = solution.defaultColumnWidth();

        for (LayoutColumn column : solution.columns) {
            // avoid empty column produced by the random optimization
            if (column.size() == 0) {
                log.debug( "        score=-1 - " + solution );
                return IScore.INVALID;
            }
            // avoid columns to small for columnWidth
            int preferredWidth = column.computeMinWidth( columnWidth );
            if (preferredWidth > columnWidth) {
                log.debug( "        score=" + preferredWidth + ">" + columnWidth + " - " + solution );
                return IScore.INVALID;
            }
            // avoid columns to wide for MaxWidthConstraint
            int columnMaxWidth = Integer.MAX_VALUE;
            for (LayoutElement elm : column) {
                MaxWidthConstraint maxWidth = elm.constraint( MaxWidthConstraint.class, new MaxWidthConstraint( Integer.MAX_VALUE, -1 ) );
                columnMaxWidth = Math.min( maxWidth.getValue(), columnMaxWidth );
            }
            if (columnWidth > columnMaxWidth /*&& solution.columns.size() > 1*/) {
                return IScore.INVALID;                
            }

            assert column.height >= 0;
            maxColumnHeight = Math.max( column.height, maxColumnHeight );
            minColumnHeight = Math.min( column.height, minColumnHeight );
        }

        //            // result: ratio = max/min
        //            double ratio = (100d / maxColumnHeight * minColumnHeight);
        //            PercentScore result = new PercentScore( (int)ratio );

        // might be > 100
        int maxClientHeight = 0;
        for (LayoutElement elm : solution.elements()) {
            maxClientHeight += elm.height;
        }
        
        int heightPercent = (int)(100d / maxClientHeight * maxColumnHeight);
        PercentScore result = new PercentScore( 100 - Math.min( 100, heightPercent ) );
        assert result.getValue() >= 0 && result.getValue() <= 100;
        return result;
    }

}