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
package org.polymap.rhei.um.auth;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.security.AuthorizationModule;
import org.polymap.core.security.AuthorizationModuleExtension;
import org.polymap.core.security.UserPrincipal;

import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.internal.Messages;

/**
 * Provides authentication based on the information stored in the currently active
 * user repository.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UmLoginModule
        implements LoginModule {

    private static Log log = LogFactory.getLog( UmLoginModule.class );

    public static final IMessages           i18n = Messages.forPrefix( "UmLoginModule" );
    
    private CallbackHandler                 callbackHandler;

    private Subject                         subject;
    
    private UserPrincipal                   principal;
    
    private String                          dialogTitle = i18n.get( "dialogTitle" );
    
    private AuthorizationModule             authModule;
    
    protected UserRepository                repo;
    
    private boolean                         loggedIn;

    
    public UmLoginModule() {
    }


    @Override
    @SuppressWarnings("hiding")
    public void initialize( Subject subject, CallbackHandler callbackHandler, 
            Map<String,?> sharedState, Map<String,?> options ) {
        this.repo = UserRepository.instance();
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        
        this.authModule = new UmAuthorizationModule();
        this.authModule.init( this );
        
        // check user/passwd settings in options
        for (Object elm : options.entrySet()) {
            Map.Entry<String,String> option = (Map.Entry)elm;
            log.debug( "option: key=" + option.getKey() + " = " + option.getValue() );
            
            if (option.getKey().equals( "dialogTitle" )) {
                dialogTitle = option.getValue();
            }
            else if (option.getKey().equals( "authorizationExtensionId" )) {
                authModule = AuthorizationModuleExtension.forId( option.getValue() ).createClass();
                this.authModule.init( this );
            }
        }
    }


    @Override
    public boolean login() throws LoginException {
        Callback label = new TextOutputCallback( TextOutputCallback.INFORMATION, dialogTitle );
        NameCallback nameCallback = new NameCallback( i18n.get( "username" ), "default" );
        PasswordCallback passwordCallback = new PasswordCallback( i18n.get( "password" ), false );
        try {
            callbackHandler.handle( new Callback[] { label, nameCallback, passwordCallback } );
        }
        catch (Exception e) {
            log.warn( "", e );
            throw new LoginException( e.getLocalizedMessage() );
        }

        String username = nameCallback.getName();
//        if (username == null) {
//            return false;
//        }
        
        // admin
        if (username == null || username.equals( "admin" )) {
            // FIXME read password hash from persistent storage and check
            log.warn( "!!! NO PASSWORD check for admin user yet !!!!!!" );
            principal = new UserPrincipal( "admin" );
            return loggedIn = true;                
        }

        // ordinary user
        User user = repo.findUser( username );
        log.info( "username: " + user.email().get() );

        if (user != null && passwordCallback.getPassword() != null) {
            String password = String.valueOf( passwordCallback.getPassword() );
            if (PasswordEncryptor.instance().checkPassword( password, user.passwordHash().get() )) {
                log.info( "username: " + user.username().get() );
                principal = new UmUserPrincipal( user );
                return loggedIn = true;                
            }
        }
        return false;
    }


    @Override
    public boolean commit() throws LoginException {
        subject.getPrincipals().add( principal );

        subject.getPrivateCredentials().add( this );
        subject.getPrivateCredentials().add( authModule );
        
        return loggedIn;
    }


    @Override
    public boolean abort() throws LoginException {
        loggedIn = false;
        return true;
    }


    @Override
    public boolean logout() throws LoginException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
    
    /**
     * 
     */
    final class UmUserPrincipal
            extends UserPrincipal {

        private User            user;

        public UmUserPrincipal( User user ) {
            super( user.username().get() );
            this.user = user;
        }

        public User getUser() {
            return user;
        }
        
    }

}
