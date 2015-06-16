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
package org.polymap.rhei.form;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.form.IFormPage;
import org.polymap.rhei.form.IFormToolkit;
import org.polymap.rhei.internal.form.FormPageController;

/**
 * Base class for one-page form containers.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FormPageContainer
        extends BasePageContainer<IFormPage,FormPageController> {

    private static Log log = LogFactory.getLog( FormPageContainer.class );
    

    public FormPageContainer( IFormPage page ) {
        super( page );
    }


    /**
     * Creates the UI of this form by calling
     * {@link #createFormContent(org.polymap.rhei.form.IFormPageSite)}.
     * 
     * @param parent The parent under which to create the form UI controls.
     */
    @Override
    public final Composite createContents( Composite parent ) {
        Composite result = super.createContents( parent );
        
        // allow sub classes to preset
        if (pageController == null) {
            pageController = new PageController( page );
        }

        try {
            page.createFormContent( pageController );
            updateEnabled();
            pageController.doLoad( new NullProgressMonitor() );
        }
        catch (Exception e) {
            StatusDispatcher.handleError( "An error occured while creating the new page.", e );
        }
        return result;
    }

    
    public final void createContents( FormPageContainer parent ) {
        toolkit = parent.toolkit;
        pageBody = parent.pageBody;
        pageController = parent.pageController;

        page.createFormContent( pageController );
        updateEnabled();
    }

    
    public void submit() throws Exception {
        pageController.submitEditor();
    }

    
    public void reloadEditor() throws Exception {
        pageController.reloadEditor();
    }


    /**
     * The container for the one and only page of this form.
     */
    protected class PageController
            extends FormPageController {

        public PageController( IFormPage page ) {
            super( FormPageContainer.this, page, "_id_", "_title_" );
            double displayWidth = UIUtils.sessionDisplay().getBounds().width;
            // minimum 110
            double width = 110;
            // plus 10px per 100 pixel display width
            if (displayWidth > 1000) {
                width += (displayWidth - 1000) * 0.1;
            }
            // but not more than 160 :)
            width = Math.min( 150, width );
            log.info( "labelWidth: " + width );
            setLabelWidth( (int)width );
        }

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
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public void setEditorTitle( String title ) {
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public void setActivePage( String pageId ) {
            log.warn( "setActivePage() not supported." );
        }
    }

}
