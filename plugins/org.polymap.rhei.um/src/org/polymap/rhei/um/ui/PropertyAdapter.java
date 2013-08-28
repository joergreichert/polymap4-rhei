/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and individual contributors as
 * indicated by the @authors tag.
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
package org.polymap.rhei.um.ui;

import java.util.Map;

import java.lang.reflect.Type;

import org.geotools.feature.NameImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;

import com.google.common.base.Joiner;

/**
 * Adapter between property types.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class PropertyAdapter
        implements Property {

    private org.polymap.rhei.um.Property delegate;

    private String                       prefix;

    private boolean                      readOnly;


    public PropertyAdapter( org.polymap.rhei.um.Property delegate ) {
        this.delegate = delegate;
    }

    public PropertyAdapter( String prefix, org.polymap.rhei.um.Property delegate ) {
        this.delegate = delegate;
        this.prefix = prefix;
    }

    protected org.polymap.rhei.um.Property delegate() {
        return delegate;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public PropertyAdapter setReadOnly( boolean readOnly ) {
        this.readOnly = readOnly;
        return this;
    }

    public Name getName() {
        String name = delegate.name();
        // remove trailing "_" from the Qi4j properties
        name = name.startsWith( "_" ) ? name.substring( 1 ) : name;
        return new NameImpl( Joiner.on( "_" ).skipNulls().join( prefix, name ) );
    }

    public PropertyType getType() {
        return new AttributeTypeImpl( getName(), delegate.type(), false, false, null, null, null );
    }

    public PropertyDescriptor getDescriptor() {
        // signal that we are a 'complex' property
        // see FormEditor#doSave() for implementation detail
        return null;
    }

    public Object getValue() {
        return delegate.get();
    }

    public void setValue( Object value ) {
        if (!readOnly) {
            // check type
            Type propType = delegate.type();
            if (propType instanceof Class 
                    && value != null 
                    && !((Class)propType).isAssignableFrom( value.getClass() )) {
                throw new ClassCastException( "Wrong value for Property of type '" + propType + "': " + value.getClass() );
            }
            delegate.set( value );
        }
    }

    public Map<Object, Object> getUserData() {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean isNillable() {
        throw new RuntimeException( "not yet implemented." );
    }

}
