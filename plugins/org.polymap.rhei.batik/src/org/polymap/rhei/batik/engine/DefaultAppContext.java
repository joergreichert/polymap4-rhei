/*
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelPath;


/**
 * Provides default implementation for property handling and panel hierarchy.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultAppContext
        implements IAppContext {

    private static Log log = LogFactory.getLog( DefaultAppContext.class );

    /**
     * 
     */
    class ScopedPropertyValue {
        protected Object        value;
        protected String        scope;

        public ScopedPropertyValue( Object value, String scope ) {
            this.value = value;
            this.scope = scope;
        }
    }

    
    // instance *******************************************
    
    /** The property suppliers. */
    private List<ScopedPropertyValue>       properties = new ArrayList();
    
    protected ReadWriteLock                 propertiesLock = new ReentrantReadWriteLock();

    /** The panel hierarchy. */
    private Map<PanelPath,IPanel>           panels = new HashMap();


    @Override
    public IPanel getPanel( PanelPath path ) {
        return panels.get( path );
    }


    @Override
    public List<IPanel> findPanels( Predicate<IPanel> filter ) {
        // make a copy so that contents is stable while iterating (remove)
        return panels.values().stream().filter( filter ).collect( Collectors.toList() );
    }

    
    public void addPanel( IPanel panel, PanelPath path ) {
        if (panels.put( path, panel ) != null) {
            throw new IllegalStateException( "Panel already exists at: " + path );
        }
    }


    public void removePanel( PanelPath path ) {
        if (panels.remove( path ) == null) {
            throw new IllegalStateException( "No Panel exists at: " + path );
        }
    }

    
//    protected IPanel openPanel( final PanelIdentifier panelId ) {
//        // find and initialize panels
//        final PanelPath prefix = activePanel != null ? activePanel.getSite().getPath() : PanelPath.ROOT;
//        List<IPanel> createdPanels = BatikComponentFactory.instance().createPanels( new Predicate<IPanel>() {
//            public boolean apply( IPanel panel ) {
//                new PanelContextInjector( panel, DefaultAppContext.this ).run();
//                PanelPath path = prefix.append( panel.id() );
//                boolean wantsToBeShown = panel.init( new DesktopPanelSite( path ), this );
//                return panel.id().equals( panelId ) || wantsToBeShown;
//            }
//        });
//
//        // add to context
//        for (IPanel panel : createdPanels) {
//            addPanel( panel );
//        }
//
//        //
//        IPanel panel = getPanel( prefix.append( panelId ) );
//        if (panel == null) {
//            throw new IllegalStateException( "No panel for ID: " + panelId );
//        }
//        
//        EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.OPENING ) );
//        
//        Composite page = scrolledPanelContainer.createPage( panel.id() );
//        page.setLayout( new FillLayout() );
//        panel.createContents( page );
//        page.layout( true );
//        scrolledPanelContainer.showPage( panel.id() );
//        
//        Point panelSize = page.computeSize( SWT.DEFAULT, SWT.DEFAULT );
//        scrolledPanelContainer.setMinHeight( panelSize.y );
//
//        activePanel = panel;
//        EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.OPENED ) );
//
//        return activePanel;
//    }


    @Override
    public void addListener( Object handler, EventFilter<PanelChangeEvent>... filters ) {
        EventManager.instance().subscribe( handler, filters );
    }


    @Override
    public void removeListener( Object handler ) {
        EventManager.instance().unsubscribe( handler );
    }


    public <T> T getPropertyValue( Context<T> prop ) {
        try {
            propertiesLock.readLock().lock();
            
            ScopedPropertyValue result = findPropertyValue( prop );
            return result != null ? (T)result.value : null;
        }
        finally {
            propertiesLock.readLock().unlock();
        }
    }
    

    public <T> T setPropertyValue( Context<T> prop, T value ) {
        try {
            propertiesLock.writeLock().lock();

            ScopedPropertyValue found = findPropertyValue( prop );
            if (value == null) {
                if (found != null) {
                    properties.remove( found );
                    return (T)found.value;
                }
                return null;
            }
            else if (found != null) {
                Object result = found.value;
                found.value = value;
                return (T)result;
            }
            else {
                properties.add( new ScopedPropertyValue( value, prop.getScope() ) );
                return null;
            }
        }
        finally {
            propertiesLock.writeLock().unlock();
        }
    }

    
    public <T> boolean compareAndSetPropertyValue( Context<T> prop, T expect, T update ) {
        try {
            propertiesLock.writeLock().lock();
         
            T value = getPropertyValue( prop );
            if (Objects.equals( value, expect )) {
                setPropertyValue( prop, update );
                return true;
            }
            else {
                return false;
            }
        }
        finally {
            propertiesLock.writeLock().unlock();
        }        
    }
    
    
    protected ScopedPropertyValue findPropertyValue( Context prop ) {
        ScopedPropertyValue result = null;
        for (ScopedPropertyValue value : properties) {
            if (value.scope.equals( prop.getScope() )
                    && prop.getDeclaredType().isAssignableFrom( value.value.getClass() )) {
                if (result != null) {
                    throw new IllegalStateException( "More than one match for context property: " + prop );                    
                }
                result = value;
            }
        }
        return result;
    }


    @Override
    public <T> T propagate( T panel ) {
        assert panel != null: "Argument is null";
        new PanelContextInjector( panel, this ).run();
        return panel;
    }
    
    

//    protected int subTypeDistance( Class<?> type, Class<?> subType ) {
//        Class<?> cursor = subType;
//        for (int i=0; cursor != null; i++) {
//            if (cursor.equals( type )) {
//                return i;
//            }
//            cursor = subType.getSuperclass();
//        }
//        throw new RuntimeException( "Types are not related: " + type.getSimpleName() + " - " + subType.getSimpleName() );
//    }

}