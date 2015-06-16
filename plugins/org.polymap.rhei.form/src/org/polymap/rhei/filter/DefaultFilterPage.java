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

import org.eclipse.jface.action.Action;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultFilterPage
        implements IFilterPage {

    private String              id;
    
    private String              title;


    public DefaultFilterPage() {
    }

    
    public DefaultFilterPage( String id, String title ) {
        this.id = id;
        this.title = title;
    }

    
    @Override
    public String getId() {
        return id;
    }


    @Override
    public String getTitle() {
        return title;
    }


    @Override
    public Action[] getActions() {
        return null;
    }


    @Override
    public byte getPriority() {
        return 1;
    }

}
