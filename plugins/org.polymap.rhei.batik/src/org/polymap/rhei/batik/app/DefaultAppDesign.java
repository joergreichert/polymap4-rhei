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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

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

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultAppDesign
        implements IAppDesign {

    private static Log log = LogFactory.getLog( DefaultAppDesign.class );

    public static final int         MAX_CONTENT_WIDTH = 1100;
    
    protected Shell                 mainWindow;
    
    protected IPanelToolkit         toolkit;

    protected StatusManager         statusManager;

    protected DefaultAppToolbar     toolbar;

    protected DefaultAppNavigator   navigator;

    protected DefaultUserPreferences  userPrefs;

    protected PageStack<PanelIdentifier> panelsArea;


    @Override
    public void init() {
        toolkit = new DefaultToolkit();
    }


    @Override
    public void close() {
        toolkit = Closer.create().closeAndNull( toolkit );
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

        Rectangle bounds = display.getBounds();
        int margins = Math.max( bounds.width - MAX_CONTENT_WIDTH, 0 );
        mainWindow.setLayout( FormLayoutFactory.defaults().margins( 0, margins/2, 10, margins/2 ).create() );

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
        panelsArea = new PageStack( parent );  //new ScrolledPageBook( parent, SWT.V_SCROLL );
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


    protected Layout newPanelLayout() {
        // 1000px display width -> 30px margin
        int margins = (int)(UIUtils.sessionDisplay().getBounds().width * 0.03 );
        ConstraintLayout result = new ConstraintLayout( margins, margins, margins );
        log.info( "display width: " + UIUtils.sessionDisplay().getBounds().width + " -> margin: " + result.marginWidth );
        return result;
    }
    

    @Override
    public void delayedRefresh() {
        // XXX this forces the content send twice to the client (measureString: calculate text height)
        // without layout fails sometimes (page to short, no content at all)
//        s.layout( true );
        panelsArea.reflow( true );
        
        // FIXME HACK! force re-layout after font sizes are known (?)
        UIUtils.activateCallback( DefaultAppDesign.class.getName() );
        mainWindow.getDisplay().timerExec( 1000, new Runnable() {
            public void run() {
                log.info( "layout..." );

//                Rectangle bounds = Display.getCurrent().getBounds();
//                int random = (refreshCount++ % 3);
//                s.setBounds( 0, 60, bounds.width, bounds.height - 60 - random );

                mainWindow.layout( true );
                panelsArea.reflow( true );
                //((Composite)scrolled.getCurrentPage()).layout();
                
                UIUtils.deactivateCallback( DefaultAppDesign.class.getName() );
            }
        });
    }

}
