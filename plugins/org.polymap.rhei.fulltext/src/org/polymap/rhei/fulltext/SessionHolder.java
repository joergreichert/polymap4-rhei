/*
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.fulltext;

import org.polymap.core.runtime.entity.EntityStateEvent;
import org.polymap.core.runtime.session.DefaultSessionContext;
import org.polymap.core.runtime.session.DefaultSessionContextProvider;
import org.polymap.core.runtime.session.SessionContext;
import org.polymap.core.security.SecurityContext;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.security.UserPrincipal;

/**
 * Helps to manages a {@link SessionContext} outside a user/UI session.
 * Useful to access session-bound data like {@link ILayer}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SessionHolder { 

    private String                          serviceContextName;
    
    private DefaultSessionContext           serviceContext;
    
    private DefaultSessionContextProvider   contextProvider;

    
    public SessionHolder( String serviceContextName ) {
        this.serviceContextName = serviceContextName;

        assert serviceContext == null && contextProvider == null;
        serviceContext = new DefaultSessionContext( serviceContextName );
        
        contextProvider = new DefaultSessionContextProvider() {
            protected DefaultSessionContext newContext( String sessionKey ) {
                return serviceContext;
            }
        };
        SessionContext.addProvider( contextProvider );
    }


    public DefaultSessionContext getContext() {
        return serviceContext;
    }


    /**
     * Maps the current thread to the context with the given sessionKey. If no
     * context exists yet, then a new one is created.
     * 
     * @see DefaultSessionContextProvider#mapContext()
     */
    public void mapServiceContext() {
        contextProvider.mapContext( serviceContext.getSessionKey(), true );

        if (SecurityContext.instance().getPrincipals().isEmpty()) {
            // allow the indexers to access all maps and layers
            // during startup
            SecurityContext.instance().addPrincipal( new UserPrincipal( SecurityUtils.ADMIN_USER ) {
                public String getPassword() {
                    throw new RuntimeException( "not yet implemented." );
                }
            });
        }
    }
    
    
    /**
     * Releases the current thread from the mapped context.
     * 
     * @see DefaultSessionContextProvider#unmapContext() 
     */
    public void unmapServiceContext() {
        contextProvider.unmapContext();
    }


    /**
     * Destroys the current {@link SessionContext} and creates a new one.
     * <p/>
     * XXX This allows to indexers the reload their content from the
     * {@link ProjectRepository} after a {@link EntityStateEvent}. It is a hack since
     * it drops the context without the chance for other indexers to remove their
     * listeners.
     */
    public void dropServiceContext() {
        if (serviceContext != null) {
            contextProvider.destroyContext( serviceContext.getSessionKey() );
        }
        serviceContext = new DefaultSessionContext( serviceContextName );
        mapServiceContext();
    }


    /**
     * Executes the given Runnable inside this session context.
     * 
     * @see SessionContext#execute(Runnable)
     */
    public void execute( Runnable task ) {
        serviceContext.execute( task );
    }
    
}
