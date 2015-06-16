/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * $Id: $
 */
package org.polymap.rhei.form;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This extended {@link IFormPage} interface allows to extend standard
 * page behaviour: state handling, load/store, dispose
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IFormPage2
        extends IFormPage {

    boolean isDirty();
    
    boolean isValid();


    /**
     * Reload all fields from the backend. This reverts any changes made so far in
     * this page. If sucessfull, {@link #isDirty()} and {@link #isValid()} should
     * return <code>true</code> afterwards.
     * 
     * @param monitor
     * @throws Exception
     */
    void doLoad( IProgressMonitor monitor ) throws Exception;


    /**
     * Store any changes in the backend. If sucessfull, {@link #isDirty()} and
     * {@link #isValid()} should return <code>true</code> afterwards.
     * 
     * @param monitor
     * @throws Exception
     */
    void doSubmit( IProgressMonitor monitor ) throws Exception;
    
    /**
     * Dispose any resource this page may have aquired in {@link #createFormContents(IFormPageSite)}
     * Form fields that were created via {@link IFormPageSite#newFormField(org.opengis.feature.Property)}
     * are automatically disposed and must not be disposed in this method.
     */
    void dispose();

}
