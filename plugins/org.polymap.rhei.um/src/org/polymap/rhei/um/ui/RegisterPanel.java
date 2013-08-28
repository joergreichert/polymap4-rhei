/* 
 * polymap.org
 * Copyright (C) 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.um.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.security.UserPrincipal;
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RegisterPanel
        extends DefaultPanel
        implements IPanel, IFormFieldListener {

    private static Log log = LogFactory.getLog( RegisterPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "um", "register" );

    private ContextProperty<UserPrincipal> userPrincipal;

    private IPanelToolkit               tk;
    
    private Button                      okBtn;

    private PersonForm                  personForm;
    

    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        tk = site.toolkit();
        return false;
    }


    @Override
    public void dispose() {
        if (personForm != null) {
            personForm.removeFieldListener( this );
            personForm = null;
        }
    }


    @Override
    public PanelIdentifier id() {
        return ID;
    }


    @Override
    public void createContents( Composite panelBody ) {
        getSite().setTitle( "Registrieren" );
        IPanelSection contents = tk.createPanelSection( panelBody, null );
        IPanelSection personSection = tk.createPanelSection( contents, "Nutzer anlegen" );

        Composite body = personSection.getBody();
        body.setLayout( ColumnLayoutFactory.defaults().spacing( 10 ).margins( 20, 20 ).create() );

        final User user = UserRepository.instance().newUser();
        
        personForm = new PersonForm( user );
        personForm.createContents( personSection );

        // btn
        okBtn = tk.createButton( body, "REGISTRIEREN", SWT.PUSH );
        okBtn.setEnabled( false );
        okBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    // create user
                    personForm.submit();
                    UserRepository.instance().commitChanges();

                    // FIXME login
//                    Polymap.instance().login( user.email().get(), passwd );
//                    userPrincipal.set( (UserPrincipal)Polymap.instance().getUser() );
                    
                    getContext().closePanel();
                }
                catch (Exception e) {
                    UserRepository.instance().revertChanges();
                    throw new RuntimeException( e );
                }
            }
        });
        
        personForm.addFieldListener( this );
    }

    
    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE) {
            okBtn.setEnabled( personForm.isValid() );
        }
    }

}
