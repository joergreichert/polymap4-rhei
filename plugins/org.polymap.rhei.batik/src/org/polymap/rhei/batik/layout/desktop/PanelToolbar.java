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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.FormDataFactory;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.TYPE;
import org.polymap.rhei.batik.layout.desktop.DesktopAppManager.DesktopPanelSite;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PanelToolbar
        extends ContributionItem {

    private static Log log = LogFactory.getLog( PanelToolbar.class );

    private DesktopAppManager           appManager;

    private Composite                   contents;

    private List<PanelChangeEvent>      pendingStartEvents = new ArrayList();
    

    public PanelToolbar( DesktopAppManager appManager ) {
        this.appManager = appManager;

        appManager.getContext().addEventHandler( this, new EventFilter<PanelChangeEvent>() {
            public boolean apply( PanelChangeEvent input ) {
                return true; //input.getType() == TYPE.ACTIVATED;
            }
        });
    }


    @Override
    public void fill( Composite parent ) {
        contents = parent;
        contents.setLayoutData( FormDataFactory.filled().create() );
        contents.setLayout( RowLayoutFactory.fillDefaults().margins( 0, 0 ).fill( false ).create() );

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
        // close
        if (ev.getType() == TYPE.DEACTIVATED) {
            for (Control child : contents.getChildren()) {
                child.dispose();
            }
        }
        // open
        if (ev.getType() == TYPE.ACTIVATED) {
            DesktopPanelSite panelSite = (DesktopPanelSite)ev.getSource().getSite();
            for (Object tool : panelSite.getTools()) {
                Button btn = null;

                // IAction
                if (tool instanceof IAction) {
                    final IAction action = (IAction)tool;
                    switch (action.getStyle()) {
                        case IAction.AS_CHECK_BOX:
                            btn = new Button( contents, SWT.CHECK );
                            break;
                        default:
                            btn = new Button( contents, SWT.PUSH );
                    }
                    btn.setLayoutData( RowDataFactory.swtDefaults().hint( SWT.DEFAULT, 28 ).create() );
                    btn.setData( WidgetUtil.CUSTOM_VARIANT, "atlas-toolbar"  );
                    btn.setData( "source", tool );
                    if (action.getText() != null) {
                        btn.setText( action.getText() );
                    }
                    if (action.getToolTipText() != null) {
                        btn.setToolTipText( action.getToolTipText() );
                    }
                    ImageDescriptor image = action.getImageDescriptor();
                    if (image != null) {
                        btn.setImage( BatikPlugin.instance().imageForDescriptor( image, action.getText() + "_icon" ) );
                    }
                    btn.addSelectionListener( new SelectionAdapter() {
                        public void widgetSelected( SelectionEvent se ) {
                            action.run();
                        }
                    });
                }
                else {
                    throw new RuntimeException( "Panel toolbar item type: " + tool );
                }
            }
            contents.layout();
//            contents.layout( true );
//            contents.getParent().layout( true );
//            Polymap.getSessionDisplay().asyncExec( new Runnable() {
//                public void run() {
//                    contents.layout( true );
//                }
//            });

        }
    }

}
