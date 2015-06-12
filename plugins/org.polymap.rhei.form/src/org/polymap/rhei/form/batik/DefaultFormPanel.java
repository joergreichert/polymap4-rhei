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

import org.eclipse.jface.action.Action;

import org.eclipse.ui.forms.widgets.FormToolkit;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.form.IFormPage;
import org.polymap.rhei.form.IFormToolkit;
import org.polymap.rhei.internal.form.AbstractFormPageContainer;
import org.polymap.rhei.internal.form.FormEditorToolkit;

/**
 * This panel supports Rhei forms. Sub-classes can use the Rhei form API the
 * create forms that are connected to features or entities.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultFormPanel
        extends DefaultPanel
        implements IPanel, IFormPage {

    private static Log log = LogFactory.getLog( DefaultFormPanel.class );

    private FormEditorToolkit   toolkit;

    private Composite           pageBody;

    private PageContainer       pageSite;


    @Override
    public final void createContents( Composite parent ) {
        toolkit = new FormEditorToolkit( new FormToolkit( UIUtils.sessionDisplay() ) );
        pageBody = parent;
        pageSite = new PageContainer( this );
        createFormContent( pageSite );
    }

    
    // default implementation of IFormPage **********

    @Override
    public final String getTitle() {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Action[] getEditorActions() {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public String getId() {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public byte getPriority() {
        throw new RuntimeException( "not yet implemented." );
    }


    /**
     *
     */
    private class PageContainer
            extends AbstractFormPageContainer {

        public PageContainer( IFormPage page ) {
            super( DefaultFormPanel.this, page, "_id_", "_title_" );
            setLabelWidth( 150 );
        }

        public void createContent() {
            page.createFormContent( this );
        }

        public Composite getPageBody() {
            return pageBody;
        }

        public IFormToolkit getToolkit() {
            return toolkit;
        }

        public void setFormTitle( String title ) {
            getSite().setTitle( title );
        }

        public void setEditorTitle( String title ) {
            getSite().setTitle( title );
        }

        public void setActivePage( String pageId ) {
            log.warn( "setActivePage() not supported." );
        }
    }

}
