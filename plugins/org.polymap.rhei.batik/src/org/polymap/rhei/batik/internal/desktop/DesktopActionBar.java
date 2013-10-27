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
package org.polymap.rhei.batik.internal.desktop;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.IContributionItem;

import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;


/**
 * The main action bar displayed at the top of the main window.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class DesktopActionBar {

    private static Log log = LogFactory.getLog( DesktopActionBar.class );

    enum PLACE {
        SEARCH,
        PANEL_TOOLBAR,
        PANEL_NAVI,
        PANEL_SWITCHER,
        USER_PREFERENCES
    }

    // instance *******************************************

    private IAppContext                 context;

    private IPanelToolkit               tk;

    private Map<PLACE,IContributionItem> items = new HashMap();


    public DesktopActionBar( IAppContext context, IPanelToolkit tk ) {
        this.context = context;
        this.tk = tk;
        context.addEventHandler( this );
    }


    public IContributionItem add( IContributionItem item, PLACE place ) {
        return items.put( place, item );
    }


    public Composite createContents( Composite parent ) {
        Composite contents = tk.createComposite( parent );
        contents.setLayout( FormLayoutFactory.defaults().spacing( 10 ).create() );

        Composite left = null;
        Composite right = null;
        
        // preferences
        IContributionItem prefs = items.get( PLACE.USER_PREFERENCES );
        if (prefs != null) {
            Composite container = new Composite( contents, SWT.BORDER );
            prefs.fill( container );
            container.setLayoutData( FormDataFactory.filled().left( 100, -200 ).right( 100 ).create() );
            right = container;
        }
        
        // search
        IContributionItem search = items.get( PLACE.SEARCH );
        if (search != null) {
            Composite container = new Composite( contents, SWT.NONE );
            search.fill( container );
            container.setLayoutData( right != null
                    ? FormDataFactory.filled().left( right, -300 ).right( right ).create()
                    : FormDataFactory.filled().left( 100, -300 ).right( 100 ).create() );
            right = container;
        }
        
        // panel toolbar
        IContributionItem tb = items.get( PLACE.PANEL_TOOLBAR );
        if (tb != null) {
            Composite container = new Composite( contents, SWT.BORDER );
            tb.fill( container );
            container.setLayoutData( right != null
                    ? FormDataFactory.filled().left( right, -150 ).right( right ).create()
                    : FormDataFactory.filled().left( 100, -150 ).right( 100 ).create() );
            right = container;
        }

        // panel navi
        IContributionItem navi = items.get( PLACE.PANEL_NAVI );
        if (navi != null) {
            Composite container = new Composite( contents, SWT.NONE );
            navi.fill( container );
            container.setLayoutData( FormDataFactory.filled().right( -1 ).create() );
            left = container;
        }
        
        // panel switcher
        IContributionItem switcher = items.get( PLACE.PANEL_SWITCHER );
        if (switcher != null) {
            Composite container = new Composite( contents, SWT.NONE );
            switcher.fill( container );
            container.setLayoutData( right != null
                    ? FormDataFactory.filled().left( left ).right( -1 ).create()
                    : FormDataFactory.filled().left( 0 ).right( -1 ).create());
            left = container;
        }

        return contents;
    }


    @EventHandler(display=true)
    protected void panelChanged( PanelChangeEvent ev ) {
    }

}
