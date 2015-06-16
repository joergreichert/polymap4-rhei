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

import org.polymap.rhei.form.IFormPage;
import org.polymap.rhei.internal.form.FormPageController;

/**
 * Base class for one-page form containers.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FormPageContainer
        extends BasePageContainer<IFormPage,FormPageController> {

    private static Log log = LogFactory.getLog( FormPageContainer.class );
    

    /**
     * Creates the UI of this form by calling
     * {@link #createFormContents(org.polymap.rhei.form.IFormPageSite)}.
     * 
     * @param parent The parent under which to create the form UI controls.
     */
    @Override
    public final void createContents( Composite parent ) {
        super.createContents( parent );
        try {
            page.createFormContents( pageController );
            updateEnabled();
            pageController.doLoad( new NullProgressMonitor() );
        }
        catch (Exception e) {
            StatusDispatcher.handleError( "An error occured while creating the new page.", e );
        }
    }

    
    public final void createContents( FormPageContainer parent ) {
        toolkit = parent.toolkit;
        pageBody = parent.pageBody;
        pageController = parent.pageController;

        page.createFormContents( pageController );
        updateEnabled();
    }

    
    public void submit() throws Exception {
        pageController.submitEditor();
    }

    
    public void reloadEditor() throws Exception {
        pageController.reloadEditor();
    }

}
