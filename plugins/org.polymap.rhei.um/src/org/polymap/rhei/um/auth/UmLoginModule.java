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
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides authentication based on the information stored in the currently active
 * user repository.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UmLoginModule
        implements LoginModule {

    private static Log log = LogFactory.getLog( UmLoginModule.class );


    /**
     * 
     */
    public UmLoginModule() {
    }


    @Override
    public void initialize( Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public boolean login() throws LoginException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public boolean commit() throws LoginException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public boolean abort() throws LoginException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public boolean logout() throws LoginException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
}
