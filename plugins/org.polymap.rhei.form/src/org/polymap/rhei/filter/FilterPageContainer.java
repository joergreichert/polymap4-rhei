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
package org.polymap.rhei.filter;

import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.ui.StatusDispatcher;
import org.polymap.rhei.form.BasePageContainer;
import org.polymap.rhei.internal.filter.FilterPageController;

/**
 * Base class for one-page filter containers.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public abstract class FilterPageContainer
        extends BasePageContainer<IFilterPage,FilterPageController> {

    private static Log log = LogFactory.getLog( FilterPageContainer.class );
    

    /**
     * Creates the UI of this form by calling
     * {@link #createFormContent(org.polymap.rhei.form.IFormPageSite)}.
     * 
     * @param parent The parent under which to create the form UI controls.
     */
    @Override
    public final void createContents( Composite parent ) {
        super.createContents( parent );
        try {
            page.createFilterContents( pageController );
            updateEnabled();
            pageController.doLoad( new NullProgressMonitor() );
        }
        catch (Exception e) {
            StatusDispatcher.handleError( "An error occured while creating the new page.", e );
        }
    }

    
    public final void createContents( FilterPageContainer parent ) {
        toolkit = parent.toolkit;
        pageBody = parent.pageBody;
        pageController = parent.pageController;

        page.createFilterContents( pageController );
        updateEnabled();
    }

    
    public Filter buildFilter() throws Exception {
        return pageController.doBuildFilter( new NullProgressMonitor() );    
    }
    
}
