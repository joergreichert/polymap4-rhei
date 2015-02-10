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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rap.rwt.RWT;

import org.polymap.core.runtime.SessionSingleton;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.app.DefaultAppManager;
import org.polymap.rhei.batik.app.IAppDesign;
import org.polymap.rhei.batik.app.IAppManager;

/**
 * Factory of components of the Batik UI. The components are defined via several
 * plugin extension points.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BatikFactory
        extends SessionSingleton {

    private static Log log = LogFactory.getLog( BatikFactory.class );

    public static final String          APP_DESIGN_EXTENSION_POINT = "design";
    public static final String          PANEL_EXTENSION_POINT = "panels";


    public static BatikFactory instance() {
        return instance( BatikFactory.class );
    }


    // instance *******************************************

    private BatikFactory() {
    }


    public IAppManager createAppManager() {
        // XXX make it extendable
        return new DefaultAppManager();
    }


    public IAppDesign createAppDesign() {
        try {
            IConfigurationElement[] elms = Platform.getExtensionRegistry()
                    .getConfigurationElementsFor( BatikPlugin.PLUGIN_ID, APP_DESIGN_EXTENSION_POINT );

            String path = RWT.getRequest().getServletPath();
            IConfigurationElement bestMatch = null;
            String bestMatcher = null;
            for (IConfigurationElement elm : elms) {
                String matcher = elm.getAttribute( "servletNameMatcher" );
                if (FilenameUtils.wildcardMatch( path, matcher )) {
                    if (bestMatcher == null || bestMatcher.length() < matcher.length()) {
                        bestMatch = elm;
                        bestMatcher = matcher;
                    }
                }
            }
            return (IAppDesign)bestMatch.createExecutableExtension( "class" );
        }
        catch (CoreException e) {
            throw new RuntimeException( e );
        }
    }


    /**
     * The caller is responsibel of initializing the panel by calling
     * {@link IPanel#init(IPanelSite, IAppContext)}, and check return value.
     *
     * @param parent
     * @param name The panel name to filter, or null to get all panels.
     * @return Newly created panel instance.
     */
    public List<IPanel> createPanels( Predicate<IPanel> filter ) {
        IConfigurationElement[] elms = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( BatikPlugin.PLUGIN_ID, PANEL_EXTENSION_POINT );

        List<IPanel> result = new ArrayList();
        for (IConfigurationElement elm : elms) {
            try {
//                String name = elm.getAttribute( "name" );
//                IPath path = Path.fromPortableString( elm.getAttribute( "path" ) );

                IPanel panel = (IPanel)elm.createExecutableExtension( "class" );
                if (filter.apply( panel )) {
                    result.add( panel );
                }
            }
            catch (Exception e) {
                log.error( "Error while initializing panel: " + elm.getName(), e );
            }
        }
        return result;
    }

}
