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
package org.polymap.rhei.filter;

import org.opengis.filter.Filter;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.rhei.form.IFormPageSite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IFilterPage2
        extends IFilterPage {

    public boolean isDirty();
    
    public boolean isValid();

    /**
     * Loads any custom field in the page.
     * 
     * @param monitor
     * @throws Exception
     */
    public void doLoad( IProgressMonitor monitor ) throws Exception;

    /**
     * Extends the given prepared standard filter.
     *
     * @param filter
     */
    public Filter doBuildFilter( Filter filter, IProgressMonitor monitor ) throws Exception;

    /**
     * Dispose any resource this page may have aquired in {@link #createFormContent(IFormPageSite)}.
     * Form fields that were created via {@link IFormPageSite#newFormField(org.eclipse.swt.widgets.Composite, org.opengis.feature.Property, org.polymap.rhei.field.IFormField, org.polymap.rhei.field.IFormFieldValidator)}
     * are automatically disposed and must not be disposed in this method.
     */
    void dispose();

}
