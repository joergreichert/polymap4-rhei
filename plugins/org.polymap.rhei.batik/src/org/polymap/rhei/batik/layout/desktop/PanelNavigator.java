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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.FormDataFactory;

import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.TYPE;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.Panels;
import org.polymap.rhei.batik.app.BatikApplication;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PanelNavigator
        extends ContributionItem {

    private static Log log = LogFactory.getLog( PanelNavigator.class );

    private DesktopAppManager         appManager;

    private Composite                 contents;

    private List<PanelChangeEvent>    pendingStartEvents = new ArrayList();

    private Composite                 breadcrumb;

    private IPanel                    activePanel;


    public PanelNavigator( DesktopAppManager appManager ) {
        this.appManager = appManager;

        appManager.getContext().addEventHandler( this, new EventFilter<PanelChangeEvent>() {
            public boolean apply( PanelChangeEvent input ) {
                return input.getType() == TYPE.ACTIVATED || input.getType() == TYPE.ACTIVATED;
            }
        });
    }


    @Override
    public void fill( Composite parent ) {
        this.contents = parent;
        contents.setLayout( new FormLayout() );

        // breadcrumb
        breadcrumb = new Composite( parent, SWT.NONE );
        breadcrumb.setLayoutData( FormDataFactory.filled().create() );
        breadcrumb.setLayout( RowLayoutFactory.fillDefaults().margins( 0, 0 ).fill( false ).create() );

        // fire pending events
        for (PanelChangeEvent ev : pendingStartEvents) {
            panelChanged( ev );
        }
        pendingStartEvents.clear();
    }


    protected void updateBreadcrumb() {
        // when called from DesktopPanelSite
        if (activePanel == null) {
            return;
        }
        // clear
        for (Control control : breadcrumb.getChildren()) {
            control.dispose();
        }
//        // home
//        Button homeBtn = new Button( breadcrumb, SWT.PUSH );
//        homeBtn.setData( WidgetUtil.CUSTOM_VARIANT, "atlas-navi"  );
//        homeBtn.setImage( BatikPlugin.instance().imageForName( "resources/icons/house.png" ) );
//        homeBtn.setToolTipText( "Zurück zur Startseite" );
//        homeBtn.setLayoutData( RowDataFactory.swtDefaults().hint( SWT.DEFAULT, 28 ).create() );
//        homeBtn.addSelectionListener( new SelectionAdapter() {
//            public void widgetSelected( SelectionEvent e ) {
//                while (activePanel.getSite().getPath().size() > 1) {
//                    appManager.closePanel();
//                    activePanel = appManager.getActivePanel();
//                }
//            }
//        });
        
        boolean showText = BatikApplication.sessionDisplay().getClientArea().width > 900;

        // path
        PanelPath path = activePanel.getSite().getPath().removeLast( 1 );
        for (int i=1; i<=path.size(); i++) {
            final IPanel panel = appManager.getContext().getPanel( path.prefix( i ) );

            Button btn = new Button( breadcrumb, SWT.PUSH );
            btn.setData( WidgetUtil.CUSTOM_VARIANT, "atlas-navi"  );
            btn.setLayoutData( RowDataFactory.swtDefaults().hint( SWT.DEFAULT, 28 ).create() );
            Image icon = panel.getSite().getIcon();
            if (showText || icon == null) {
                btn.setText( panel.getSite().getTitle() );
            }
            else {
                btn.setToolTipText( panel.getSite().getTitle() );
            }
            if (icon != null) {
                btn.setImage( icon );
            }

            btn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent e ) {
                    while (!activePanel.equals( panel )) {
                        appManager.closePanel( null );
                        activePanel = appManager.getActivePanel();
                    }
                }
            });

//            Label separator = new Label( breadcrumb, SWT.VERTICAL );
//            separator.setText( " " );
//            separator.setData( WidgetUtil.CUSTOM_VARIANT, "atlas-navi"  );
        }
        
        // switcher
        PanelPath prefix = activePanel.getSite().getPath().removeLast( 1 );
        for (final IPanel panel : appManager.getContext().findPanels( Panels.withPrefix( prefix ) )) {
            final Button btn = new Button( breadcrumb, SWT.TOGGLE );
            btn.setData( WidgetUtil.CUSTOM_VARIANT, "atlas-navi"  );
            Image icon = panel.getSite().getIcon();
            String title = panel.getSite().getTitle();
            
            if (icon == null && title == null) {
                btn.setVisible( false );
            }
            else if (showText || icon == null) {
                btn.setText( panel.getSite().getTitle() );
            }
            else {
                btn.setToolTipText( panel.getSite().getTitle() );
            }
            if (icon != null) {
                btn.setImage( icon );
            }
            btn.setLayoutData( RowDataFactory.swtDefaults().hint( SWT.DEFAULT, 28 ).create() );
            
            if (panel.equals( activePanel )) {
                btn.setSelection( true );
                btn.addSelectionListener( new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent ev ) {
                        btn.setSelection( true );
                    }
                });
            }
            else {
                btn.addSelectionListener( new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent ev ) {
                        appManager.activatePanel( panel.id() );
                    }
                });
            }
        }
        
        breadcrumb.layout( true );
        contents.layout( true );
    }


    @EventHandler(display=true)
    protected void panelChanged( PanelChangeEvent ev ) {
        if (contents == null) {
            pendingStartEvents.add( ev );
            return;
        }
        // open
        if (ev.getType() == TYPE.ACTIVATED) {
            activePanel = ev.getSource();
            updateBreadcrumb();
        }
    }

}
