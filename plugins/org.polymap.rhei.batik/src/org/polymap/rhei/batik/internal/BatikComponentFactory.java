/*
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.runtime.SessionSingleton;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IApplicationLayouter;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;

/**
 * Factory of components of the Atlas UI. The components are defined via several
 * plugin extension points.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BatikComponentFactory
        extends SessionSingleton {

    private static Log log = LogFactory.getLog( BatikComponentFactory.class );

    public static final String          APPLICATION_LAYOUTER_EXTENSION_POINT = "applicationLayouters";

    public static final String          PANEL_EXTENSION_POINT = "panels";


    public static BatikComponentFactory instance() {
        return instance( BatikComponentFactory.class );
    }


    // instance *******************************************

    private BatikComponentFactory() {
    }


    /**
     * Creates the main application layouter for the current environment.
     */
    public IApplicationLayouter createApplicationLayouter() {
        try {
            IConfigurationElement[] elms = Platform.getExtensionRegistry()
                    .getConfigurationElementsFor( BatikPlugin.PLUGIN_ID, APPLICATION_LAYOUTER_EXTENSION_POINT );
            assert elms.length == 1;

            return (IApplicationLayouter)elms[0].createExecutableExtension( "class" );
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
