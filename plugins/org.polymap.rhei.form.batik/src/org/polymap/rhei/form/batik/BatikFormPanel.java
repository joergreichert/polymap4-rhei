/*
 * polymap.org
 * Copyright (C) 2013-2015, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.form.batik;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelSite;
import org.polymap.rhei.form.IFormPage;

/**
 * This panel supports Rhei forms. Sub-classes can use the Rhei form API the
 * create forms that are connected to features or entities.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class BatikFormPanel
        extends BatikFormContainer
        implements IPanel {

    private static Log log = LogFactory.getLog( BatikFormPanel.class );
    
    private PanelSite       panelSite;
    
    private IAppContext     context;

    
    public BatikFormPanel() {
        super( null );
        page = createFormPage();
    }


    @Override
    protected Composite createBody( Composite parent ) {
        // just use the body of the panel, no extra Composite
        return parent;
    }


    protected abstract IFormPage createFormPage();


    @Override
    public void setSite( PanelSite site, IAppContext context ) {
        this.panelSite = site;
        this.context = context;
    }


    @Override
    public void init() {
    }


    @Override
    public PanelSite site() {
        return panelSite;
    }

}
