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

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static org.polymap.rhei.batik.IPanelSite.PanelStatus.VISIBLE;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.layout.RowDataFactory;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.rap.rwt.client.service.BrowserNavigationEvent;
import org.eclipse.rap.rwt.client.service.BrowserNavigationListener;

import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.IPanelSite.PanelStatus;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.EventType;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.Panels;
import org.polymap.rhei.batik.app.DefaultToolkit;
import org.polymap.rhei.batik.app.IAppDesign;
import org.polymap.rhei.batik.app.IAppManager;
import org.polymap.rhei.batik.engine.PageStack.Page;
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

    public static final String          CSS_PREFIX = "atlas-panel";
    public static final String          CSS_PANEL_HEADER = CSS_PREFIX + "-header";
    public static final String          CSS_BREADCRUMP = CSS_PREFIX + "-breadcrump";
    public static final String          CSS_SWITCHER = CSS_PREFIX + "-switcher";
    
    private IAppManager                 appManager;

    protected Shell                     mainWindow;
    
    protected BrowserNavigation         browserHistory;

    protected StatusManager2            statusManager;

    protected DefaultUserPreferences    userPrefs;

    protected PageStack<PanelPath>      panelsArea;
    
    protected DefaultLayoutSupplier     panelLayoutSettings = new DefaultLayoutSupplier();
 
    protected DefaultLayoutSupplier     appLayoutSettings = new DefaultLayoutSupplier();


    @Override
    public void init() {
        appManager = BatikApplication.instance().getAppManager();

        browserHistory = RWT.getClient().getService( BrowserNavigation.class );
        browserHistory.addBrowserNavigationListener( this );
    }


    @Override
    public void close() {
        if (browserHistory != null) {
            browserHistory.removeBrowserNavigationListener( this );
            browserHistory = null;
        }
        appManager.getContext().removeListener( this );
    }


    /** 
     * Browser history event. 
     */
    @Override
    public void navigated( BrowserNavigationEvent ev ) {
        log.info( "navigated(): " + ev.getState() );
        if (!ev.getState().equals( "start" )) {
            IAppContext context = appManager.getContext();
            context.openPanel( PanelPath.ROOT, new PanelIdentifier( "start" ) );
        }
    }


    @Override
    public IPanelToolkit createToolkit( PanelPath panelPath ) {
        return new DefaultToolkit( panelPath );
    }


    @Override
    public Shell createMainWindow( Display display ) {
        mainWindow = new Shell( display, SWT.NO_TRIM );
        mainWindow.setMaximized( true );
        UIUtils.setVariant( mainWindow, IAppDesign.CSS_SHELL );

        updateMainWindowLayout();
        
        mainWindow.addControlListener( new ControlAdapter() {
            private Rectangle lastDisplayArea = display.getBounds();
            @Override
            public void controlResized( ControlEvent ev ) {
                Rectangle displayArea = display.getBounds();
                if (!displayArea.equals( lastDisplayArea )) {
                    lastDisplayArea = displayArea;
                    updateMainWindowLayout();
                }
            }
        });
        
        // header
        Composite headerContainer = fillHeaderArea( mainWindow );
        headerContainer.setLayoutData( FormDataFactory.filled().clearBottom().create() );

        // panels
        Composite panelContainer = fillPanelArea( mainWindow );
        panelContainer.setLayoutData( FormDataFactory.filled().top( headerContainer, 0 ).create() );

        // status manager
        statusManager = new StatusManager2( (DefaultAppManager)appManager );
        statusManager.createContents( mainWindow );
        
        appManager.getContext().addListener( this, ev -> ev.getType().isOnOf( EventType.LIFECYCLE, EventType.TITLE ) );
        
        mainWindow.open();
        return mainWindow;
    }


    protected Composite fillHeaderArea( Composite parent ) {
        Composite result = new Composite( parent, SWT.NO_FOCUS | SWT.BORDER );
        UIUtils.setVariant( result, IAppDesign.CSS_HEADER );
        result.setLayout( FormLayoutFactory.defaults().margins( 0, 0 ).create() );
        Label l = new Label( result, SWT.NONE );
        UIUtils.setVariant( l, IAppDesign.CSS_HEADER );
        l.setText( "mapzone" );
        return result;
    }


    protected Composite fillPanelArea( Composite parent ) {
        // layout supplier
        DelegatingLayoutSupplier ls = new DelegatingLayoutSupplier( getAppLayoutSettings() ) {
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
        };

        // panelsArea
        panelsArea = new PageStack<PanelPath>( parent, ls ) {
            
            private Set<Page>       previousShown;
            
            @Override
            protected void preUpdateLayout() {
                previousShown = getPages().stream()
                        .filter( page -> page.isShown )
                        .collect( Collectors.toSet() );
                log.debug( "previousShown: " + previousShown );
            }
            
            @Override
            protected void postUpdateLayout() {
                log.debug( "now pages: " + panelsArea.getPages() );
                // update panel status according to isShown state
                getPages().stream()
                        .filter( page -> page.isShown != previousShown.contains( page ) )
                        .forEach( page -> {
                            PanelPath panelPath = page.key;
                            IPanel p = appManager.getContext().getPanel( panelPath );
                            PanelStatus panelStatus = p.getSite().getPanelStatus();
                            
                            // new or focused pages are explicitly set isShown=true, they already have proper
                            // PanelStatus (>= VISIBLE); however, layout may decide to show pages when more space
                            // becomes available, those panels have status < VISIBLE and need to be updated
                            if (page.isShown && VISIBLE.ge( panelStatus )) {
                                ((DefaultAppManager)appManager).updatePanelStatus( p, VISIBLE );                                 
                            }
                            // XXX
//                            if (!page.isShown && panelStatus.ge( VISIBLE )) {
//                                ((DefaultAppManager)appManager).updatePanelStatus( p, INITIALIZED );                                
//                            }
                        });
            }
        };
        panelsArea.showEmptyPage();
        return UIUtils.setVariant( panelsArea, CSS_PANELS );
    }

    
    @Override
    public LayoutSupplier getAppLayoutSettings() {
        return appLayoutSettings;
    }


    @Override
    public LayoutSupplier getPanelLayoutPreferences() {
        return panelLayoutSettings;
    }

    
    /**
     * Creates the contents of panel, including head and other decorations. Override
     * to change behaviour.
     */
    protected void createPanelContents( final IPanel panel, final Composite parent ) {
        parent.setLayout( FormLayoutFactory.defaults().create() );
        UIUtils.setVariant( parent, CSS_PANEL );
        
        // head
        Composite head = UIUtils.setVariant( new Composite( parent, SWT.BORDER | SWT.NO_FOCUS ), CSS_PANEL_HEADER );
        head.setLayoutData( FormDataFactory.filled().clearBottom().height( 28 ).create() );
        head.setLayout( FormLayoutFactory.defaults().margins( 2 ).spacing( 2 ).create() );

        // decoration
        createPanelDecoration( panel, head );
      
        // title
        Label title = UIUtils.setVariant( new Label( head, SWT.NO_FOCUS|SWT.CENTER ), CSS_PANEL_HEADER );
        title.setData( "_type_", CSS_PANEL_HEADER );
        title.setText( Optional.ofNullable( panel.getSite().getTitle() ).orElse( "..." ) );
        title.setLayoutData( FormDataFactory.filled()/*.left( center, 0, Alignment.CENTER )*/.top( 0, 4 ).create() );
        
        // panel
        Composite panelParent = new Composite( parent, SWT.NO_FOCUS );
        panelParent.setLayoutData( FormDataFactory.filled().top( 0, 35 ).create() );
        panelParent.setLayout( new ConstraintLayout( getPanelLayoutPreferences() ) );
        panel.createContents( panelParent );
    }
    
    
    protected void createPanelDecoration( IPanel panel, Composite head  ) {
        // close btn
        if (panel.getSite().getPath().size() > 1) {
            Button closeBtn = UIUtils.setVariant( new Button( head, SWT.NO_FOCUS ), CSS_PANEL_HEADER );
            closeBtn.setText( "x" );
            //closeBtn.setImage( BatikPlugin.instance().imageForName( "resources/icons/close3.gif" ) );
            closeBtn.setToolTipText( "Dieses Panel schließen" );
            closeBtn.setLayoutData( FormDataFactory.filled().clearRight().width( 20 ).create() );
            closeBtn.addSelectionListener( new SelectionAdapter() {
                @Override
                public void widgetSelected( SelectionEvent ev ) {
                    appManager.closePanel( panel.getSite().getPath() );
                }
            });
        }
        
        // switcher
        Composite switcher = new Composite( head, SWT.NONE );
//        switcher.setLayout( RowLayoutFactory.fillDefaults().margins( 1, 1 ).spacing( 5 ).fill( false ).create() );
        switcher.setLayout( FormLayoutFactory.defaults().spacing( 15 ).margins( 1, 1 ).create() );
        UIUtils.setVariant( switcher, CSS_SWITCHER );

        PanelPath prefix = panel.getSite().getPath().removeLast( 1 );
        appManager.getContext()
                .findPanels( Panels.withPrefix( prefix ) ).stream()
                .sorted( reverseOrder( comparing( p -> p.getSite().getStackPriority() ) ) )
                .forEach( p -> {
                    IPanelSite panelSite = p.getSite();
                        
                    int btnCount = switcher.getChildren().length;
                    Button btn = createSwitcherButton( switcher, p );
                    btn.setLayoutData( btnCount == 0
                            ? FormDataFactory.filled().clearRight().create()
                            : FormDataFactory.filled().clearRight().left( switcher.getChildren()[btnCount-1] ).create() );
  
                    btn.setSelection( panelSite.getPanelStatus().ge( PanelStatus.VISIBLE ) );

                    btn.addSelectionListener( new SelectionAdapter() {
                        public void widgetSelected( SelectionEvent ev ) {
                            appManager.openPanel( p.id() );
                        }
                    });
                });

        Point size = switcher.computeSize( SWT.DEFAULT, 30, true );
        switcher.setLayoutData( FormDataFactory.filled().clearLeft().width( size.x ).create() );
    }

    
    protected Button createSwitcherButton( Composite switcher, IPanel panel ) {
        final Button btn = UIUtils.setVariant( new Button( switcher, SWT.PUSH ), CSS_PANEL_HEADER );
        btn.setLayoutData( RowDataFactory.swtDefaults().hint( SWT.DEFAULT, 21 ).create() );

        boolean showText = UIUtils.sessionDisplay().getClientArea().width > 900;
        
        // update title/icon
        Image icon = panel.getSite().getIcon();
        String title = panel.getSite().getTitle();
        
        if (icon == null && title == null) {
            btn.setVisible( false );
        }
        else if (showText || icon == null) {
            btn.setText( title + " >" );
        }
        else {
            btn.setToolTipText( title );
        }
        if (icon != null) {
            btn.setImage( icon );
        }
        return btn;
    }
    
    
    /**
     * Updates the contents of a panel, including head, after
     * {@link PanelChangeEvent}. Override to change behaviour.
     */
    protected void updatePanelContents( IPanel panel ) {
        Page page = panelsArea.getPage( panel.getSite().getPath() );
        UIUtils.visitChildren( page.control, child -> {
            if (CSS_PANEL_HEADER.equals( child.getData( "_type_" ) )) {
                ((Label)child).setText( Optional.ofNullable( panel.getSite().getTitle() ).orElse( "" ) );
                return false;
            }
            else {
                return true;
            }
        });
    }

    
    /**
     * Sets/adapts the {@link #mainWindow} layout right after init and before
     * {@link #delayedRefresh()}. Override to change behaviour.
     */
    protected void updateMainWindowLayout() {
        Rectangle displayArea = Display.getCurrent().getBounds();

        int marginsWidth = -1;
        int spacing = -1;
        if (displayArea.width < 500) {
            marginsWidth = spacing = 5;
        }
        else if (displayArea.width < 1366) { // many current notebook displays?
            marginsWidth = spacing = (int)(displayArea.width * 0.025);
        }
        else {
            marginsWidth = (int)(displayArea.width * 0.025) + 100;
            spacing = (int)(displayArea.width * 0.025);
        }
        log.debug( "adjustLayout(): display width=" + displayArea.width + " -> spacing=" + spacing );
        
        // panel layout
        panelLayoutSettings.spacing = spacing;
        
        // app layout
        appLayoutSettings.spacing = (int)(spacing * 0.75);
        appLayoutSettings.marginLeft = appLayoutSettings.marginRight = marginsWidth;
        
        mainWindow.setLayout( FormLayoutFactory.defaults().margins( 
                appLayoutSettings.marginTop, appLayoutSettings.marginRight, 
                appLayoutSettings.marginBottom, appLayoutSettings.marginLeft ).create() );
        
        // propagate settings to PageStackLayout
        mainWindow.layout( true );
    }
    
    
    /** 
     * {@link EventType#LIFECYCLE} event.
     */
    @EventHandler(display=true)
    protected void panelChanged( PanelChangeEvent ev ) {
        IPanel panel = ev.getPanel();
        
        // lifecycle event
        if (ev.getType() == EventType.LIFECYCLE) {
            PanelPath pageId = panel.getSite().getPath();
            PanelStatus panelStatus = (PanelStatus)ev.getNewValue();  //panel.getSite().getPanelStatus();

            // update visibility of panel
            if (panelsArea.hasPage( pageId ) && panelStatus != null) {
                panelsArea.setPageVisible( pageId, panelStatus.ge( PanelStatus.VISIBLE ) );
            }
    
            // focused
            if (panelStatus == PanelStatus.FOCUSED) {
                if (panelsArea.hasPage( pageId )) {
                    panelsArea.setPageVisible( pageId, true );
//                    panelsArea.setFocusedPage( pageId );
                }
                else {
                    Composite page = panelsArea.createPage( pageId,
                            // every new panel is created on top
                            (int)System.currentTimeMillis() );
                    createPanelContents( panel, page );
                    panelsArea.setPageVisible( pageId, true );
                    panelsArea.setPagePreferredWidth( pageId, panel.getSite().getPreferredWidth() );
//                    panelsArea.setFocusedPage( pageId );
    
//                    Point panelSize = page.computeSize( SWT.DEFAULT, SWT.DEFAULT );
//                    panelsArea.setMinHeight( panelSize.y );
                }
    
                String title = panel.getSite().getTitle();
                mainWindow.setText( title != null ? title : "" );        
                browserHistory.pushState( panel.id().id(), StringUtils.abbreviate( title, 25 ) );
            }
    
            // disposed
            else if (panelStatus == null) {
                // not yet initialized panels have no page
                if (panelsArea.hasPage( pageId )) {
                    panelsArea.removePage( pageId );
                }
            }

            delayedRefresh();            
        }
        
        // title or icon changed
        else if (ev.getType() == EventType.TITLE) {
            updatePanelContents( panel );
        }
    }


    @Override
    public void delayedRefresh() {
        
        updateMainWindowLayout();
        
        // XXX this forces the content send twice to the client (measureString: calculate text height)
        // without layout fails sometimes (page to short, no content at all)
          mainWindow.layout( true );
          panelsArea.reflow( true );
        
        // FIXME HACK! force re-layout after font sizes are known (?)
        UIUtils.activateCallback( DefaultAppDesign.class.getName() );
        mainWindow.getDisplay().timerExec( 1000, new Runnable() {
            public void run() {
                log.info( "layout..." );

                mainWindow.layout( true );
                panelsArea.reflow( true );
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
