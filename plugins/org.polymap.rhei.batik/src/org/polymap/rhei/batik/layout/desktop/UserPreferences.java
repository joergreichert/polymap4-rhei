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
package org.polymap.rhei.batik.layout.desktop;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.app.BatikApplication;
import org.polymap.rhei.batik.app.LogoutAction;
import org.polymap.rhei.batik.internal.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class UserPreferences
        extends ContributionItem {

    private static Log log = LogFactory.getLog( UserPreferences.class );
    
    private static final IMessages      i18n = Messages.forPrefix( "UserPreferences" );
    
    private DesktopAppManager   appManager;

    private Composite           contents;
    
    private String              username;
    
    /** Shows user name in the top bar. Only initialized if wide enough.*/
    private Button              usernameLnk;

    private Button              btn;
    
    private Set<IAction>        menuContributions = new HashSet();

    
    public UserPreferences( DesktopAppManager appManager ) {
        this.appManager = appManager;
    }

    
    public String getUsername() {
        return username;
    }

    
    public void setUsername( String username ) {
        this.username = username;
        if (usernameLnk != null) {
            usernameLnk.setText( " " + username );
            usernameLnk.setToolTipText( i18n.get( "userTip", username ) );
            if (username.toLowerCase().contains( "admin" )) {
                usernameLnk.setText( "Administrator" );
                usernameLnk.setForeground( Graphics.getColor( 0xff, 0x30, 0x30 ) );
            }
        }
    }


    public void addMenuContribution( IAction action ) {
        menuContributions.add( action );
    }


    @Override
    public void fill( Composite parent ) {
        contents = parent;
        contents.setLayout( FormLayoutFactory.defaults().create() );
        
        btn = new Button( parent, SWT.PUSH );
        btn.setLayoutData( FormDataFactory.filled().left( 100, -50 ).create() );
        btn.setData( WidgetUtil.CUSTOM_VARIANT, "atlas-navi"  );
        btn.setImage( BatikPlugin.instance().imageForName( "resources/icons/cog.png" ) );
        btn.setToolTipText( i18n.get( "menuTip" ) );
        
        btn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                openMenu( ev );
            }
        });

        if (BatikApplication.sessionDisplay().getClientArea().width >= 900) {
            usernameLnk = new Button( contents, SWT.PUSH | SWT.LEFT );
            usernameLnk.setLayoutData( FormDataFactory.filled().right( btn ).width( 160 ).create() );
            usernameLnk.setData( WidgetUtil.CUSTOM_VARIANT, "atlas-navi"  );
            usernameLnk.setText( " [" + i18n.get( "noUser" ) + "]" );
            usernameLnk.setImage( BatikPlugin.instance().imageForName( "resources/icons/user.png" ) );
            
            // FIXME
            usernameLnk.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent e ) {
                    appManager.getContext().openPanel( new PanelIdentifier( "azvlogin" ) );
                }
            });
        }
    }


    protected void openMenu( SelectionEvent ev ) {
        Menu menu = new Menu( contents );

        if (usernameLnk == null && username != null) {
            addAction( menu, new UsernameAction() );
        }
        for (final IAction action : menuContributions) {
            addAction( menu, action );
        }
        addAction( menu, new LogoutAction() );
        
        menu.setLocation( btn.toDisplay( 0, 30 ) );
        menu.setVisible( true );
    }


    protected void addAction( Menu menu, final IAction action ) {
        MenuItem item = new MenuItem( menu, SWT.PUSH );
        item.setText( action.getText() );
        ImageDescriptor icon = action.getImageDescriptor();
        if (icon != null) {
            item.setImage( BatikPlugin.instance().imageForDescriptor( icon, action.getText() ) );
        }
        item.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev2 ) {
                action.run();
            }
        });
    }


    /**
     * 
     */
    public class UsernameAction
            extends Action {

        public UsernameAction() {
            super( username );
            setToolTipText( i18n.get( "userTip", username ) );
            setImageDescriptor( BatikPlugin.imageDescriptorFromPlugin( BatikPlugin.PLUGIN_ID, "resources/icons/user.png" ) );
        }


        @Override
        public void run() {
        }
        
    }

}
