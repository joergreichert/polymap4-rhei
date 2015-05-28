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

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.polymap.QiEntity;
import org.qi4j.polymap.event.ModelChangeSupport;
import org.qi4j.polymap.event.PropertyChangeSupport;

import org.polymap.rhei.um.User;

/**
 * The Qi4j implementation of {@link User}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Concerns( {
    PropertyChangeSupport.Concern.class
})
@Mixins( {
    QiUser.Mixin.class, 
    PropertyChangeSupport.Mixin.class,
    ModelChangeSupport.Mixin.class,
    QiEntity.Mixin.class,
    QiPerson.Mixin.class,
    QiGroupable.Mixin.class
})
public interface QiUser
        extends User, QiGroupable, QiEntity, PropertyChangeSupport, EntityComposite {
    
    @Optional
    Property<String>            _username();
    
    @Optional
    Property<String>            _passwordHash();
    
    @Optional
    Property<String>            _company();
    
//    /**
//     * Wurde die Identität des Nutzers überprüft?
//     */
//    @UseDefaults
//    Property<Boolean>           authentifiziert();
    
    
    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements QiUser {
        
        @Override
        public org.polymap.rhei.um.Property<String> passwordHash() {
            return QiProperty.create( _passwordHash() );
        }

        @Override
        public org.polymap.rhei.um.Property<String> username() {
            //System.out.println( "    property username: " + _username().get() );
            return QiProperty.create( _username() );
        }

        @Override
        public org.polymap.rhei.um.Property<String> company() {
            //System.out.println( "    property company: " + _company().get() );
            return QiProperty.create( _company() );
        }

        public String getLabelString( String sep ) {
            throw new RuntimeException( "not yet implemented" );
        }
        
    }
    
}
