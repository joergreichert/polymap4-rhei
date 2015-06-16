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
package org.polymap.rhei.filter;

import org.polymap.rhei.form.IBasePage;

/**
 * A form page consisting of form fields created via its {@link IFormPageSite}.
 * <p/>
 * If you need more control over UI elements and/or submit/load then consider the
 * {@link IFilterPage2} interface.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IFilterPage
        extends IBasePage {

    /**
     * Creates the user interface of this form page.
     * <p>
     * <b>Example code:</b>
     * <pre>
     *    site.setFormTitle( "Title" );
     *    site.getPageBody().setLayout( new FormLayout() );
     *
     *    Composite field = site.newFormField( null, "", new TextFormField(), null );
     *    field.setLayoutData( layoutData );
     * </pre>
     *
     * @param site The API to create fields and interact with the framework.
     */
    void createFilterContents( IFilterPageSite site );

}
