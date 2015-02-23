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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.batik.toolkit.LayoutSupplier;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PageStack<K>
        extends Composite {

    private static Log log = LogFactory.getLog( PageStack.class );

    /**
     * 
     */
    static class Page {
        
        int                 priority;
        
        boolean             visible = true;
        
        Composite           control;

        public Page( Composite panel , int priority  ) {
            this.priority = priority;
            this.control = panel;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public String toString() {
            return "Page[priority=" + priority + ", visible=" + visible + ", control=" + control.getClass().getSimpleName() + "]";
        }
    }
    
    
    // instance *******************************************
    
    private Map<K,Page>     pages = new HashMap();
    

    public PageStack( Composite parent, LayoutSupplier layoutSettings ) {
        super( parent, SWT.NONE );
        setLayout( new PageStackLayout( this, layoutSettings ) );
    }

    
    @Override
    public PageStackLayout getLayout() {
        return (PageStackLayout)super.getLayout();
    }


    public Collection<Page> getPages() {
        return pages.values();
    }

    
    public Composite createPage( K key, int priority ) {
        Composite scrolled = new Composite( this, SWT.VERTICAL );
        if (pages.put( key, new Page( scrolled, priority ) ) != null) {
            throw new IllegalStateException( "Key already exists: " + key );
        }
        return scrolled;
    }
    
    
    public void removePage( K key ) {
        Page page = pages.remove( key );
        if (!page.control.isDisposed()) {
            page.control.dispose();
        }
    }


    public Page getPage( K key ) {
        return pages.get( key );
    }
    
    
//    /**
//     * Shows the given page. This method has no effect if the given page is not
//     * contained in this pagebook.
//     */
//    public void showPage( Control page ) {
//        if (page == currentPage)
//            return;
//        if (page.getParent() != this)
//            return;
//        Control oldPage = currentPage;
//        currentPage = page;
//        // show new page
//        if (page != null) {
//            if (!page.isDisposed()) {
//                // page.setVisible(true);
//                layout( true );
//                page.setVisible( true );
//            }
//        }
//        // hide old *after* new page has been made visible in order to avoid
//        // flashing
//        if (oldPage != null && !oldPage.isDisposed())
//            oldPage.setVisible( false );
//    }


    public boolean hasPage( K key ) {
        return pages.containsKey( key );
    }


    public void showPage( K key ) {
        Page page = getPage( key );
        page.visible = true;
    }


    public void hidePage( K key ) {
        Page page = getPage( key );
        page.visible = false;
    }


    public void showEmptyPage() {
    }


    @Override
    public Point computeSize( int wHint, int hHint, boolean changed ) {
        return getLayout().computeSize( this, wHint, hHint, changed );
    }


    public void setMinHeight( int y ) {
    }


    public void reflow( boolean b ) {
        super.layout( true );
    }
    
}
