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
package org.polymap.rhei.batik.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.internal.widgets.JSExecutor;

import org.eclipse.jface.action.Action;

import org.polymap.core.runtime.IMessages;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.internal.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("restriction")
public class LogoutAction
        extends Action {

    private static Log log = LogFactory.getLog( LogoutAction.class );
    
    private static final IMessages      i18n = Messages.forPrefix( "LogoutAction" );

    
    public LogoutAction() {
        super( i18n.get( "title" ) );
        setImageDescriptor( BatikPlugin.imageDescriptorFromPlugin( BatikPlugin.PLUGIN_ID, "resources/icons/switch.png" ) );
    }


    @Override
    public void run() {
        JSExecutor.executeJS( "window.location.reload();" );
    }
    
}
