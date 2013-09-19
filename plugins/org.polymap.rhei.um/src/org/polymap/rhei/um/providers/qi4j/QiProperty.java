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
package org.polymap.rhei.um.providers.qi4j;

import org.qi4j.api.property.Property;

/**
 * Adapter between user property and entity/Qi4j property.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class QiProperty<T>
        implements org.polymap.rhei.um.Property<T>{

    public static <T> org.polymap.rhei.um.Property<T> create( Property<T> delegate ) {
        return new QiProperty( delegate );
    }

    
    // instance *******************************************
    
    private Property<T>         delegate;
    
    
    public QiProperty( Property<T> delegate ) {
        assert delegate != null;
        this.delegate = delegate;
    }

    @Override
    public T get() {
        return delegate.get();
    }

    @Override
    public void set( T value ) {
        delegate.set( value );
    }

    @Override
    public Class<T> type() {
        return (Class<T>)delegate.type();
    }

    @Override
    public String name() {
        String qiName = delegate.qualifiedName().name();
        return qiName.startsWith( "_" ) ? qiName.substring( 1 ) : qiName;
    }
    
}
