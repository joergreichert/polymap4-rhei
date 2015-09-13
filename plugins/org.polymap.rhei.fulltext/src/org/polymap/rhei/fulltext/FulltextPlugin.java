/*
 * polymap.org
 * Copyright (C) 2014-2015, Falko Bräutigam. All rights reserved.
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

import org.polymap.core.ui.ImageRegistryHelper;
import org.polymap.core.ui.StatusDispatcher;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FulltextPlugin 
        extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.polymap.rhei.fulltext";

	private static FulltextPlugin      instance;
	
	
	public static FulltextPlugin instance() {
    	return instance;
    }

    /**
     * Use this to create frequently used images used by this plugin.
     */
    public static ImageRegistryHelper images() {
        return instance().images;
    }

    
	// instance *******************************************

    private ImageRegistryHelper         images = new ImageRegistryHelper( this );
    
    private ErrorHandler                errorHandler;

    
    public void start( final BundleContext context ) throws Exception {
        super.start( context );
        instance = this;

        errorHandler = new ErrorHandler() {
            @Override
            public void handleError( String msg, Throwable e ) {
                StatusDispatcher.handleError( FulltextPlugin.PLUGIN_ID, FulltextPlugin.this, msg, e );
            }
        };
    }
	

    public void stop( BundleContext context ) throws Exception {
        errorHandler = null;
        instance = null;
        super.stop( context );
    }


    public void handleError( final String msg, Throwable e ) {
        errorHandler.handleError( msg, e );        
    }

    
    public void setErrorHandler( ErrorHandler errorHandler ) {
        this.errorHandler = errorHandler;    
    }

    
    // ErrorHandler ***************************************
    
    public interface ErrorHandler {
    
        public void handleError( final String msg, Throwable e );

    }
    
}
