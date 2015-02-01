/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.batik.dashboard;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.SiteProperty;
import org.polymap.rhei.batik.internal.DefaultSiteProperty;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Dashboard {

    private static Log log = LogFactory.getLog( Dashboard.class );

    private String                          id;
    
    private IPanelSite                      panelSite;
    
    private Map<IDashlet,IDashletSite>      dashlets = new HashMap();

    
    public Dashboard( IPanelSite panelSite, String id ) {
        this.panelSite = panelSite;
        this.id = id;
    }
    
    
    public void addDashlet( IDashlet dashlet ) {
        DashletSite site = new DashletSite();
        dashlet.init( site );
        dashlets.put( dashlet, site );
    }
    
    
    public Composite createContents( Composite parent ) {
        IPanelToolkit tk = panelSite.toolkit();
        
        for (Entry<IDashlet,IDashletSite> entry : dashlets.entrySet()) {
            IDashletSite dashletSite = entry.getValue();
            IDashlet dashlet = entry.getKey();
            
            String title = dashletSite.title().get();
            int border = dashletSite.isBoxStyle().get() ? SWT.BORDER : SWT.NONE;
            int expandable = SWT.NONE;
            IPanelSection section = tk.createPanelSection( parent, title, border, expandable );
            
            dashlet.createContents( section.getBody() );
        }
        return parent;
    }
    
    
    /**
     * 
     */
    class DashletSite
            implements IDashletSite {

        private DefaultSiteProperty<String>     title = new DefaultSiteProperty( "" );
        
        private DefaultSiteProperty<Boolean>    isBoxStyle = new DefaultSiteProperty( false );
        
        @Override
        public SiteProperty<String> title() {
            return title;
        }

        @Override
        public SiteProperty<Boolean> isBoxStyle() {
            return isBoxStyle;
        }

        @Override
        public IPanelSite panelSite() {
            return panelSite();
        }

        @Override
        public IPanelToolkit toolkit() {
            return panelSite.toolkit();
        }
        
    }
    
}
