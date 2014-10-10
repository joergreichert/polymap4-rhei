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
package org.polymap.rhei.batik.layout.desktop;

import static org.polymap.rhei.batik.Panels.withPrefix;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;

import org.eclipse.rwt.IBrowserHistory;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.events.BrowserHistoryEvent;
import org.eclipse.rwt.events.BrowserHistoryListener;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.forms.widgets.ScrolledPageBook;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.IApplicationLayouter;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.TYPE;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.app.BatikApplication;
import org.polymap.rhei.batik.internal.BatikComponentFactory;
import org.polymap.rhei.batik.internal.DefaultAppContext;
import org.polymap.rhei.batik.internal.PanelContextInjector;
import org.polymap.rhei.batik.layout.desktop.DesktopActionBar.PLACE;
import org.polymap.rhei.batik.toolkit.ConstraintLayout;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DesktopAppManager
        implements IApplicationLayouter, BrowserHistoryListener {

    private static Log log = LogFactory.getLog( DesktopAppManager.class );
    
    protected DesktopAppContext         context = new DesktopAppContext();

    protected DesktopToolkit            tk = new DesktopToolkit( context );

    protected DesktopAppWindow          mainWindow;

    protected DesktopActionBar          actionBar;

    protected ScrolledPageBook          scrolledPanelContainer;

    protected IPanel                    activePanel;

    protected IBrowserHistory           browserHistory;

    protected UserPreferences           userPrefs;

    protected PanelNavigator            panelNavigator;

    protected StatusManager             statusManager;
    

    @Override
    public Window initMainWindow( Display display ) {
        browserHistory = RWT.getBrowserHistory();
        browserHistory.createEntry( "Start", "Start" );
        browserHistory.addBrowserHistoryListener( this );
        
        // panel navigator area
        actionBar = new DesktopActionBar( context, tk );
        actionBar.add( new PanelToolbar( this ), PLACE.PANEL_TOOLBAR );
        actionBar.add( panelNavigator = new PanelNavigator( this ), PLACE.PANEL_NAVI );
        actionBar.add( userPrefs = new UserPreferences( this ), PLACE.USER_PREFERENCES );
        actionBar.add( statusManager = new StatusManager( this ), PLACE.STATUS );

        // mainWindow
        mainWindow = new DesktopAppWindow( this ) {
            @Override
            protected Composite fillNavigationArea( Composite parent ) {
                return actionBar.createContents( parent );
            }
            @Override
            protected Composite fillPanelArea( Composite parent ) {
                scrolledPanelContainer = new ScrolledPageBook( parent, SWT.V_SCROLL );
                scrolledPanelContainer.showEmptyPage();
                
//                scrolledPanelContainer = (ScrolledComposite)tk.createComposite( parent, SWT.BORDER, SWT.V_SCROLL );
//                panelArea = (Composite)scrolledPanelContainer.getContent();
//                panelArea.setLayout( new FillLayout( SWT.VERTICAL ) );
//                tk.createLabel( panelArea, "Panels..." );
                return scrolledPanelContainer;
            }
            @Override
            protected StatusManager getStatusManager() {
                return statusManager;
            }
        };
        // open root panel / after main window is created
        display.asyncExec( new Runnable() {
            public void run() {
                openPanel( new PanelIdentifier( "start" ) );
            }
        });
        return mainWindow;
    }


    @Override
    public void dispose() {
        browserHistory.removeBrowserHistoryListener( this );
    }


    /** 
     * Browser history event. 
     */
    @Override
    public void navigated( BrowserHistoryEvent ev ) {
        log.info( "BROWSER: " + ev.entryId );
        
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
    protected IPanel openPanel( final PanelIdentifier panelId ) {
        // find and initialize panels
        final PanelPath prefix = activePanel != null ? activePanel.getSite().getPath() : PanelPath.ROOT;
        List<IPanel> panels = BatikComponentFactory.instance().createPanels( new Predicate<IPanel>() {
            public boolean apply( IPanel panel ) {
                new PanelContextInjector( panel, context ).run();
                EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.INITIALIZING ) );
                
                PanelPath path = prefix.append( panel.id() );
                boolean wantsToBeShown = panel.init( new DesktopPanelSite( path ), context );
                
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
        
        final Composite page = scrolledPanelContainer.createPage( panel.id() );
        page.setLayout( newPanelLayout() );

        panel.createContents( page );
        scrolledPanelContainer.showPage( panel.id() );

        Point panelSize = page.computeSize( SWT.DEFAULT, SWT.DEFAULT );
        scrolledPanelContainer.setMinHeight( panelSize.y );

        activePanel = panel;
        EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.ACTIVATED ) );

        browserHistory.createEntry( panelId.id(), activePanel.getSite().getTitle() );
        mainWindow.delayedRefresh( null );
        
        return activePanel;
    }

    
    protected Layout newPanelLayout() {
        // 1000px display width -> 30px margin
        int margins = (int)(BatikApplication.sessionDisplay().getBounds().width * 0.03 );
        ConstraintLayout result = new ConstraintLayout( margins, margins, margins );
        log.info( "display width: " + BatikApplication.sessionDisplay().getBounds().width + " -> margin: " + result.marginWidth );
        return result;
    }
    
    
    protected void closePanel( PanelPath panelPath ) {
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
            if (scrolledPanelContainer.hasPage( panel.id() )) {
                scrolledPanelContainer.removePage( panel.id() );
            }
            EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.DISPOSED ) );
        }
        
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.DEACTIVATING ) );
        scrolledPanelContainer.removePage( activePanel.id() );
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.DEACTIVATED ) );
        
        // activate child panel
        activePath = activePath.removeLast( 1 );
        activePanel = context.getPanel( activePath );
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.ACTIVATING ) );
        scrolledPanelContainer.showPage( activePanel.id() );
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.ACTIVATED ) );

        browserHistory.createEntry( activePanel.id().id(), activePanel.getSite().getTitle() );
    }

    
    public void activatePanel( PanelIdentifier panelId ) {
        PanelPath prefix = activePanel != null ? activePanel.getSite().getPath().removeLast( 1 ) : PanelPath.ROOT;
        PanelPath activePath = prefix.append( panelId );

        // deactivating
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.DEACTIVATING ) );
        IPanel previous = activePanel = context.getPanel( activePath );
        EventManager.instance().publish( new PanelChangeEvent( previous, TYPE.DEACTIVATED ) );

        // activating
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.ACTIVATING ) );
        if (scrolledPanelContainer.hasPage( panelId )) {
            scrolledPanelContainer.showPage( panelId );
        }
        else {
            EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.ACTIVATING ) );
            Composite page = scrolledPanelContainer.createPage( activePanel.id() );
            page.setLayout( newPanelLayout() );

            activePanel.createContents( page );
            page.layout( true );
            scrolledPanelContainer.showPage( activePanel.id() );
            
            Point panelSize = page.computeSize( SWT.DEFAULT, SWT.DEFAULT );
            scrolledPanelContainer.setMinHeight( panelSize.y );
        }
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.ACTIVATED ) );
        
        browserHistory.createEntry( panelId.id(), activePanel.getSite().getTitle() );
        mainWindow.delayedRefresh( null );
    }


    protected DesktopAppContext getContext() {
        return context;
    }

    
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
                BatikApplication.sessionDisplay().asyncExec( runnable );
                return null;
            }
            else if (Display.getCurrent() == null) {
                BatikApplication.sessionDisplay().syncExec( runnable );
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
    class DesktopAppContext
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
            return UIThreadRunnable.exec( false, new Callable<IPanel>() {
                public IPanel call() throws Exception {
                    return DesktopAppManager.this.openPanel( panelId );
                }
            });
        }

        @Override
        public void closePanel( final PanelPath panelPath ) {
            UIThreadRunnable.exec( false, new Callable() {
                public Object call() throws Exception {
                    DesktopAppManager.this.closePanel( panelPath );
                    return false;
                }
            });
        }
    }


    /**
     *
     */
    protected class DesktopPanelSite
            implements IPanelSite {

        private PanelPath           path;

        private String              title = "Untitled";
        
        private Image               icon;

        /** Toolbar tools: {@link IAction} or {@link IContributionItem}. */
        private List                tools = new ArrayList();
        
        private IStatus             status = Status.OK_STATUS;


        protected DesktopPanelSite( PanelPath path ) {
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
            panelNavigator.updateBreadcrumb();
        }

        @Override
        public Image getIcon() {
            return icon;
        }
        
        @Override
        public void setIcon( Image icon ) {
            this.icon = icon;
            panelNavigator.updateBreadcrumb();
        }

        @Override
        public void addSidekick() {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public IPanelToolkit toolkit() {
            return tk;
        }

        @Override
        public void layout( boolean changed ) {
            mainWindow.delayedRefresh( null );
        }

        @Override
        public int getLayoutPreference( String key ) {
            if (LAYOUT_SPACING_KEY.equals( key )) {
                // 1000px display width -> 20px spacing
                return (int)(BatikApplication.sessionDisplay().getBounds().width * 0.02);
            }
            else if (LAYOUT_MARGINS_KEY.equals( key )) {
                // 1000px display width -> 20px margins
                return (int)(BatikApplication.sessionDisplay().getBounds().width * 0.02);
            }
            else {
                throw new RuntimeException( "Unknown layout key: " + key );
            }
        }

    }

}
