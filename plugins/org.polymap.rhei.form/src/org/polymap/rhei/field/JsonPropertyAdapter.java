/*
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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

import org.geotools.feature.NameImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.json.JSONObject;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;

import com.google.common.base.Joiner;

/**
 * Adapter between an {@link JSONObject} and and an OGC {@link Property}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class JsonPropertyAdapter
        implements Property {

    private JSONObject                  json;
    
    private boolean                     readOnly;
    
    private String                      prefix;

    private String                      propName;
    
    private Class                       propType;

    /**
     * Constructs a new adapter assuming that the property type is {@link String}.
     * 
     * @param json
     * @param propName
     */
    public JsonPropertyAdapter( JSONObject json, String propName ) {
        this( json, propName, String.class );
    }

    public JsonPropertyAdapter( JSONObject json, String propName, Class propType ) {
        this.json = json;
        this.propName = propName;
        this.propType = propType;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public JsonPropertyAdapter setReadOnly( boolean readOnly ) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public Name getName() {
        return new NameImpl( Joiner.on( "_" ).skipNulls().join( prefix, propName ) );
    }

    @Override
    public PropertyType getType() {
        return new AttributeTypeImpl( getName(), propType, false, false, null, null, null );
    }

    @Override
    public PropertyDescriptor getDescriptor() {
        // signal that we are a 'complex' property
        // see FormEditor#doSave() for implementation detail
        return null;
    }

    @Override
    public Object getValue() {
        Object result = json.opt( propName );
        if (result != null && propType != null && !propType.isAssignableFrom( result.getClass() )) {
            throw new ClassCastException( "Property tpe is: " + propType + ", value type is: " + result.getClass() );
        }
        return result;
    }

    @Override
    public void setValue( Object value ) {
        if (!readOnly) {
            if (value != null && propType != null && !propType.isAssignableFrom( value.getClass() )) {
                throw new ClassCastException( "Property tpe is: " + propType + ", value type is: " + value.getClass() );
            }
            json.put( propName, value );
        }
    }

    @Override
    public Map<Object, Object> getUserData() {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean isNillable() {
        throw new RuntimeException( "not yet implemented." );
    }

}
