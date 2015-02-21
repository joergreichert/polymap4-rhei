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
package org.polymap.rhei.batik.dashboard;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.polymap.core.ui.FormDataFactory;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelIdentifier;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DashboardPanel
        extends DefaultPanel
        implements IPanel {

    public static final PanelIdentifier     ID = new PanelIdentifier( "dashboard" );


    @Override
    public void init() {
        getSite().setTitle( "Dashboard" );

//        // info action
//        Image icon = JFaceResources.getImage( Dialog.DLG_IMG_MESSAGE_INFO );
//        Action infoAction = new Action( "Info" ) {
//            public void run() {
//                MessageDialog.openInformation( UIUtils.shellToParentOn(),
//                        "Information", "Atlas Client Version: " + BatikPlugin.instance().getBundle().getVersion() );
//            }
//        };
//        infoAction.setImageDescriptor( ImageDescriptor.createFromImage( icon ) );
//        infoAction.setToolTipText( "Version Information" );
//        site.addToolbarAction( infoAction );
    }


    @Override
    public PanelIdentifier id() {
        return ID;
    }


    @Override
    public void createContents( Composite parent ) {
        Composite contents = getSite().toolkit().createComposite( parent );
        contents.setLayout( new FormLayout() );

        Label l = getSite().toolkit().createLabel( contents, "Dashboard!" );
        l.setLayoutData( FormDataFactory.filled().create() );
    }

}
