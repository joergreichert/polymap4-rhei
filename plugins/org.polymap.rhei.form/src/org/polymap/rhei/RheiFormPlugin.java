/* 
 * polymap.org
 * Copyright (C) 2010-2015, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei;

import org.osgi.framework.BundleContext;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.ui.ImageRegistryHelper;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RheiFormPlugin 
        extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.polymap.rhei.form";

	// The shared instance
	private static RheiFormPlugin plugin;
	

	public static RheiFormPlugin getDefault() {
    	return plugin;
    }


	/**
     * Use this to create frequently used images used by this plugin.
     */
    public static ImageRegistryHelper images() {
        return getDefault().images;
    }

    
	// instance *******************************************
	
	private ImageRegistryHelper        images = new ImageRegistryHelper( this );
	
	
    public void start( BundleContext context ) throws Exception {
        super.start( context );
        plugin = this;
    }

    public void stop( BundleContext context ) throws Exception {
        plugin = null;
        super.stop( context );
    }

}
