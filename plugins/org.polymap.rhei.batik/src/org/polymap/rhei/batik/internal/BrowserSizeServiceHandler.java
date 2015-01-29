/* 
 * polymap.org
 * Copyright (C) 2013-2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.batik.internal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.rap.rwt.service.ServiceHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BrowserSizeServiceHandler
        implements ServiceHandler {

    private static Log log = LogFactory.getLog( BrowserSizeServiceHandler.class );

    /* FIXME !!! */
    public static Display       current;

    @Override
    public void service( HttpServletRequest request, HttpServletResponse response ) 
            throws IOException, ServletException {
        log.info( "Browser size: " + request.getParameter( "width" ) );
        
        current.asyncExec( new Runnable() {
            public void run() {
                log.info( "Display size: " + current.getBounds() );
                EventManager.instance().publish( new ApplicationResizeEvent( current ) );
            }
        });
    }
    
}
