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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.rap.rwt.client.service.BrowserNavigationEvent;
import org.eclipse.rap.rwt.client.service.BrowserNavigationListener;

import org.polymap.core.runtime.Closer;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.TYPE;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.app.DefaultActionBar.PLACE;
import org.polymap.rhei.batik.app.DefaultAppManager.DefaultPanelSite;
import org.polymap.rhei.batik.internal.PageStack;
import org.polymap.rhei.batik.toolkit.ConstraintLayout;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.LayoutSupplier;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultAppDesign
        implements IAppDesign, BrowserNavigationListener {

    private static Log log = LogFactory.getLog( DefaultAppDesign.class );

    public static final int             MAX_CONTENT_WIDTH = 1100;
    
    protected Shell                     mainWindow;
    
    protected BrowserNavigation         browserHistory;

    protected IPanelToolkit             toolkit;

    protected StatusManager             statusManager;

    protected DefaultAppToolbar         toolbar;

    protected DefaultAppNavigator       navigator;

    protected DefaultUserPreferences    userPrefs;

    protected PageStack<PanelIdentifier> panelsArea;
    
    protected DefaultLayoutSupplier     panelLayoutSettings = new DefaultLayoutSupplier();
 
    protected DefaultLayoutSupplier     appLayoutSettings = new DefaultLayoutSupplier();


    @Override
    public void init() {
        toolkit = new DefaultToolkit();

        browserHistory = RWT.getClient().getService( BrowserNavigation.class );
        browserHistory.addBrowserNavigationListener( this );
    }


    @Override
    public void close() {
        toolkit = Closer.create().closeAndNull( toolkit );
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
        log.info( "navigated(): " + ev.getState() );
//        BatikApplication.instance().getAppManager().activatePanel( new PanelIdentifier( "start" ) );
        
//        if (activePanel == null) {
//            log.info( "   no activePanel, skipping." );
//            return;
//        }
//        
//        // go to start panel (no matter what)
//        while (activePanel.getSite().getPath().size() > 1) {
//            closePanel( activePanel.getSite().getPath() );
//            activePanel = getActivePanel();
//        }
    }


    @Override
    public IPanelToolkit getToolkit() {
        return toolkit;
    }


    @Override
    public Shell createMainWindow( Display display ) {
        mainWindow = new Shell( display, SWT.NO_TRIM );
        mainWindow.setMaximized( true );
        UIUtils.setVariant( mainWindow, IAppDesign.CSS_SHELL );

        adjustLayout();
        
        mainWindow.addControlListener( new ControlAdapter() {
            private Rectangle lastDisplayArea = display.getBounds();
            @Override
            public void controlResized( ControlEvent ev ) {
                Rectangle displayArea = display.getBounds();
                if (!displayArea.equals( lastDisplayArea )) {
                    lastDisplayArea = displayArea;
                    adjustLayout();
                }
            }
        });
        
        // header
        Composite headerContainer = fillHeaderArea( mainWindow );
        headerContainer.setLayoutData( FormDataFactory.filled().clearBottom().create() );

        // actionbar
        Composite actionbarContainer = fillActionArea( mainWindow );
        actionbarContainer.setLayoutData( FormDataFactory.filled().top( headerContainer ).clearBottom().height( 30 ).create() );

        // panels
        Composite panelContainer = fillPanelArea( mainWindow );
        panelContainer.setLayoutData( FormDataFactory.filled().top( actionbarContainer, 10 ).create() );

        IAppManager appManager = BatikApplication.instance().getAppManager();
        appManager.getContext().addListener( this, new EventFilter<PanelChangeEvent>() {
            public boolean apply( PanelChangeEvent input ) {
                return input.getType() == TYPE.ACTIVATING 
                        || input.getType() == TYPE.ACTIVATED
                        || input.getType() == TYPE.DISPOSING
                        || input.getType() == TYPE.DEACTIVATING;
            }
        });
        
        mainWindow.open();
        return mainWindow;
    }


    protected Composite fillHeaderArea( Composite parent ) {
        Composite result = new Composite( parent, SWT.NO_FOCUS | SWT.BORDER );
        UIUtils.setVariant( result, IAppDesign.CSS_HEADER );
        result.setLayout( FormLayoutFactory.defaults().margins( 30, 0 ).create() );
        Label l = new Label( result, SWT.NONE );
        UIUtils.setVariant( l, IAppDesign.CSS_HEADER );
        l.setText( "mapzone" );
        return result;
    }


    protected Composite fillActionArea( Composite parent ) {
        IAppContext context = BatikApplication.instance().getAppManager().getContext();
        DefaultActionBar actionbar = new DefaultActionBar( context, toolkit );
        //actionbar.add( toolbar = new DefaultAppToolbar(), PLACE.PANEL_TOOLBAR );
        actionbar.add( navigator = new DefaultAppNavigator(), PLACE.PANEL_NAVI );
        //actionbar.add( userPrefs = new DefaultUserPreferences(), PLACE.USER_PREFERENCES );
        //actionbar.add( statusManager = new StatusManager(), PLACE.STATUS );
        return actionbar.createContents( parent, SWT.BORDER );
    }


    protected Composite fillPanelArea( Composite parent ) {
        panelsArea = new PageStack( parent, new DelegatingLayoutSupplier( getAppLayoutSettings() ) {
            @Override
            public int getMarginLeft() {
                return 0;
            }
            @Override
            public int getMarginRight() {
                return 0;
            }
            @Override
            public int getMarginTop() {
                return getSpacing()/2;
            }
        });
        panelsArea.showEmptyPage();
        return UIUtils.setVariant( panelsArea, CSS_PANELS );
    }

    
    @EventHandler(display=true)
    protected void panelChanged( PanelChangeEvent ev ) {
        IPanel panel = ev.getPanel();

        //
        if (ev.getType() == TYPE.ACTIVATING) {
            if (panelsArea.hasPage( panel.id() )) {
                panelsArea.showPage( panel.id() );                
            }
            else {
                Composite page = panelsArea.createPage( panel.id(),
                        // every new panel is created on top
                        (int)System.currentTimeMillis() );
                page.setLayout( newPanelLayout() );
                UIUtils.setVariant( page, CSS_PANEL );

                panel.createContents( page );
                page.layout( true );
                panelsArea.showPage( panel.id() );

                Point panelSize = page.computeSize( SWT.DEFAULT, SWT.DEFAULT );
                panelsArea.setMinHeight( panelSize.y );
            }
        }
        //
        else if (ev.getType() == TYPE.ACTIVATED) {
            DefaultPanelSite panelSite = (DefaultPanelSite)ev.getSource().getSite();
            mainWindow.setText( panelSite.getTitle() );        
            browserHistory.pushState( panel.id().id(), StringUtils.abbreviate( panelSite.getTitle(), 25 ) );
            delayedRefresh();
        }
        //
        else if (ev.getType() == TYPE.DEACTIVATING) {
//            if (panelsArea.hasPage( panel.id() )) {
//                panelsArea.hidePage( panel.id() );
//            }
        }
        else if (ev.getType() == TYPE.DISPOSING) {
            if (panelsArea.hasPage( panel.id() )) {
                panelsArea.removePage( panel.id() );
            }
        }
    }


    @Override
    public LayoutSupplier getAppLayoutSettings() {
        return appLayoutSettings;
    }


    @Override
    public LayoutSupplier getPanelLayoutPreferences() {
        return panelLayoutSettings;
    }


    protected Layout newPanelLayout() {
        // 1000px display width -> 30px margin
//        int margins = (int)(UIUtils.sessionDisplay().getBounds().width * 0.03 );
//        ConstraintLayout result = new ConstraintLayout( margins, margins, margins );
        ConstraintLayout result = new ConstraintLayout( getPanelLayoutPreferences() );
        return result;
    }
    

    /**
     * Sets/adapts the {@link #mainWindow} layout rigth after init and before
     * {@link #delayedRefresh()}. Override to change behaviour.
     */
    protected void adjustLayout() {
        Rectangle displayArea = Display.getCurrent().getBounds();

        int marginsWidth = -1;
        int spacing = -1;
        if (displayArea.width < 500) {
            marginsWidth = spacing = 5;
        }
        else if (displayArea.width < 1200) {
            marginsWidth = spacing = (int)(displayArea.width * 0.025);
        }
        else {
            marginsWidth = (int)(displayArea.width * 0.025) + 100;
            spacing = (int)(displayArea.width * 0.025);
        }
        log.info( "adjustLayout(): display width=" + displayArea.width + " -> spacing=" + spacing );
        
        // panel layout
        panelLayoutSettings.spacing = spacing;
        
        // app layout
        appLayoutSettings.spacing = spacing;
        appLayoutSettings.marginLeft = appLayoutSettings.marginRight = marginsWidth;
        
        mainWindow.setLayout( FormLayoutFactory.defaults().margins( 
                appLayoutSettings.marginTop, appLayoutSettings.marginRight, 
                appLayoutSettings.marginBottom, appLayoutSettings.marginLeft ).create() );
        
        // propagate settings to PageStackLayout
        mainWindow.layout( true );
    }
    
    
    @Override
    public void delayedRefresh() {
        adjustLayout();
        
        // XXX this forces the content send twice to the client (measureString: calculate text height)
        // without layout fails sometimes (page to short, no content at all)
//        s.layout( true );
//        panelsArea.reflow( true );
        
        // FIXME HACK! force re-layout after font sizes are known (?)
        UIUtils.activateCallback( DefaultAppDesign.class.getName() );
        mainWindow.getDisplay().timerExec( 1000, new Runnable() {
            public void run() {
                log.info( "layout..." );

                mainWindow.layout( true );
//                panelsArea.reflow( true );
                //((Composite)scrolled.getCurrentPage()).layout();
                
                UIUtils.deactivateCallback( DefaultAppDesign.class.getName() );
            }
        });
    }


    /**
     * 
     */
    public static class DefaultLayoutSupplier
            extends LayoutSupplier {
        
        public int marginLeft, marginRight, marginTop, marginBottom, spacing;
    
        @Override
        public int getMarginLeft() {
            return marginLeft;
        }
        @Override
        public int getMarginRight() {
            return marginRight;
        }
        @Override
        public int getMarginTop() {
            return marginTop;
        }
        @Override
        public int getMarginBottom() {
            return marginBottom;
        }
        @Override
        public int getSpacing() {
            return spacing;
        }
    }
    
    
    /**
     * 
     */
    public static class DelegatingLayoutSupplier
            extends LayoutSupplier {
        
        private LayoutSupplier          delegate;

        public DelegatingLayoutSupplier( LayoutSupplier delegate ) {
            this.delegate = delegate;
        }
        @Override
        public int getMarginLeft() {
            return delegate.getMarginLeft();
        }
        @Override
        public int getMarginRight() {
            return delegate.getMarginRight();
        }
        @Override
        public int getMarginTop() {
            return delegate.getMarginTop();
        }
        @Override
        public int getMarginBottom() {
            return delegate.getMarginBottom();
        }
        @Override
        public int getSpacing() {
            return delegate.getSpacing();
        }
        
    }
    
}
