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
package org.polymap.rhei.um.auth;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.security.Principal;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.polymap.core.security.AuthorizationModule;
import org.polymap.core.security.UserPrincipal;

import org.polymap.rhei.um.User;
import org.polymap.rhei.um.auth.UmLoginModule.UmUserPrincipal;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UmAuthorizationModule
        implements AuthorizationModule {

    private static Log log = LogFactory.getLog( UmAuthorizationModule.class );

    private UmLoginModule       loginModule;

    
    @Override
    public void init( @SuppressWarnings("hiding") LoginModule loginModule ) {
        this.loginModule = (UmLoginModule)loginModule;
    }


    @Override
    public void initialize( Subject subject, CallbackHandler callbackHandler, 
            Map<String, ?> sharedState, Map<String, ?> options ) {
    }


    @Override
    public Set<Principal> rolesOf( Subject subject ) {
        Set<UserPrincipal> principals = subject.getPrincipals( UserPrincipal.class );
        UserPrincipal principal = Iterables.getOnlyElement( principals );
        
        Set<Principal> result = new HashSet();
        if (principal.getName().equals( "admin" )) {
            //
        }
        else {
            User user = ((UmUserPrincipal)principal).getUser();
            for (final String groupName : loginModule.repo.groupsOf( user )) {
                result.add( new Principal() {
                    @Override
                    public String getName() {
                        return groupName;
                    }
                });
            }
        }
        return result;
    }
    
}
