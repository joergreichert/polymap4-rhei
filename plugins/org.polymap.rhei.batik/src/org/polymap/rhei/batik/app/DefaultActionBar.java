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
package org.polymap.rhei.batik.app;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 * The main action bar displayed at the top of the main window.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultActionBar {

    private static Log log = LogFactory.getLog( DefaultActionBar.class );

    public enum PLACE {
        SEARCH,
        PANEL_TOOLBAR,
        PANEL_NAVI,
        PANEL_SWITCHER,
        USER_PREFERENCES,
        STATUS
    }

    /**
     * 
     */
    public interface Part {
        
        public void fillContents( Composite parent );
        
    }
    
    // instance *******************************************

    private IAppContext                 context;

    private IPanelToolkit               tk;

    private Map<PLACE,Part>             parts = new HashMap();


    public DefaultActionBar( IAppContext context, IPanelToolkit tk ) {
        this.context = context;
        this.tk = tk;
    }


    public Part add( Part part, PLACE place ) {
        return parts.put( place, part );
    }


    public Composite createContents( Composite parent, int style ) {
        Composite contents = tk.createComposite( parent, style );
        UIUtils.setVariant( contents, IAppDesign.CSS_ACTIONS );
        contents.setLayout( FormLayoutFactory.defaults().spacing( 10 ).create() );

        Composite left = null;
        Composite right = null;
        
        // preferences
        Part prefs = parts.get( PLACE.USER_PREFERENCES );
        if (prefs != null) {
            Composite container = new Composite( contents, SWT.NONE );
            prefs.fillContents( container );
            container.setLayoutData( FormDataFactory.filled()/*.left( 100, -210 )*/.left( -1 ).right( 100 ).create() );
            right = container;
        }
        
        // search
        Part search = parts.get( PLACE.SEARCH );
        if (search != null) {
            Composite container = new Composite( contents, SWT.NONE );
            search.fillContents( container );
            container.setLayoutData( right != null
                    ? FormDataFactory.filled().left( right, -300 ).right( right ).create()
                    : FormDataFactory.filled().left( 100, -300 ).right( 100 ).create() );
            right = container;
        }
        
        // status
        Part status = parts.get( PLACE.STATUS );
        if (status != null) {
            Composite container = new Composite( contents, SWT.NONE );
            status.fillContents( container );
            container.setLayoutData( right != null
                    ? FormDataFactory.filled().left( -1 ).width( 35 ).right( right ).create()
                    : FormDataFactory.filled().left( 100, -200 ).right( 100 ).create() );
            right = container;
        }
        
        // panel toolbar
        Part tb = parts.get( PLACE.PANEL_TOOLBAR );
        if (tb != null) {
            Composite container = new Composite( contents, SWT.NONE );
            tb.fillContents( container );
            container.setLayoutData( right != null
                    ? FormDataFactory.filled().left( 50 ).right( 50, 200 ).create()
                    : FormDataFactory.filled().left( 100, -200 ).right( 100 ).create() );
            right = container;
        }

        // panel navi
        Part navi = parts.get( PLACE.PANEL_NAVI );
        if (navi != null) {
            Composite container = new Composite( contents, SWT.NONE );
            navi.fillContents( container );
            container.setLayoutData( FormDataFactory.filled().right( -1 ).width( 600 ).create() );
            left = container;
        }
        
        // panel switcher
        Part switcher = parts.get( PLACE.PANEL_SWITCHER );
        if (switcher != null) {
            Composite container = new Composite( contents, SWT.NONE );
            switcher.fillContents( container );
            container.setLayoutData( right != null
                    ? FormDataFactory.filled().left( left ).right( -1 ).create()
                    : FormDataFactory.filled().left( 0 ).right( -1 ).create());
            left = container;
        }

        return contents;
    }

}
