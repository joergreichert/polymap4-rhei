/* 
 * polymap.org
 * Copyright (C) 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.um;

/**
 * Base class that represents a searchable entity in the user management. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface Entity {

//    /**
//     * Globally unique identifier of this entity.
//     */
//    public Property<String> id();
    
    /**
     * Creates a string representation that is used to represent the entity in the user interface.
     * 
     * @param separator
     * @return Newly created String.
     */
    public String getLabelString( String sep );

}
