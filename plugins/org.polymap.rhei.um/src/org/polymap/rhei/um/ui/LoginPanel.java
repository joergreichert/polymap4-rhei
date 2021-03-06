/* 
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ISettingStore;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.security.SecurityContext;
import org.polymap.core.security.UserPrincipal;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.LayoutSupplier;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.NotEmptyValidator;
import org.polymap.rhei.field.PlainValuePropertyAdapter;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.StringFormField.Style;
import org.polymap.rhei.form.DefaultFormPage;
import org.polymap.rhei.form.IFormPageSite;
import org.polymap.rhei.form.batik.BatikFormContainer;
import org.polymap.rhei.um.UmPlugin;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.auth.UmSecurityConfiguration;
import org.polymap.rhei.um.internal.Messages;
import org.polymap.rhei.um.operations.NewPasswordOperation;

/**
 * <p/>
 * Register {@link UmSecurityConfiguration} in order to make this work.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LoginPanel
        extends DefaultPanel {

    private static Log log = LogFactory.getLog( LoginPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "um", "login" );

    private Context<UserPrincipal> user;

    private IPanelToolkit                  tk;

    
    @Override
    public void init() {
        super.init();
        this.tk = getSite().toolkit();
    }

    
    @Override
    public void createContents( Composite panelBody ) {
        getSite().setTitle( "Login" );
        LayoutSupplier layoutPrefs = getSite().getLayoutPreference();
        panelBody.setLayout( FormLayoutFactory.defaults()
                .margins( layoutPrefs.getMarginLeft(), layoutPrefs.getMarginTop() ).create() );
        
        IPanelSection section = tk.createPanelSection( panelBody, "Anmelden" );
        
        LoginForm loginForm = new LoginForm( getContext(), getSite(), user ) {
            protected boolean login( String name, String passwd ) {
                if (super.login( name, passwd )) {
                    getContext().closePanel( getSite().getPath() );
                    return true;
                }
                else {
                    getSite().setStatus( new Status( IStatus.WARNING, UmPlugin.ID, "Nutzername oder Passwort sind nicht korrekt." ) );
                    return false;
                }
            }
            
        };
        new BatikFormContainer( loginForm ).createContents( section );
    }
    
    
    /**
     * 
     * <p/>
     * Register {@link UmSecurityConfiguration} in order to make this work.
     */
    public static class LoginForm
            extends DefaultFormPage {

        private static final IMessages          i18n = Messages.forPrefix( "LoginForm" );
        
        protected Context<UserPrincipal>        user;

        protected Button                        loginBtn;

        protected String                        username, password;

        protected boolean                       storeLogin;
        
        private IAppContext                     context;

        protected IFormPageSite                 formSite;
        
        protected IPanelSite                    panelSite;
        
        private IFormFieldListener              fieldListener;
        
        private boolean                         showRegisterLink;

        private boolean                         showStoreCheck;
        
        private boolean                         showLostLink;

        
        public LoginForm( IAppContext context, IPanelSite panelSite, Context<UserPrincipal> user ) {
            this.context = context;
            this.panelSite = panelSite;
            this.user = user;

            try {
                ISettingStore settings = RWT.getSettingStore();
                username = settings.getAttribute( "org.polymap.rhei.um.LoginForm.login" );
                password = settings.getAttribute( "org.polymap.rhei.um.LoginForm.passwd" );
                settings.removeAttribute( "org.polymap.rhei.um.LoginForm.login" );
                settings.removeAttribute( "org.polymap.rhei.um.LoginForm.passwd" );
                storeLogin = username != null;
            }
            catch (IOException e) {
                throw new RuntimeException( e );
            }
        }

        
        public LoginForm setShowRegisterLink( boolean showRegisterLink ) {
            this.showRegisterLink = showRegisterLink;
            return this;
        }
        
        public LoginForm setShowStoreCheck( boolean showStoreCheck ) {
            this.showStoreCheck = showStoreCheck;
            return this;
        }
        
        public void setShowLostLink( boolean showLostLink ) {
            this.showLostLink = showLostLink;
        }


        @Override
        public void createFormContents( final IFormPageSite site ) {
            formSite = site;
            Composite body = site.getPageBody();
            body.setLayout( ColumnLayoutFactory.defaults()
                    .spacing( 5 /*panelSite.getLayoutPreference( LAYOUT_SPACING_KEY ) / 4*/ )
                    .margins( panelSite.getLayoutPreference().getSpacing() / 2 ).create() );
            // username
            site.newFormField( new PlainValuePropertyAdapter( "username", username ) )
                    .field.put( new StringFormField() ).validator.put( new NotEmptyValidator() )
                    .label.put( i18n.get( "username" ) ).tooltip.put( i18n.get( "usernameTip" ) )
                    .create().setFocus();
            // password
            site.newFormField( new PlainValuePropertyAdapter( "password", password ) )
                    .field.put( new StringFormField( Style.PASSWORD ) )
                    .validator.put( new NotEmptyValidator() /*{
                        @Override
                        public String validate( Object passwd ) {
                            return validatePasswd( (String)passwd ) ? null : "Passwort ist nicht korrekt";
                        }
                    }*/)
                    .label.put( i18n.get( "password" ) )
                    .create();

            // store login
            if (showStoreCheck) {
                site.newFormField( new PlainValuePropertyAdapter( "store", storeLogin ) )
                        .field.put( new CheckboxFormField() )
                        .label.put( i18n.get( "storeLogin" ) ).tooltip.put( i18n.get( "storeLoginTip" ) )
                        .create();
            }
            // btn
            loginBtn = site.getToolkit().createButton( body, i18n.get( "login" ), SWT.PUSH );
            loginBtn.setEnabled( username != null );
            loginBtn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent ev ) {
                    login( username, password );
                    if (storeLogin) {
                        storeLogin( username, password );
                    }
                }
            });

            Composite links = null;
            if (showLostLink) {
                links = panelSite.toolkit().createComposite( body );
                Link lnk = panelSite.toolkit().createLink( links, i18n.get( "lost" ) );
                lnk.setToolTipText( i18n.get( "lostTip" ) );
                lnk.addSelectionListener( new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent ev ) {
                        if (username != null && username.length() > 0) {
                            sendNewPassword( username );
                        }
                    }
                });
            }

            if (showRegisterLink) {
                links = links != null ? links : panelSite.toolkit().createComposite( body );
                Link registerLnk = panelSite.toolkit().createLink( links, i18n.get( "register" ) );
                registerLnk.addSelectionListener( new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent e ) {
                        context.openPanel( panelSite.getPath(), RegisterPanel.ID );
                    }
                });
            }

            // listener
            site.addFieldListener( fieldListener = new IFormFieldListener() {
                public void fieldChange( FormFieldEvent ev ) {
                    if (ev.getEventCode() != VALUE_CHANGE) {
                        return;
                    }
                    else if (ev.getFieldName().equals( "store" ) ) {
                        storeLogin = (Boolean)ev.getNewModelValue().orElse( null );
                    }
                    else if (ev.getFieldName().equals( "username" ) ) {
                        username = (String)ev.getNewModelValue().orElse( null );
                    }
                    else if (ev.getFieldName().equals( "password" ) ) {
                        password = (String)ev.getNewModelValue().orElse( null );
                    }
                    if (loginBtn != null && !loginBtn.isDisposed()) {
                        // don't check dirty to allow login with stored credentials
                        loginBtn.setEnabled( /*site.isDirty() &&*/ site.isValid() );
                    }
                }
            });
        }

        
