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
package org.polymap.rhei.batik.app;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;

import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.TYPE;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.Panels;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultAppNavigator 
        implements DefaultActionBar.Part {

    public static final String      CSS_PREFIX = "atlas-navi";
    public static final String      CSS_BREADCRUMP = CSS_PREFIX + "-breadcrump";
    public static final String      CSS_SWITCHER = CSS_PREFIX + "-switcher";

    private static Log log = LogFactory.getLog( DefaultAppNavigator.class );

    private IAppManager             appManager;

    private Composite               contents;

    private List<PanelChangeEvent>  pendingStartEvents = new ArrayList();

    private Composite               breadcrumb;

    private Composite               switcher;

    private IPanel                  activePanel;


    public DefaultAppNavigator() {
        appManager = BatikApplication.instance().getAppManager();
        appManager.getContext().addListener( this, ev -> ev.getType() == TYPE.ACTIVATED || ev.getType() == TYPE.TITLE );
    }


    @Override
    public void fillContents( Composite parent ) {
        this.contents = parent;
        contents.setLayout( new FormLayout() );

        // fire pending events
        for (PanelChangeEvent ev : pendingStartEvents) {
            panelChanged( ev );
        }
        pendingStartEvents.clear();
    }


    protected void updateBreadcrumb() {
        // clear
        if (breadcrumb != null) {
            breadcrumb.dispose();
        }
        breadcrumb = new Composite( contents, SWT.NONE );
        breadcrumb.setLayoutData( FormDataFactory.filled().right( 50 ).create() );
        breadcrumb.setLayout( RowLayoutFactory.fillDefaults().margins( 0, 1 ).fill( false ).create() );
        UIUtils.setVariant( breadcrumb, CSS_BREADCRUMP );
        
        boolean showText = UIUtils.sessionDisplay().getClientArea().width > 900;
        
//        // home
//        Button homeBtn = new Button( breadcrumb, SWT.PUSH );
//        UIUtils.setVariant( homeBtn, CSS_PREFIX );
//        homeBtn.setImage( BatikPlugin.instance().imageForName( "resources/icons/house.png" ) );
//        homeBtn.setToolTipText( "Zurück zur Startseite" );
//        homeBtn.setLayoutData( RowDataFactory.swtDefaults().hint( SWT.DEFAULT, 28 ).create() );
//        homeBtn.addSelectionListener( new SelectionAdapter() {
//            public void widgetSelected( SelectionEvent e ) {
//                while (activePanel.getSite().getPath().size() > 1) {
//                    appManager.closePanel( activePanel.getSite().getPath() );
//                    activePanel = appManager.getActivePanel();
//                }
//            }
//        });
//        //homeBtn.setEnabled( activePanel.getSite().getPath().size() > 1 );

        // path
        PanelPath path = activePanel.getSite().getPath().removeLast( 1 );
        for (int i=1; i<=path.size(); i++) {
            final IPanel panel = appManager.getContext().getPanel( path.prefix( i ) );

            Button btn = new Button( breadcrumb, SWT.PUSH );
            UIUtils.setVariant( btn, CSS_PREFIX );
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
    }
    
    
    protected void updateSwitcher() {    
        if (switcher != null) {
            switcher.dispose();
        }
        switcher = new Composite( contents, SWT.NONE );
        switcher.setLayout( RowLayoutFactory.fillDefaults().margins( 1, 1 ).spacing( 0 ).fill( false ).create() );
        UIUtils.setVariant( switcher, CSS_SWITCHER );
        
        boolean showText = UIUtils.sessionDisplay().getClientArea().width > 900;

        PanelPath prefix = activePanel.getSite().getPath().removeLast( 1 );
        appManager.getContext().findPanels( Panels.withPrefix( prefix ) )
                .stream()
                .sorted( reverseOrder( comparing( panel -> panel.getSite().getStackPriority() ) ) )
                .forEach( panel -> {
                        final Button btn = new Button( switcher, SWT.TOGGLE );
                        UIUtils.setVariant( btn, CSS_PREFIX );
                        Image icon = panel.getSite().getIcon();
                        String title = panel.getSite().getTitle();

                        if (icon == null && title == null) {
                            btn.setVisible( false );
                        }
                        else if (showText || icon == null) {
                            btn.setText( title );
                        }
                        else {
                            btn.setToolTipText( title );
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
                });
        
        // calculate width
        Point size = switcher.computeSize( SWT.DEFAULT, 30, true );
        switcher.setLayoutData( FormDataFactory.filled().clearLeft().width( size.x+5 ).create() );
    }


    @EventHandler(display=true)
    protected void panelChanged( PanelChangeEvent ev ) {
        if (contents == null) {
            pendingStartEvents.add( ev );
        }
        else {
            // open
            if (ev.getType() == TYPE.ACTIVATED) {
                activePanel = ev.getSource();
                if (activePanel != null) {
                    updateSwitcher();
                    updateBreadcrumb();

                    breadcrumb.layout( true );
                    switcher.layout( true );
                    contents.layout( true );
                }
            }
            // title or icon
            else if (ev.getType() == TYPE.TITLE) {
                updateSwitcher();

                switcher.layout( true );
                contents.layout( true );
            }
        }
    }

}
