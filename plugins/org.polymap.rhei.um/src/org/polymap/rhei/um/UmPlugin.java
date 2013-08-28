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
package org.polymap.rhei.um;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UmPlugin 
        extends AbstractUIPlugin {

	public static final String         ID = "org.polymap.rhei.um"; //$NON-NLS-1$

	private static UmPlugin            plugin;
	

    public void start( BundleContext context ) throws Exception {
        super.start( context );
        plugin = this;
    }


    public void stop( BundleContext context ) throws Exception {
        plugin = null;
        super.stop( context );
    }


    public static UmPlugin getDefault() {
        return plugin;
    }

}
