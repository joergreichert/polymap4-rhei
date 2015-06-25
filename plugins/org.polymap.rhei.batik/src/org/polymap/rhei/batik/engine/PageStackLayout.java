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
package org.polymap.rhei.batik.engine;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import org.eclipse.ui.forms.widgets.ILayoutExtension;

import org.polymap.rhei.batik.engine.PageStack.Page;
import org.polymap.rhei.batik.toolkit.LayoutSupplier;

/**
 * Layout of the {@link PageStack}.
 * <p/>
 * <b>Layout rules:</b>
 * <ul>
 * <li>children are sorted in a <b>priority stack</b></li>
 * <li>client can request a page {@link PageStack.Page#isVisible}</li>
 * <li>one visible page is marked as "focused"<li>
 * <li>the layout decides which pages are actually {@link PageStack.Page#isShown} depending on:</li> 
 *     <ul>
 *     <li>the priority of the page starting from the "focused" page
 *     <li>minimal (preferred?) width of the pages</b>
 *     <li>available space</li>
 *     </ul>
 * <li></li>
 * <li></li>
 * </ul>
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PageStackLayout 
        extends Layout 
        implements ILayoutExtension {

    private static Log log = LogFactory.getLog( PageStackLayout.class );

    public static final int         DEFAULT_PAGE_MIN_WIDTH = 300;
    
    private final PageStack         pageStack;

    private LayoutSupplier          margins;
    
    private Rectangle               cachedClientArea;
    

    PageStackLayout( PageStack pageStack, LayoutSupplier margins ) {
        this.pageStack = pageStack;
        this.margins = margins;
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
        assert pageStack == composite;

        Rectangle clientArea = pageStack.getClientArea();
        log.debug( "layout(): clientArea=" + clientArea );        

        Rectangle displayArea = composite.getDisplay().getBounds();
        if (clientArea.width > displayArea.width) {
            log.warn( "Invalid client area: " + clientArea + ", display width: " + displayArea.width + ", flushCache: " + flushCache );
            return;
        }
        
        if (!flushCache && clientArea.equals( cachedClientArea )) {
            log.warn( "Ignoring cachedClientArea: " + cachedClientArea + ", flushCache: " + flushCache );
            return;
        }
        cachedClientArea = clientArea;
        
        pageStack.preUpdateLayout();
        
        // available size
        int availWidth = clientArea.width - margins.getMarginLeft() - margins.getMarginRight();
        int availHeight = clientArea.height - margins.getMarginTop() + margins.getMarginBottom();

        Collection<Page> pages = pageStack.getPages();
        List<Page> sortedVisible = pages.stream()
                .filter( page -> page.isVisible )
                .sorted( Comparator.comparingInt( Page::getPriority ) )
                .collect( Collectors.toList() );

        // calc and make max pages visible: top down
        List<Page> topDown = new ArrayList( sortedVisible );
        Collections.reverse( topDown );

        int filledWidth = 0;
        int greyStep = 5, grey = 0xff - greyStep;
        
        Page focusedPage = pageStack.getFocusedPage();
        if (focusedPage == null && !topDown.isEmpty()) {
            focusedPage = topDown.get( 0 );
        }
        boolean focusedPageSeen = false;
        
        for (Page page : topDown) {
            boolean pageVisible = false;
            page.isShown = false;

            // focused page is the top most page shown
            focusedPageSeen = focusedPageSeen || page == focusedPage;
            if (focusedPageSeen) {
                
                if (filledWidth <= availWidth) {
                    Point prefPageSize = page.preferredWidth > 0
                            ? new Point( page.preferredWidth, Integer.MAX_VALUE )
                            : page.control.computeSize( SWT.DEFAULT, Integer.MAX_VALUE );

                    // limit: DEFAULT_PAGE_MIN_WIDTH < prefPageSize < clientArea.width
                    int prefPageWidth = min( availWidth, max( prefPageSize.x, DEFAULT_PAGE_MIN_WIDTH ) );

                    // right most is always displayed
                    if (filledWidth == 0) {
                        page.isShown = true;
                        filledWidth = min( prefPageWidth, availWidth );
                    }
                    //
                    else if (filledWidth + prefPageWidth <= availWidth) {
                        page.isShown = true;
                        filledWidth += prefPageWidth + margins.getSpacing();

                        //page.control.setBackground( new Color( page.control.getDisplay(), grey, grey, grey ) );
                        grey -= greyStep;
                    }
                    
                    if (page.isShown) {
                        // just save minPageSize for next step
                        page.control.setBounds( 0, 0, prefPageWidth, prefPageSize.y );
                        page.control.setVisible( pageVisible = true );
                    }
                }
            }
            // it is important to do this only on pages that are actually not visible;
            // doing this in advance on top of the loop would remove focus from the page
            if (!pageVisible) {
                page.control.setVisible( false );
            }
        }
        
        // actually set bounds: bottom up
        int panelX = margins.getMarginLeft();
        for (Page page : sortedVisible) {
            if (page.control.isVisible()) {
                Rectangle minSize = page.control.getBounds();
                // all remaining width if topPage
                int pageWidth = page == focusedPage 
                        ? clientArea.width - panelX - margins.getMarginRight() 
                        : minSize.width;
                
                page.control.setBounds( panelX, margins.getMarginTop(), pageWidth, availHeight );
                page.control.layout( flushCache );
                
                if (page.control instanceof ScrolledComposite) {
                    Point pageSize = page.control.computeSize( pageWidth, SWT.DEFAULT );
                    ((ScrolledComposite)page.control).setMinHeight( pageSize.y );
                }
                
                panelX += pageWidth + margins.getSpacing();
                log.debug( "    page: " + page.control.getBounds() );                
            }
        }
        pageStack.postUpdateLayout();
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