//        protected boolean validatePasswd( String passwd ) {
//            return passwd != null && Polymap.instance().validatePassword( username, passwd );
//        }

        
        /**
         * Does the login for given name and password. This default implementation
         * calls {@link Polymap#login(String, String)} and sets the {@link #user}
         * variable with the resulting {@link UserPrincipal}.
         * <p/>
         * If the login fails then nothing is done. Override this method to add special handling.
         * For example setting the status of the panel via:
         * <pre>
         * getSite().setStatus( new Status( IStatus.WARNING, UmPlugin.ID, "Nutzername oder Passwort sind nicht korrekt." ) );
         * </pre>.
         * 
         * @param name
         * @param passwd
         * @return True, if sucessfully logged in.
         */
        protected boolean login( final String name, final String passwd ) {
            SecurityContext sc = SecurityContext.instance();
            if (sc.login( name, passwd )) {
                user.set( (UserPrincipal)sc.getUser() );
                return true;
            }
            else {
                return false;
            }
        }


        protected void sendNewPassword( String name ) {
            UserRepository repo = UserRepository.instance();
            User umuser = repo.findUser( name );
            if (umuser != null) {
                IUndoableOperation op = new NewPasswordOperation( umuser );
                OperationSupport.instance().execute( op, true, false );
                panelSite.setStatus( new Status( IStatus.OK, UmPlugin.ID, i18n.get( "passwordSent", name ) ) );
            }
            else {
                panelSite.setStatus( new Status( IStatus.WARNING, UmPlugin.ID, i18n.get( "noSuchUser", name ) ) );
            }
        }


        protected void storeLogin( final String name, final String passwd ) {
            try {
                ISettingStore settings = RWT.getSettingStore();
                settings.setAttribute( "org.polymap.rhei.um.LoginForm.login", name );
                settings.setAttribute( "org.polymap.rhei.um.LoginForm.passwd", passwd );
            }
            catch (IOException e) {
                throw new RuntimeException( e );
            }
        }
        
    }        
        
}
