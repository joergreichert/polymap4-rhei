/*
 * polymap.org
 * Copyright (C) 2010-2015, Falko Bräutigam. All rights reserved.
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
 */
package org.polymap.rhei.form;

import org.opengis.feature.Property;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Provides the interface used inside {@link IFormPage} methods to
 * interact with the framework.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IFormPageSite
        extends IBasePageSite {

    public FieldBuilder newFormField( Property property );
    
    /**
     * (Re)loads all fields of the editor from the backend.
     * <p/>
     * This method might long run and/or block while accessing the backend system.
     * 
     * @param monitor This method can be called from within a {@link Job}. It reports
     *        progress to this monitor. Outside a job this parameter might be
     *        <code>null</code>.
     */
    public void submit( IProgressMonitor monitor ) throws Exception;

}
