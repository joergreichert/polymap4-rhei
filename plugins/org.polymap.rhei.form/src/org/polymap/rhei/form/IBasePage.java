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

import org.eclipse.jface.action.Action;

/**
 * A base page of form and filter pages consisting of form fields created via its
 * site.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IBasePage {

    String getTitle();


    /**
     * An array of actions that are contributed to the toolbar of the editor of
     * this page.
     * 
     * @return The actions to be added to the toolbar of the editor of this
     *         page, or null if no actions are to be contributed.
     */
    Action[] getActions();
    
    String getId();


    /**
     * The priority of this page within the entiry editor of a feature.
     * 
     * @return A value of 0 specifies that this page has no special priority;
     *         {@link Byte#MAX_VALUE} that this page is shown "on top" of the stack
     *         of all pages and it is shown by default.
     */
    byte getPriority();
    
}
