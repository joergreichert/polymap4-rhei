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
package org.polymap.rhei.form.batik;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.Defaults;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.dashboard.DashletSite;
import org.polymap.rhei.batik.dashboard.IDashlet;
import org.polymap.rhei.batik.toolkit.LayoutConstraint;
import org.polymap.rhei.form.IFormPage;
import org.polymap.rhei.form.IFormToolkit;
import org.polymap.rhei.internal.form.FormPageController;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BatikFormDashlet
        extends BatikFormContainer
        implements IDashlet {

    private static Log log = LogFactory.getLog( BatikFormDashlet.class );
    
    protected DashletSite                   dashletSite;
    
    public Config<BatikFormDashlet,String>  title;

    @Defaults
    public Config<BatikFormDashlet,List<LayoutConstraint>> constraints;

    
    public BatikFormDashlet( IFormPage page ) {
        super( page );
        ConfigurationFactory.inject( this );

        this.pageController = new FormPageController( page ) {
            @Override
            public Composite getPageBody() {
                return pageBody;
            }
            @Override
            public IFormToolkit getToolkit() {
                return toolkit;
            }
            @Override
            public void setPageTitle( String title ) {
                dashletSite.title.set( title );
            }
            @Override
            public void setEditorTitle( String title ) {
            }
            @Override
            public void setActivePage( String pageId ) {
                throw new UnsupportedOperationException( "This is a single page container." );
            }
            @Override
            protected Object getEditor() {
                return BatikFormDashlet.this;
            }
            @Override
            protected Composite createFieldComposite( Composite parent ) {
                return UIUtils.setVariant( toolkit.createComposite( parent ), CSS_FORMFIELD );
            }
        };
    }


    @Override
    public void init( DashletSite site ) {
        this.dashletSite = site;
        title.ifPresent( value -> site.title.set( value ) );
        site.constraints.set( constraints.get() );
    }
    
}
