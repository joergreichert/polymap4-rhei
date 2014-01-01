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
package org.polymap.rhei.batik.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.statushandlers.AbstractStatusHandler;
import org.eclipse.ui.statushandlers.StatusAdapter;

import org.eclipse.core.runtime.IStatus;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PanelStatusHandler
        extends AbstractStatusHandler {

    private static Log log = LogFactory.getLog( PanelStatusHandler.class );


    public PanelStatusHandler() {
    }


    @Override
    public void handle( StatusAdapter adapter, int style ) {
        IStatus status = adapter.getStatus();
        if (status.getException() != null) {
            log.info( status.getMessage(), status.getException() );
        }
        else {
            log.info( status.getMessage() );
        }
    }
    
}
