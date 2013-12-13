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
package org.polymap.rhei.batik.toolkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

import org.polymap.rhei.batik.internal.cp.BestFirstOptimizer;
import org.polymap.rhei.batik.internal.cp.ISolution;
import org.polymap.rhei.batik.internal.cp.ISolver;
import org.polymap.rhei.batik.internal.cp.ISolver.ScoredSolution;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ConstraintLayout
        extends Layout {

    private static Log log = LogFactory.getLog( ConstraintLayout.class );

    public int                  marginWidth = 10;

    public int                  marginHeight = 10;
    
    public int                  spacing = 10;
    
    private LayoutSolution      solution;
    
    private Rectangle           clientArea;

    
    @Override
    protected void layout( Composite composite, boolean flushCache ) {
        if (computeSolution( composite, flushCache )) {
            // layout elements
            int colX = marginWidth;
            for (LayoutColumn column : solution.columns) {
                assert column.width > 0;
                int elmY = marginHeight;

                for (LayoutElement elm : column) {
                    assert elm.height >= 0;
                    elm.control.setBounds( colX, elmY, column.width, elm.height );
                    elmY += elm.height + spacing;
                }
                colX += column.width + spacing;
            }
        }
    }

    
    @Override
    protected Point computeSize( Composite composite, int wHint, int hHint, boolean flushCache ) {
        Point result = null;

        if (computeSolution( composite, flushCache )) {
            // find heighest column
            LayoutColumn maxColumn = null;
            for (LayoutColumn column : solution.columns) {
                maxColumn = maxColumn == null || column.height >= maxColumn.height ? column : maxColumn;
            }
            int height = maxColumn.height + (2*marginHeight) + ((maxColumn.size()-1)*spacing);
            result = new Point( SWT.DEFAULT, height );
            //log.info( "    computeSize: " + result );
        }
        else {
            result = new Point( wHint, hHint );
        }
        return result;
    }

    
    private boolean computeSolution( Composite composite, boolean flushCache ) {
        assert solution == null || solution.composite == composite;

        if (solution == null || flushCache) {
            clientArea = composite.getClientArea();
            if (clientArea.width <= 0 || clientArea.height < 0) {
                return false;
            }
            if (clientArea.width > composite.getDisplay().getBounds().width) {
                log.warn( "Invalid client area: " + clientArea + ", display: " + composite.getDisplay().getBounds().width );
                return false;
            }

            log.debug( "LAYOUT: " + composite.hashCode() + " -> " + clientArea );

            ISolver solver = new BestFirstOptimizer( 250, 10 );
            solver.addGoal( new PriorityOnTopGoal( 1 ) );
            solver.addGoal( new MinOverallHeightGoal( 0 ) );
            //            solver.addGoal( new ElementRelationGoal( this ), 1 );

            for (Control child : composite.getChildren()) {
                ConstraintData data = (ConstraintData)child.getLayoutData();
                if (data != null) {
                    data.fillSolver( solver );
                }
            }
            LayoutSolution start = new LayoutSolution( composite, clientArea, marginWidth, marginHeight, spacing );
            start.justifyElements();

            List<ScoredSolution> results = solver.solve( start );
            solution = (LayoutSolution)results.get( results.size()-1 ).solution;
        }
        return solution != null;
    }


    /**
     * 
     */
    protected static class LayoutSolution
            implements ISolution {

        public int                      marginWidth = 10;

        public int                      marginHeight = 10;
        
        public int                      spacing = 10;

        public Rectangle                clientArea;
        
        public Composite                composite;
        
        public ArrayList<LayoutColumn>  columns = new ArrayList( 3 );
        
        
        public LayoutSolution( Composite composite, Rectangle clientArea, int mw, int mh, int s ) {
            this( clientArea, mw, mh, s );
            this.composite = composite;
            this.columns.add( new LayoutColumn( Arrays.asList( composite.getChildren() ) ) );
        }
        
        @Override
        public LayoutSolution clone() {
            try {
                LayoutSolution result = (LayoutSolution)super.clone();
                result.columns = new ArrayList( columns.size() );
                for (LayoutColumn column : columns) {
                    result.columns.add( column.clone() );
                }
                return result;
            }
            catch (CloneNotSupportedException e) {
                throw new RuntimeException( e );
            }
        }

        /** Ctor used for testing. */
        LayoutSolution( Rectangle clientArea, int mw, int mh, int s ) {
            this.clientArea = clientArea;
            this.marginWidth = mw;
            this.marginHeight = mh;
            this.spacing = s;
            assert clientArea.width > 0;
        }
        
        @Override
        public String surrogate() {
            int result = 1;
            for (LayoutColumn column : columns) {
                result = 31 * result + column.width;
                for (LayoutElement elm : column) {
                    result = 31 * result + elm.hashCode();
                }
            }
            return String.valueOf( result );
        }

        /** 
         * Returns {@link Iterables#concat(Iterable) concatenation} of the elements of all {@link #columns}. 
         */
        public Iterable<LayoutElement> elements() {
            return Iterables.concat( columns );
        }
        
        public LayoutElement remove( int index ) {
            int c = 0;
            for (LayoutColumn column : columns) {
                for (Iterator<LayoutElement> it=column.iterator(); it.hasNext(); c++) {
                    LayoutElement elm = it.next();
                    if (c == index) {
                        it.remove();
                        return elm;
                    }
                }
            }
            throw new IllegalArgumentException( "Invalid index: index=" + index + ", size=" + c );
        }
        
        /** Initialize columns width/height. */
        public void justifyElements() {
            int columnWidth = defaultColumnWidth();
            // compute columns width
            for (LayoutColumn column : columns) {
                column.width = columnWidth;
            }
            // set element heights
            for (LayoutColumn column : columns) {
                column.justifyElements();
            }
        }

        /** Equal width for all columns. */
        public int defaultColumnWidth() {
            int result = (clientArea.width - (marginWidth*2) - ((columns.size()-1) * spacing)) / columns.size();
            assert result > 0;
            return result;
        }

        @Override
        public String toString() {
            return columns.toString();
        }
    }
    

    /**
     * 
     */
    protected static class LayoutColumn
            extends ArrayList<LayoutElement> {
    
        public int      width, height;
        
        public LayoutColumn( List<Control> controls ) {
            super( controls.size() );
            for (Control control : controls) {
                add( new LayoutElement( control ) );
            }
        }

        @Override
        public LayoutColumn clone() {
            LayoutColumn result = (LayoutColumn)super.clone();
            result.clear();
            for (LayoutElement elm : this) {
                result.add( elm.clone() );
            }
            result.justifyElements();
            return result;
        }

        /** Ctor for testing only. */
        LayoutColumn() {
        }
        
        @Override
        public void clear() {
            super.clear();
            justifyElements();
        }

        public int computeMinWidth( int wHint ) {
            // minimum: wHint == 0
            int result = wHint;
            for (LayoutElement elm : this) {
//                result = Math.max( result, elm.computeWidth( wHint ) );
                MinWidthConstraint minWidth = elm.constraint( MinWidthConstraint.class, new MinWidthConstraint( SWT.DEFAULT, -1 ) );
                result = Math.max( minWidth.getValue(), result );
            }
            return result;
        }
        
        public void justifyElements() {
            assert width > 0;
            height = 0;
            for (LayoutElement elm : this) {
                MinHeightConstraint minHeight = elm.constraint( MinHeightConstraint.class, 
                        new MinHeightConstraint( SWT.DEFAULT, -1 ) );
                elm.height = elm.control.computeSize( width, minHeight.getValue() ).y;
                elm.y = height;
                
                height += elm.height;
            }
            assert isEmpty() || height > 0 : "Column height==0!";
        }

        @Override
        public String toString() {
            return Joiner.on( "" ).join( "{h=", height, ",elms=", super.toString(), "}" );  
        }
    }


    /**
     * 
     */
    protected static class LayoutElement
            implements Cloneable {
        
        public Control  control;
        
        /** Position is *without* margins! */
        public int      y = -1, height = -1;
        
        public LayoutElement( Control control ) {
            assert control != null;
            this.control = control;
        }

        /** Ctor for testing only. */
        LayoutElement() {
        }
        
        @Override
        protected LayoutElement clone() {
            try {
                return (LayoutElement)super.clone();
            }
            catch (CloneNotSupportedException e) {
                throw new RuntimeException( e );
            }
        }

        public <T extends LayoutConstraint> T constraint( Class<T> type, T defaultValue ) {
            ConstraintData data = (ConstraintData)control.getLayoutData();
            return data != null ? data.constraint( type, defaultValue ) : defaultValue;
        }

        public int computeWidth( int wHint ) {
            int width = control.computeSize( wHint, SWT.DEFAULT ).x;
            // min constraint
            MinWidthConstraint minConstraint = constraint( MinWidthConstraint.class, null );
            width = minConstraint != null ? Math.max( minConstraint.getValue(), width ) : width;
            // max constraint
            MaxWidthConstraint maxConstraint = constraint( MaxWidthConstraint.class, null );
            width = maxConstraint != null ? Math.min( maxConstraint.getValue(), width ) : width;
            return width;
        }
        
        @Override
        public int hashCode() {
            return control.hashCode();
        }

        @Override
        public boolean equals( Object obj ) {
            return control.equals( ((LayoutElement)obj).control );
        }

        @Override
        public String toString() {
            Object title = control.getData( "title" ) != null ? control.getData( "title" ) : control.hashCode();
            return Joiner.on( "" ).join( "[y=", y, ",control=", title, "]" );  
        }
        
    }

}
