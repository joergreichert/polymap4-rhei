/* 
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.um.providers.qi4j;

import java.util.Collection;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.polymap.QiEntity;

import org.polymap.rhei.um.Groupable;

/**
 * The Qi4j implementation of {@link Groupable}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface QiGroupable
        extends QiEntity, Groupable, EntityComposite {
    
    @Optional
    @UseDefaults
    Property<Collection<String>> _groups();
    
    
    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements QiGroupable {
        
    }
    
}
