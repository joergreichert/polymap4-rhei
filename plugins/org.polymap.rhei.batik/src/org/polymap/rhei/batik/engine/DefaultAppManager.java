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

import static org.polymap.rhei.batik.IPanelSite.PanelStatus.CREATED;
import static org.polymap.rhei.batik.IPanelSite.PanelStatus.FOCUSED;
import static org.polymap.rhei.batik.IPanelSite.PanelStatus.INITIALIZED;
import static org.polymap.rhei.batik.IPanelSite.PanelStatus.VISIBLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.Closer;
import org.polymap.core.runtime.Predicates;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.UIThreadExecutor;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.IPanelSite.PanelStatus;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.EventType;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.app.IAppDesign;
import org.polymap.rhei.batik.app.IAppManager;
import org.polymap.rhei.batik.engine.BatikFactory.PanelExtensionPoint;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.LayoutSupplier;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultAppManager
        implements IAppManager {

    private static Log log = LogFactory.getLog( DefaultAppManager.class );

    protected DefaultAppContext         context = new AppContext();

    protected IAppDesign                design;

    /** Cache for panel sites */
    protected Map<PanelPath,PanelSite>  panelSites = new HashMap();
    
    protected PanelPath                 top = PanelPath.ROOT;
    
    /** The panel hierarchy. */
    private Map<PanelPath,IPanel>       panels = new HashMap();


    @Override
    public void init() {
        design = BatikApplication.instance().getAppDesign();
        
        // open root panel / after main window is created
        UIThreadExecutor.async( 
                () -> context.openPanel( PanelPath.ROOT, new PanelIdentifier( "start" ) ),
                UIThreadExecutor.runtimeException() );
    }


    @Override
    public void close() {
    }    

    
    protected <T> void fireEvent( IPanel panel, EventType eventType, T newValue, T previousValue  ) {
        // XXX avoid race conditions; EventManager does not seem to always handle display events properly
        EventManager.instance().syncPublish( new PanelChangeEvent( panel, eventType, newValue, previousValue ) );        
    }

    
    @Override
    public IPanel getPanel( PanelPath path ) {
        return panels.get( path );
    }


    @Override
    public List<IPanel> findPanels( Predicate<IPanel> filter ) {
        // make a copy so that contents is stable while iterating (remove)
        return panels.values().stream().filter( filter ).collect( Collectors.toList() );
    }


    @Override
    public IAppContext getContext() {
        return context;
    }

    
    protected IPanelSite getOrCreatePanelSite( PanelPath path, int stackPriority ) {
        PanelSite result = panelSites.get( path );
        if (result == null) {
            result = new PanelSite( path, stackPriority );
            if (panelSites.put( path, result ) != null) {
                throw new IllegalStateException();
            }
        }
        return result;
    }
    
    
    protected List<IPanel> wantToBeShown( PanelPath parentPath ) {
        // create, filter, init, add panels
        return BatikFactory.instance().allPanelExtensionPoints()
                // sort in order to initialize main panel context first
                .sorted( Collections.reverseOrder( Comparator.comparing( ep -> ep.stackPriority ) ) )
                // initialize, then filter
                .filter( ep -> {
                        new PanelContextInjector( ep.panel, context ).run();
                        PanelPath path = parentPath.append( ep.panel.id() );
                        IPanelSite panelSite = getOrCreatePanelSite( path, ep.stackPriority );
                        ep.panel.setSite( panelSite, context );
                        if (ep.panel.getSite() == null) {
                            throw new IllegalStateException( "Panel.getSite() == null after setSite()!");
                        }
                        return ep.panel.wantsToBeShown(); 
                })
                .filter( Predicates.notNull() )
                .map( ep -> ep.panel )
                .collect( Collectors.toList() );
    }

    
    /**
     * Provides the default logic for opening a panel:
     * <ul>
     * </ul>
     */
    protected IPanel openPanel( PanelPath parentPath, PanelIdentifier panelId ) {
        disposePanels( parentPath );
        IPanel panel = createPanel( parentPath, panelId );
        raisePanelStatus( panel, PanelStatus.FOCUSED );
        top = panel.getSite().getPath();
        return panel;
    }

    
    protected IPanel createPanel( PanelPath parentPath, PanelIdentifier panelId ) {
        // create, filter, init, add panels
        PanelExtensionPoint ep = BatikFactory.instance().allPanelExtensionPoints()
                .filter( _ep -> _ep.panel.id().equals( panelId ) )
                .findFirst().orElseThrow( () -> new IllegalStateException( "No such panel: " + panelId ) );
                
        new PanelContextInjector( ep.panel, context ).run();
        PanelPath path = parentPath.append( ep.panel.id() );
        IPanelSite panelSite = getOrCreatePanelSite( path, ep.stackPriority );
        ep.panel.setSite( panelSite, context );
        if (ep.panel.getSite() == null) {
            throw new IllegalStateException( "Panel.getSite() == null after setSite()!");
        }

        panels.put( path, ep.panel );
        updatePanelStatus( ep.panel, PanelStatus.CREATED );
        return ep.panel;
    }

    
    protected void disposePanels( PanelPath panelPath ) {
        int pathSize = panelPath.size();
        findPanels( panel -> panel.getSite().getPath().size() > pathSize )
                .forEach( panel -> {
                        PanelPath path = panel.getSite().getPath();
                        disposePanel( path );                        
                });
    }
    
    
    protected void disposePanel( PanelPath panelPath ) {
        IPanel panel = getPanel( panelPath );
        try {
            panel.dispose();
        }
        catch (Exception e) {
            log.warn( "", e );
        }
        if (panels.remove( panelPath ) == null) {
            throw new IllegalStateException( "No Panel exists at: " + panelPath );
        }
        panelSites.remove( panelPath );
        updatePanelStatus( panel, null );
    }
        
    
    protected void closePanel( PanelPath panelPath ) {
        // close all children and siblings
        PanelPath parentPath = panelPath.removeLast( 1 );
        disposePanels( parentPath );

        // set top 
        top = parentPath;

        // raise top's panel status
        IPanel panel = getPanel( top );
        raisePanelStatus( panel, PanelStatus.FOCUSED );
    }

    
    protected void raisePanelStatus( IPanel panel, PanelStatus targetStatus ) {
        // initialize
        if (panel.getSite().getPanelStatus() == CREATED && targetStatus.ge( INITIALIZED )) {
            panel.init();
            updatePanelStatus( panel, INITIALIZED );
        }
        // make visible
        if (panel.getSite().getPanelStatus() == INITIALIZED && targetStatus.ge( VISIBLE )) {
            updatePanelStatus( panel, VISIBLE );
        }
        // make active
        if (panel.getSite().getPanelStatus() == VISIBLE && targetStatus.ge( FOCUSED )) {
            updatePanelStatus( panel, FOCUSED );
        }
    }
    
    
    protected void updatePanelStatus( IPanel panel, PanelStatus panelStatus ) {
        PanelSite panelSite = (PanelSite)panel.getSite();
        PanelStatus previous = panelSite.panelStatus;
        panelSite.panelStatus = panelStatus;
        fireEvent( panel, EventType.LIFECYCLE, panelSite.panelStatus, previous );
    }

    
    /**
     * Facade of the global {@link DefaultAppManager#context}.
     */
    protected class AppContext
            extends DefaultAppContext {
        
        @Override
        public void setUserName( String username ) {
            log.warn( "setUserName(): not implemented yet." );
        }

        @Override
        public void addPreferencesAction( IAction action ) {
            log.warn( "addPreferencesAction(): not implemented yet." );
        }

        @Override
        public IPanel openPanel( final PanelPath panelPath, final PanelIdentifier panelId ) {
            return DefaultAppManager.this.openPanel( panelPath, panelId );
        }

        @Override
        public void closePanel( final PanelPath panelPath ) {
            DefaultAppManager.this.closePanel( panelPath );
        }
        
        @Override
        public IPanel getPanel( PanelPath path ) {
            return DefaultAppManager.this.getPanel( path );
        }

        @Override
        public List<IPanel> findPanels( Predicate<IPanel> filter ) {
            return DefaultAppManager.this.findPanels( filter );
        }
        
        @Override
        public List<IPanel> wantToBeShown( PanelPath parent ) {
            return DefaultAppManager.this.wantToBeShown( parent );
        }
    }


    /**
     *
     */
    protected class PanelSite
            implements IPanelSite {

        private PanelPath           path;
        
        private Integer             stackPriority;

        private String              title;
        
        private Image               icon;

        /** Toolbar tools: {@link IAction} or {@link IContributionItem}. */
        private List                tools = new ArrayList();
        
        private IStatus             status = Status.OK_STATUS;
        
        private PanelStatus         panelStatus;
        
        private IPanelToolkit       toolkit;
        
        private int                 preferredWidth = SWT.DEFAULT;


        protected PanelSite( PanelPath path, Integer stackPriority  ) {
            assert path != null;
            this.path = path;
            this.stackPriority = stackPriority;
            this.toolkit = design.createToolkit( path );
        }

        @Override
        protected void finalize() throws Throwable {
            toolkit = Closer.create().closeAndNull( toolkit );
        }

        @Override
        public PanelStatus getPanelStatus() {
            return panelStatus;
        }

        @Override
        public PanelPath getPath() {
            return path;
        }

        @Override        
        public Integer getStackPriority() {
            return stackPriority;
        }

        @Override
        public void setStatus( IStatus status ) {
            IPanel panel = getPanel( path );
            IStatus previous = this.status;
            this.status = status;
            fireEvent( panel, EventType.STATUS, this.status, previous );
        }

        @Override
        public IStatus getStatus() {
            return status;
        }

        @Override
        public void addToolbarAction( IAction action ) {
            tools.add( action );
        }

        public List getTools() {
            return tools;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public void setTitle( String title ) {
            String previous = this.title;
            this.title = title;
            IPanel panel = getPanel( path );
            if (panel != null) {
                fireEvent( panel, EventType.TITLE, this.title, previous );
            }
            else {
                log.warn( "No panel yet for path: " + path );
            }
        }

        @Override
        public Image getIcon() {
            return icon;
        }
        
        @Override
        public void setIcon( Image icon ) {
            Image previous = this.icon;
            this.icon = icon;
            IPanel panel = getPanel( path );
            fireEvent( panel, EventType.TITLE, this.icon, previous );
        }

        @Override
        public IPanelToolkit toolkit() {
            return toolkit;
        }

        @Override
        public void layout( boolean changed ) {
            design.delayedRefresh();
        }

        @Override
        public LayoutSupplier getLayoutPreference() {
            return design.getPanelLayoutPreferences();
        }

        @Override        
        public int getPreferredWidth() {
            return preferredWidth;
        }

        @Override
        public void setPreferredWidth( int preferredWidth ) {
            this.preferredWidth = preferredWidth;
        }
        
    }

}
