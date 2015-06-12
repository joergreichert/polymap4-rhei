/*
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam, and individual contributors as
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
package org.polymap.rhei.field;

import java.util.Map;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Type;

import org.geotools.feature.NameImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyType;

import com.google.common.base.Joiner;

/**
 * Adapter between a POJO Bean and and an OGC {@link Property}. Used by
 * {@link IFormPageProvider} instances to handle complex attributes.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class BeanPropertyAdapter
        implements Property {

    private Object                      bean;
    
    private PropertyDescriptor          delegate;
    
    private boolean                     readOnly;
    
    private String                      prefix;


    public BeanPropertyAdapter( Object bean, String propName ) {
        try {
            this.bean = bean;
            this.delegate = new PropertyDescriptor( propName, bean.getClass() );
        }
        catch (IntrospectionException e) {
            throw new RuntimeException( e );
        }
    }

    protected PropertyDescriptor delegate() {
        return delegate;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public BeanPropertyAdapter setReadOnly( boolean readOnly ) {
        this.readOnly = readOnly;
        return this;
    }

    public Name getName() {
        return new NameImpl( Joiner.on( "_" ).skipNulls().join( prefix, delegate.getName() ) );
    }

    public PropertyType getType() {
        return new AttributeTypeImpl( getName(), delegate.getPropertyType(), false, false, null, null, null );
    }

    public org.opengis.feature.type.PropertyDescriptor getDescriptor() {
        // signal that we are a 'complex' property
        // see FormEditor#doSave() for implementation detail
        return null;
    }

    public Object getValue() {
        try {
            return delegate.getReadMethod().invoke( bean );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    public void setValue( Object value ) {
        if (!readOnly) {
            // check type
            Type propType = delegate.getPropertyType();
            if (propType instanceof Class 
                    && value != null 
                    && !((Class)propType).isAssignableFrom( value.getClass() )) {
                throw new ClassCastException( "Wrong value for Property of type '" + propType + "': " + value.getClass() );
            }
            try {
                delegate.getWriteMethod().invoke( bean, value );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
    }

    public Map<Object, Object> getUserData() {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean isNillable() {
        throw new RuntimeException( "not yet implemented." );
    }

}
