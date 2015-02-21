/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.batik.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import org.eclipse.ui.forms.widgets.ILayoutExtension;

import org.polymap.rhei.batik.internal.PageStack.Page;

/**
 * Layout of the {@link PageStack}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class PageStackLayout 
        extends Layout 
        implements ILayoutExtension {

    private static Log log = LogFactory.getLog( PageStackLayout.class );

    public static final int         DEFAULT_PAGE_MIN_WIDTH = 300;
    
    private final PageStack         pageStack;
    

    PageStackLayout( PageStack pageStack ) {
        this.pageStack = pageStack;
    }

    
    @Override
    protected Point computeSize( Composite composite, int wHint, int hHint, boolean flushCache ) {
        Rectangle clientArea = pageStack.getClientArea();
        
        Point result = new Point( wHint, hHint );
        result.x = wHint != SWT.DEFAULT ? wHint : clientArea.width;
        result.y = hHint != SWT.DEFAULT ? hHint : clientArea.height;
        return result;
    }
    
    
    @Override
    protected void layout( Composite composite, boolean flushCache ) {
        log.info( "layout() ..." );
        assert pageStack == composite;
        Rectangle clientArea = pageStack.getClientArea();
        
        Collection<Page> pages = pageStack.getPages();
        List<Page> sortedVisible = pages.stream()
                .filter( page -> page.visible )
                .sorted( Comparator.comparingInt( Page::getPriority ) )
                .collect( Collectors.toList() );

        // calc and make max pages visible: top -> down
        List<Page> topDown = new ArrayList( sortedVisible );
        Collections.reverse( topDown );

        int filledWidth = 0;
        int grey = 0xff, greyStep = 4;
        for (Page page : topDown) {
            page.control.setVisible( false );

            if (filledWidth <= clientArea.width) {
                Point minPageSize = page.control.computeSize( SWT.DEFAULT, Integer.MAX_VALUE );
                int minPageWidth = Math.max( minPageSize.x, DEFAULT_PAGE_MIN_WIDTH );
                // right most is always displayed
                if (filledWidth == 0) {
                    filledWidth = Math.min( minPageWidth, clientArea.width );
                    // just save minPageSize for next step
                    page.control.setBounds( 0, 0, minPageWidth, minPageSize.y );
                    page.control.setVisible( true );
                }
                //
                else if (filledWidth + minPageWidth <= clientArea.width) {
                    filledWidth += minPageWidth;
                    // just save minPageSize for next step
                    page.control.setBounds( 0, 0, minPageWidth, minPageSize.y );
                    page.control.setVisible( true );
                    
                    page.control.setBackground( new Color( page.control.getDisplay(), grey, grey, grey ) );
                    grey -= greyStep;
                }
            }
        }
        
        // actually set bounds: bottom -> up
        int panelX = 0;
        Page topPage = topDown.isEmpty() ? null : topDown.get( 0 );
        for (Page page : sortedVisible) {
            if (page.control.isVisible()) {
                Rectangle minSize = page.control.getBounds();
                // all remaining width if topPage
                int pageWidth = page == topPage ? clientArea.width - panelX : minSize.width;
                page.control.setBounds( panelX, 0, pageWidth, clientArea.height );
                panelX += pageWidth;
                log.info( "    page: " + page.control.getBounds() );                
            }
        }
    }
    
    
    @Override
    public int computeMaximumWidth( Composite parent, boolean changed ) {
        return computeSize( parent, SWT.DEFAULT, SWT.DEFAULT, changed ).x;
    }
    
    
    @Override
    public int computeMinimumWidth( Composite parent, boolean changed ) {
        return computeSize( parent, 0, SWT.DEFAULT, changed ).x;
    }
}