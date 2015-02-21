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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.layout.RowLayoutFactory;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.TYPE;
import org.polymap.rhei.batik.app.DefaultAppManager.DefaultPanelSite;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultAppToolbar
        implements DefaultActionBar.Part {

    private static Log log = LogFactory.getLog( DefaultAppToolbar.class );

    public static final String      CSS_PREFIX = "atlas-toolbar";

    private Composite               contents;

    private List<PanelChangeEvent>  pendingStartEvents = new ArrayList();
    

    public DefaultAppToolbar() {
        IAppManager appManager = BatikApplication.instance().getAppManager();
        appManager.getContext().addListener( this, new EventFilter<PanelChangeEvent>() {
            public boolean apply( PanelChangeEvent input ) {
                return true; //input.getType() == TYPE.ACTIVATED;
            }
        });
    }


    @Override
    public void fillContents( Composite parent ) {
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
            DefaultPanelSite panelSite = (DefaultPanelSite)ev.getSource().getSite();
            throw new RuntimeException( "not implemented" );
//            for (Object tool : panelSite.getTools()) {
//                Button btn = null;
//
//                // IAction
//                if (tool instanceof IAction) {
//                    final IAction action = (IAction)tool;
//                    switch (action.getStyle()) {
//                        case IAction.AS_CHECK_BOX:
//                            btn = new Button( contents, SWT.CHECK );
//                            break;
//                        default:
//                            btn = new Button( contents, SWT.PUSH );
//                    }
//                    btn.setLayoutData( RowDataFactory.swtDefaults().hint( SWT.DEFAULT, 28 ).create() );
//                    UIUtils.setVariant( btn, CSS_PREFIX );
//                    btn.setData( "source", tool );
//                    if (action.getText() != null) {
//                        btn.setText( action.getText() );
//                    }
//                    if (action.getToolTipText() != null) {
//                        btn.setToolTipText( action.getToolTipText() );
//                    }
//                    ImageDescriptor image = action.getImageDescriptor();
//                    if (image != null) {
//                        btn.setImage( BatikPlugin.instance().imageForDescriptor( image, action.getText() + "_icon" ) );
//                    }
//                    btn.addSelectionListener( new SelectionAdapter() {
//                        public void widgetSelected( SelectionEvent se ) {
//                            action.run();
//                        }
//                    });
//                }
//                else {
//                    throw new RuntimeException( "Panel toolbar item type: " + tool );
//                }
//            }
//            contents.layout();
            
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
