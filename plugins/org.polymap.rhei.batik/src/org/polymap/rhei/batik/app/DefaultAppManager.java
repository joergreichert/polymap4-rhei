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
package org.polymap.rhei.batik.app;

import static org.polymap.rhei.batik.Panels.withPrefix;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.rap.rwt.client.service.BrowserNavigationEvent;
import org.eclipse.rap.rwt.client.service.BrowserNavigationListener;

import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.TYPE;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.internal.BatikFactory;
import org.polymap.rhei.batik.internal.PanelContextInjector;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultAppManager
        implements IAppManager, BrowserNavigationListener {

    private static Log log = LogFactory.getLog( DefaultAppManager.class );

    protected Default2AppContext        context = new Default2AppContext();

    protected IPanel                    activePanel;

    protected BrowserNavigation         browserHistory;

    protected DefaultUserPreferences    userPrefs;

    protected IAppDesign                design;
    

    @Override
    public void init() {
        design = BatikApplication.instance().getAppDesign();
        
        browserHistory = RWT.getClient().getService( BrowserNavigation.class );
        browserHistory.pushState( "Start", "Start" );
        browserHistory.addBrowserNavigationListener( this );

        // open root panel / after main window is created
        UIUtils.sessionDisplay().asyncExec( new Runnable() {
            public void run() {
                openPanel( new PanelIdentifier( "start" ) );
            }
        });
    }


    @Override
    public void close() {
        if (browserHistory != null) {
            browserHistory.removeBrowserNavigationListener( this );
            browserHistory = null;
        }
    }


    /** 
     * Browser history event. 
     */
    @Override
    public void navigated( BrowserNavigationEvent ev ) {
        log.info( "BROWSER: " + ev.getState() );
        if (activePanel == null) {
            log.info( "   no activePanel, skipping." );
            return;
        }
        
        // go to start panel (no matter what)
        while (activePanel.getSite().getPath().size() > 1) {
            closePanel( activePanel.getSite().getPath() );
            activePanel = getActivePanel();
        }

//        if ("start".equalsIgnoreCase( ev.entryId )) {
//            //mainWindow.getShell().dispose();
//            //JSExecutor.executeJS( "window.location.reload();" );
//        }
    }


    /**
     * Opens the {@link IPanel} for the given id and adds it to the top of the current
     * panel path.
     *
     * @param panelId
     * @throws IllegalStateException If no panel could be found for the given id.
     */
    @Override
    public IPanel openPanel( final PanelIdentifier panelId ) {
        // find and initialize panels
        final PanelPath prefix = activePanel != null ? activePanel.getSite().getPath() : PanelPath.ROOT;
        List<IPanel> panels = BatikFactory.instance().createPanels( new Predicate<IPanel>() {
            public boolean apply( IPanel panel ) {
                new PanelContextInjector( panel, context ).run();
                EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.INITIALIZING ) );
                
                PanelPath path = prefix.append( panel.id() );
                boolean wantsToBeShown = panel.init( new DefaultPanelSite( path ), context );
                
                if (panel.getSite() == null) {
                    throw new IllegalStateException( "Panel must not return null for getSite(). (Did you call super.init()?)");
                }
                
                if (panel.id().equals( panelId ) || wantsToBeShown) {
                    EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.INITIALIZED ) );
                    return true;
                }
                return false;
            }
        });

        // add to context
        for (IPanel panel : panels) {
            context.addPanel( panel );
        }

        //
        IPanel panel = context.getPanel( prefix.append( panelId ) );
        if (panel == null) {
            throw new IllegalStateException( "No panel for ID: " + panelId );
        }

        // update UI
        EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.ACTIVATING ) );
        activePanel = panel;
        
        EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.ACTIVATED ) );
        browserHistory.pushState( panelId.id(), activePanel.getSite().getTitle() );
        
        return activePanel;
    }

    
    @Override
    public void closePanel( PanelPath panelPath ) {
        assert activePanel != null;
        
        PanelPath activePath = activePanel.getSite().getPath();
        if (panelPath != null && !activePath.equals( panelPath )) {
            log.warn( "Active panel is not the requested panel to close. Check your usage of IPanelSite#closePanel()!" );
            return;
        }
        // remove/dispose activePanel and siblings
        for (IPanel panel : context.findPanels( withPrefix( activePath.removeLast( 1 ) ) )) {
            EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.DISPOSING ) );
            panel.dispose();
            context.removePanel( panel.getSite().getPath() );
            EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.DISPOSED ) );
        }
        
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.DEACTIVATING ) );
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.DEACTIVATED ) );
        
        // activate child panel
        activePath = activePath.removeLast( 1 );
        activePanel = context.getPanel( activePath );
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.ACTIVATING ) );
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.ACTIVATED ) );

        browserHistory.pushState( activePanel.id().id(), activePanel.getSite().getTitle() );
    }

    
    @Override
    public void activatePanel( PanelIdentifier panelId ) {
        PanelPath prefix = activePanel != null ? activePanel.getSite().getPath().removeLast( 1 ) : PanelPath.ROOT;
        PanelPath activePath = prefix.append( panelId );

        // deactivating
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.DEACTIVATING ) );
        IPanel previous = activePanel = context.getPanel( activePath );
        EventManager.instance().publish( new PanelChangeEvent( previous, TYPE.DEACTIVATED ) );

        // activating
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.ACTIVATING ) );
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.ACTIVATED ) );
        
        browserHistory.pushState( panelId.id(), activePanel.getSite().getTitle() );
    }


    @Override
    public IAppContext getContext() {
        return context;
    }

    
    @Override
    public IPanel getActivePanel() {
        return activePanel;
    }


    /**
     * 
     */
    static class UIThreadRunnable
            implements Runnable {
        
        static <T> T exec( boolean async, Callable<T> task ) {
            UIThreadRunnable runnable = new UIThreadRunnable( task );
            if (async) {
                UIUtils.sessionDisplay().asyncExec( runnable );
                return null;
            }
            else if (Display.getCurrent() == null) {
                UIUtils.sessionDisplay().syncExec( runnable );
                return (T)runnable.result;
            }            
            else {
                runnable.run();
                return (T)runnable.result;
            }            
        }
        
        public Callable         delegate;
        public Object           result;

        public UIThreadRunnable( Callable delegate ) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            try {
                result = delegate.call();
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
    }
    

    /**
     *
     */
    class Default2AppContext
            extends DefaultAppContext {

        @Override
        public void setUserName( String username ) {
            userPrefs.setUsername( username );
        }

        @Override
        public void addPreferencesAction( IAction action ) {
            userPrefs.addMenuContribution( action );
        }

        @Override
        public IPanel openPanel( final PanelIdentifier panelId ) {
            return UIThreadRunnable.exec( false, () -> DefaultAppManager.this.openPanel( panelId ) );
        }

        @Override
        public void closePanel( final PanelPath panelPath ) {
            UIThreadRunnable.exec( false, () -> { DefaultAppManager.this.closePanel( panelPath ); return null; } );
        }
    }


    /**
     *
     */
    protected class DefaultPanelSite
            implements IPanelSite {

        private PanelPath           path;

        private String              title = "Untitled";
        
        private Image               icon;

        /** Toolbar tools: {@link IAction} or {@link IContributionItem}. */
        private List                tools = new ArrayList();
        
        private IStatus             status = Status.OK_STATUS;


        protected DefaultPanelSite( PanelPath path ) {
            assert path != null;
            this.path = path;
        }

        @Override
        public PanelPath getPath() {
            return path;
        }

        @Override
        public void setStatus( IStatus status ) {
            this.status = status;
            IPanel panel = context.getPanel( path );
            EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.STATUS ) );
        }

        @Override
        public IStatus getStatus() {
            return status;
        }

        @Override
        public void addToolbarAction( IAction action ) {
            tools.add( action );
        }

        @Override
        public void addToolbarItem( IContributionItem item ) {
            tools.add( item );
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
            this.title = title;
            IPanel panel = context.getPanel( path );
            if (panel != null) {
                EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.TITLE ) );
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
            this.icon = icon;
            IPanel panel = context.getPanel( path );
            EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.TITLE ) );
        }

        @Override
        public void addSidekick() {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public IPanelToolkit toolkit() {
            return design.getToolkit();
        }

        @Override
        public void layout( boolean changed ) {
            design.delayedRefresh();
        }

        @Override
        public int getLayoutPreference( String key ) {
            if (LAYOUT_SPACING_KEY.equals( key )) {
                // 1000px display width -> 20px spacing
                return (int)(UIUtils.sessionDisplay().getBounds().width * 0.02);
            }
            else if (LAYOUT_MARGINS_KEY.equals( key )) {
                // 1000px display width -> 20px margins
                return (int)(UIUtils.sessionDisplay().getBounds().width * 0.02);
            }
            else {
                throw new RuntimeException( "Unknown layout key: " + key );
            }
        }

    }

}
