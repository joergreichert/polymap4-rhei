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

import org.osgi.framework.BundleContext;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FullTextPlugin 
        extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.polymap.rhei.fulltext";

	private static FullTextPlugin      instance;
	
	
	public static FullTextPlugin instance() {
    	return instance;
    }


	// instance *******************************************

    public void start( final BundleContext context ) throws Exception {
        super.start( context );
        instance = this;
    }
	

    public void stop( BundleContext context ) throws Exception {
        instance = null;
        super.stop( context );
    }

}
