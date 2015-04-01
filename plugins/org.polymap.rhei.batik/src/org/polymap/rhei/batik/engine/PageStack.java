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
    public class Page {
        
        public K            key;
        
        public int          priority;
        
        /** The state that was requested from client code via API */
        public boolean      isVisible = true;
        
        /** The actual visibility in the UI as calculated by the {@link PageStackLayout}. */
        public boolean      isShown;
        
        public Composite    control;
        
        public int          preferredWidth = SWT.DEFAULT;

        protected Page( Composite panel, int priority, K key ) {
            this.priority = priority;
            this.control = panel;
            this.key = key;
        }
        
        public int getPriority() {
            return priority;
        }
        
        @Override
        public String toString() {
            return "Page[key=" + key + ", priority=" + priority + ", visible=" + isVisible + ", shown=" + isShown + "]";
        }
    }
    
    
    // instance *******************************************
    
    private Map<K,Page>     pages = new HashMap();
    
    private Page            focusedPage;
    

    public PageStack( Composite parent, LayoutSupplier layoutSettings ) {
        super( parent, SWT.NONE );
        setLayout( new PageStackLayout( this, layoutSettings ) );
    }

    
    protected void preUpdateLayout() {
    }
    
    
    protected void postUpdateLayout() {
    }

    
    @Override
    public PageStackLayout getLayout() {
        return (PageStackLayout)super.getLayout();
    }


    public Collection<Page> getPages() {
        return pages.values();
    }

    
    public Composite createPage( K key, int priority ) {
//        ScrolledComposite scrolled = new ScrolledComposite( this, SWT.VERTICAL );
//        scrolled.setExpandVertical( true );
//        //scrolled.setExpandHorizontal( true );

        Composite content = new Composite( this, SWT.NONE );
        if (pages.put( key, new Page( content, priority, key ) ) != null) {
            throw new IllegalStateException( "Key already exists: " + key );
        }
//        Composite content = new Composite( scrolled, SWT.NONE );
//        content.setLayout( new FillLayout() );
//        scrolled.setContent( content );
        return content;
    }
    

    public void removePage( K key ) {
        Page page = pages.remove( key );
        if (!page.control.isDisposed()) {
            page.control.dispose();
        }
        page.control = null;
    }


    public Page getPage( K key ) {
        return pages.get( key );
    }
    
    
    public boolean hasPage( K key ) {
        return pages.containsKey( key );
    }


    public void setPageVisible( K key, boolean visible ) {
        Page page = getPage( key );
        page.isVisible = visible;
    }

    
    public void setPagePreferredWidth( K key, int preferredWidth ) {
        Page page = getPage( key );
        page.preferredWidth = preferredWidth;
    }


//    public void setFocusedPage( K key ) {
//        focusedPage = getPage( key );
//    }
    
    
    public Page getFocusedPage() {
        return focusedPage;
    }


    public void showEmptyPage() {
    }


    @Override
    public Point computeSize( int wHint, int hHint, boolean changed ) {
        return getLayout().computeSize( this, wHint, hHint, changed );
    }


    public void reflow( boolean b ) {
        super.layout( true );
    }
    
}
