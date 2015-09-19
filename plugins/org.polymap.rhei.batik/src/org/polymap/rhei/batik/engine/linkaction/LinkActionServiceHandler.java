/* 
 * polymap.org
 * Copyright (C) 2011-2015, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.batik.engine.linkaction;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.rap.rwt.service.ServiceHandler;

import org.polymap.core.runtime.ConcurrentReferenceHashMap;
import org.polymap.core.runtime.ConcurrentReferenceHashMap.ReferenceType;

import org.polymap.rhei.batik.toolkit.ILinkAction;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LinkActionServiceHandler
        implements ServiceHandler {

    private static Log log = LogFactory.getLog( LinkActionServiceHandler.class );

    public static final String          SERVICE_HANDLER_ID = "org.polymap.rhei.batik.LinkActionServiceHandler";

    public static final String          ID_REQUEST_PARAM = "id";

    static ConcurrentReferenceHashMap<String,ILinkAction>   providers 
            = new ConcurrentReferenceHashMap( ReferenceType.STRONG, ReferenceType.WEAK );


    /**
     * Registers the given provider for downloading. An unique id of the newly
     * registered download is build automatically.
     * 
     * @param action The action to execute. (<b>WEAKLY</b> referenced inside!)
     * @return The download URL for the given provider.
     */
    public static String register( ILinkAction action ) {
        return register( String.valueOf( action.hashCode() ), action );
    }


    /**
     * Registers the given provider for downloading.
     * 
     * @param id
     * @param provider
     * @return The download URL for the given provider.
     */
    protected static String register( String id, ILinkAction action ) {
        if (providers.put( id, action ) != null) {
            log.warn( "ContetProvider already registered for id: " + id );
        }
       // UICallBack.activate( LinkActionServiceHandler.class.getName() );
        return id;        
    }
    

    // instance *******************************************
    
    @Override
    public void service( HttpServletRequest request, HttpServletResponse response ) 
            throws IOException, ServletException {
        String id = request.getParameter( ID_REQUEST_PARAM );
        log.info( "Request: id=" + id );
        if (id == null) {
            log.warn( "No 'id' param in request." );
            response.sendError( 404 );
            return;
        }

        final ILinkAction linkAction = providers.get( id );
        if (linkAction == null) {
            log.warn( "No content provider registered for id: " + id );
            response.sendError( 404 );
            return;
        }
        
        //response.flushBuffer();

        Runnable task = () -> { try { linkAction.linkPressed(); } catch (Exception e) { log.warn( "", e ); } };
        Display display = linkAction.display();
        if (display != null) {
            display.asyncExec( task );
        }
        else {
            task.run();
        }
    }
    
}
