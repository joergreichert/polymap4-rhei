/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.batik.internal.desktop;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolTip;

import org.eclipse.jface.action.ContributionItem;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.TYPE;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class StatusManager
        extends ContributionItem {
        //implements IStatusLineManager {

    private static Log log = LogFactory.getLog( StatusManager.class );
    
    private DesktopAppManager       appManager;

    private Composite               contents;

    private List<PanelChangeEvent>  pendingStartEvents = new ArrayList();

    private IPanel                  activePanel;

    private Label                   iconLabel;
    
    private ToolTip                 tip;


    public StatusManager( DesktopAppManager appManager ) {
        this.appManager = appManager;

        appManager.getContext().addEventHandler( this, new EventFilter<PanelChangeEvent>() {
            public boolean apply( PanelChangeEvent input ) {
                return input.getType() == TYPE.ACTIVATED || input.getType() == TYPE.STATUS;
            }
        });
    }

    
    @Override
    public void fill( Composite parent ) {
        this.contents = parent;
        contents.setLayout( FormLayoutFactory.defaults().margins( 0, 3 ).create() );

        iconLabel = new Label( contents, SWT.NONE );
        iconLabel.setLayoutData( FormDataFactory.filled().top( 0, 2 ).right( -1 ).width( 25 ).create() );
        iconLabel.setText( "..." );

        Label sep2 = new Label( contents, SWT.SEPARATOR | SWT.VERTICAL );
        sep2.setLayoutData( FormDataFactory.filled().left( -1 ).create() );
        
        // fire pending events
        for (PanelChangeEvent ev : pendingStartEvents) {
            panelChanged( ev );
        }
        pendingStartEvents.clear();
    }


    @EventHandler(display=true)
    protected void panelChanged( PanelChangeEvent ev ) {
        if (contents == null) {
            pendingStartEvents.add( ev );
            return;
        }

        if (ev.getType() == TYPE.ACTIVATED) {
            activePanel = ev.getSource();
            IStatus status = activePanel.getSite().getStatus();
            // restore status only if it is not 'OK'
            update( status.isOK() ? null : status, false );
        }
        else if (ev.getType() == TYPE.STATUS) {
            update( activePanel.getSite().getStatus(), true );
        }
    }


//    public IProgressMonitor getProgressMonitor() {
//        // XXX Auto-generated method stub
//        throw new RuntimeException( "not yet implemented." );
//    }
//
//    public boolean isCancelEnabled() {
//        // XXX Auto-generated method stub
//        throw new RuntimeException( "not yet implemented." );
//    }
//
//    public void setCancelEnabled( boolean enabled ) {
//        // XXX Auto-generated method stub
//        throw new RuntimeException( "not yet implemented." );
//    }


    protected void update( IStatus status, boolean popup ) {
        iconLabel.setImage( null );
        iconLabel.setToolTipText( null );
        
        if (status != null && status != Status.OK_STATUS) {
            // init tip after contents was layouted
            if (tip == null) {
                // http://hnvcam.blogspot.de/2010/04/swt-create-fake-tooltip.html
                tip = new ToolTip( contents.getShell(), SWT.BALLOON /*SWT.BALLOON | SWT.ICON_ERROR*/ );
                Point iconLocation = iconLabel.getParent().getLocation();
                tip.setLocation( iconLocation.x, iconLocation.y + 90 );
            }

            iconLabel.setToolTipText( status.getMessage() );
            tip.setMessage( status.getMessage() );

            switch (status.getSeverity()) {
                case IStatus.OK: {
                    if (status != Status.OK_STATUS && status.getMessage() != null) {
                        iconLabel.setImage( BatikPlugin.instance().imageForName( "resources/icons/ok-status.gif" ) );
                    }
                    tip.setText( "Aktion war erfolgreich" );
                    break;
                }
                case IStatus.ERROR: {
                    iconLabel.setImage( BatikPlugin.instance().imageForName( "resources/icons/errorstate.gif" ) );
                    tip.setText( "Ein Problem ist aufgetreten" );
                    break;
                }
                case IStatus.WARNING: {
                    iconLabel.setImage( BatikPlugin.instance().imageForName( "resources/icons/warningstate.gif" ) );
                    tip.setText( "Achtung" );
                    break;
                }
                case IStatus.INFO: {
                    iconLabel.setImage( BatikPlugin.instance().imageForName( "resources/icons/info.png" ) );
                    tip.setText( "Hinweis" );
                    break;
                }
            }

            if (popup) {
                tip.setVisible( true );
            }
        }
    }

}
