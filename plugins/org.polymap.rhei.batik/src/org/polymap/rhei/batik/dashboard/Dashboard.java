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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.LayoutConstraint;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Dashboard
        extends Configurable {

    private static Log log = LogFactory.getLog( Dashboard.class );

    @Mandatory
    @DefaultBoolean( true )
    public Config2<Dashboard,Boolean>       defaultBorder;
    
    private String                          id;
    
    private IPanelSite                      panelSite;
    
    private Map<IDashlet,DashletSite>       dashlets = new HashMap();

    
    public Dashboard( IPanelSite panelSite, String id ) {
        this.panelSite = panelSite;
        this.id = id;
    }
    
    
    public void dispose() {
        dashlets.keySet().stream().forEach( dashlet -> dashlet.dispose() );
        dashlets.clear();
    }
    
    
    public Dashboard addDashlet( IDashlet dashlet ) {
        DashletSite site = new DashletSiteImpl();
        BatikApplication.instance().getContext().propagate( dashlet );
        dashlet.init( site );
        dashlets.put( dashlet, site );
        return this;
    }
    
    
    public Composite createContents( Composite parent ) {
        IPanelToolkit tk = panelSite.toolkit();
        
        for (Entry<IDashlet,DashletSite> entry : dashlets.entrySet()) {
            DashletSite dashletSite = entry.getValue();
            IDashlet dashlet = entry.getKey();

            // listen to changes of the site made by the dashlet
            EventManager.instance().subscribe( this, ev -> ev.getSource() == dashletSite );
            
            String title = dashletSite.title.get();
            int border = defaultBorder.get() ? SWT.BORDER : SWT.NONE;
            if (dashletSite.border.get() != null) {
                border = dashletSite.border.get() ? SWT.BORDER : SWT.NONE;
            }
            int expandable = dashletSite.isExpandable.get() ? IPanelSection.EXPANDABLE : SWT.NONE;
            IPanelSection section = tk.createPanelSection( parent, title, border, expandable );
            
            List<LayoutConstraint> constraints = dashletSite.constraints.get();
            section.addConstraint( constraints.toArray( new LayoutConstraint[constraints.size()]) );
            
            dashlet.createContents( section.getBody() );
        }        
        return parent;
    }
    
    
    @EventHandler
    protected void sitePropertyChanged( PropertyChangeEvent ev ) {
        if (ev.getPropertyName().equals( "title" )) {
           log.warn( "!!! Dashlet TITLE changed! !!!" );    
        }
    }
    
    
    /**
     * 
     */
    class DashletSiteImpl
            extends DashletSite {

        @Override
        public IPanelSite panelSite() {
            return panelSite;
        }

        @Override
        public IPanelToolkit toolkit() {
            return panelSite.toolkit();
        }
        
    }
}
