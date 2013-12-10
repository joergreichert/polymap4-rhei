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

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.service.ISettingStore;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.UserPrincipal;
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.um.UmPlugin;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.internal.Messages;
import org.polymap.rhei.um.operations.ChangePasswordOperation;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UserSettingsPanel
        extends DefaultPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( UserSettingsPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "um", "userSettings" );

    public static final IMessages       i18n = Messages.forPrefix( "UserSettingsPanel" );

//    private ContextProperty<UserPrincipal> userPrincipal;

    private IPanelToolkit               tk;
    
    private Button                      okBtn;

    private PersonForm                  personForm;

    private User                        user;

    private PersonFormListener          personFormListener;

    private PasswordFormListener        pwdFormListener;

    private PasswordForm                pwdForm;

    private Button                      pwdBtn;
    

    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        tk = site.toolkit();
        // XXX sort out context property issues
        UserPrincipal loggedIn = (UserPrincipal)Polymap.instance().getUser(); //userPrincipal.get();
        user = loggedIn != null ? UserRepository.instance().findUser( loggedIn.getName() ) : null;        
        return false;
    }


    @Override
    public void dispose() {
        if (personForm != null) {
            personForm.removeFieldListener( personFormListener );
            personForm = null;
        }
    }


    @Override
    public PanelIdentifier id() {
        return ID;
    }


    @Override
    public void createContents( Composite parent ) {
        assert user != null;
        getSite().setTitle( i18n.get( "title" ) );

        // welcome section
        IPanelSection pwdSection = tk.createPanelSection( parent, i18n.get( "pwdSectionTitle" ) );
        pwdSection.getBody().setLayout( ColumnLayoutFactory.defaults().spacing( 20 ).columns( 1, 1 ).create() );
        pwdForm = new PasswordForm( getSite(), user );
        pwdForm.createContents( pwdSection );
        pwdForm.addFieldListener( pwdFormListener = new PasswordFormListener() );

        pwdBtn = tk.createButton( pwdSection.getBody(), i18n.get( "changePasswordBtn" ), SWT.PUSH );
        pwdBtn.setEnabled( false );
        pwdBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    pwdForm.submit();

                    IUndoableOperation op = new ChangePasswordOperation( user, pwdFormListener.pwd1 );
                    OperationSupport.instance().execute( op, true, false );
                    
                    pwdBtn.setEnabled( false );
                    getSite().setStatus( new Status( IStatus.OK, UmPlugin.ID, i18n.get( "passwordChanged" ) ) );

                    ISettingStore settings = RWT.getSettingStore();
                    settings.removeAttribute( "org.polymap.rhei.um.LoginForm.login" );
                    settings.removeAttribute( "org.polymap.rhei.um.LoginForm.passwd" );
                }
                catch (Exception e) {
                    throw new RuntimeException( e );
                }
            }
        });

        // person section
        IPanelSection personSection = tk.createPanelSection( parent, i18n.get( "settingsSectionTitle" ) );
        Composite body = personSection.getBody();
        body.setLayout( ColumnLayoutFactory.defaults().spacing( 10 ).columns( 1, 1 ).create() );

        personForm = new PersonForm( getSite(), user );
        personForm.createContents( personSection );
        personForm.addFieldListener( personFormListener = new PersonFormListener() );

        // btn
        okBtn = tk.createButton( body, i18n.get( "changeSettingsBtn" ), SWT.PUSH );
        okBtn.setEnabled( false );
        okBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    // create user
                    personForm.submit();
                    UserRepository.instance().commitChanges();
                    
                    okBtn.setEnabled( false );
                    getSite().setStatus( new Status( IStatus.OK, UmPlugin.ID, i18n.get( "settingsChanged" ) ) );
                }
                catch (Exception e) {
                    UserRepository.instance().revertChanges();
                    throw new RuntimeException( e );
                }
            }
        });
    }

    
    class PasswordFormListener
            implements IFormFieldListener{

        private String      pwd1, pwd2;

        @Override
        public void fieldChange( FormFieldEvent ev ) {
            if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE) {
                pwdBtn.setEnabled( true );            
                getSite().setStatus( Status.OK_STATUS );

                if (ev.getFieldName().equals( "pwd1" )) {
                    pwd1 = ev.getNewValue();
                }
                if (ev.getFieldName().equals( "pwd2" )) {
                    pwd2 = ev.getNewValue();
                }

                if (!pwdForm.isValid()) {
                    pwdBtn.setEnabled( false );
                }
                else if (!pwd1.equals( pwd2 )) {
                    pwdBtn.setEnabled( false );
                    getSite().setStatus( new Status( IStatus.ERROR, UmPlugin.ID, i18n.get( "passwordsNotEqual" ) ) );
                }
//                else if (pwd1.length() < 8) {
//                    pwdBtn.setEnabled( false );
//                    getSite().setStatus( new Status( IStatus.ERROR, UmPlugin.ID, i18n.get( "passwordToShort" ) ) );
//                }
//                else if (pwd1.contains( )length() < 8) {
//                    Pattern.compile( )
//                    pwdBtn.setEnabled( false );
//                    getSite().setStatus( new Status( IStatus.ERROR, UmPlugin.ID, i18n.get( "passwordToShort" ) ) );
//                }
            }
        }
    }

    
    class PersonFormListener
            implements IFormFieldListener{
        
        private String      email, name;

        @Override
        public void fieldChange( FormFieldEvent ev ) {
            if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE) {
                okBtn.setEnabled( false );            

                if (ev.getFieldName().equals( "email" )) {
                    email = ev.getNewValue();
                }
                if (ev.getFieldName().equals( "name" )) {
                    name = ev.getNewValue();
                }

                if (personForm.isValid()) {
                    getSite().setStatus( Status.OK_STATUS );
                    okBtn.setEnabled( true );

//                    if (UserRepository.instance().findUser( email ) == null) {
//                        okBtn.setEnabled( true );
//                    }
//                    else {
//                        getSite().setStatus( new Status( IStatus.ERROR, UmPlugin.ID, "Der Nutzername existiert bereits: " + email ) );
//                    }
                }
            }
        }
    }
    
}
