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
package org.polymap.rhei.batik;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.IMemento;

/**
 * Extended version of {@link IMemento}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Memento
        implements IMemento {

    private static Log log = LogFactory.getLog( Memento.class );
    
    private IMemento            delegate;

    public Memento( IMemento delegate ) {
        this.delegate = delegate;
    }

    public Optional<Boolean> optBoolean( String key ) {
        return Optional.ofNullable( getBoolean( key ) );    
    }
    
    public Optional<Float> optFloat( String key ) {
        return Optional.ofNullable( getFloat( key ) );    
    }
    
    public Optional<String> optString( String key ) {
        return Optional.ofNullable( getString( key ) );    
    }
    
    public Memento getOrCreateChild( String type ) {
        return delegate.getChild( type ) != null ? getChild( type ) : createChild( type );
    }
    
    // delegate *******************************************
    
    public Memento createChild( String type ) {
        return new Memento( delegate.createChild( type ) );
    }

    public Memento createChild( String type, String id ) {
        return new Memento( delegate.createChild( type, id ) );
    }

    public Memento getChild( String type ) {
        return new Memento( delegate.getChild( type ) );
    }

    public IMemento[] getChildren( String type ) {
        return delegate.getChildren( type );
    }

    public Float getFloat( String key ) {
        return delegate.getFloat( key );
    }

    public String getType() {
        return delegate.getType();
    }

    public String getID() {
        return delegate.getID();
    }

    public Integer getInteger( String key ) {
        return delegate.getInteger( key );
    }

    public String getString( String key ) {
        return delegate.getString( key );
    }

    public Boolean getBoolean( String key ) {
        return delegate.getBoolean( key );
    }

    public String getTextData() {
        return delegate.getTextData();
    }

    public String[] getAttributeKeys() {
        return delegate.getAttributeKeys();
    }

    public void putFloat( String key, float value ) {
        delegate.putFloat( key, value );
    }

    public void putInteger( String key, int value ) {
        delegate.putInteger( key, value );
    }

    public void putMemento( IMemento memento ) {
        delegate.putMemento( memento );
    }

    public void putString( String key, String value ) {
        delegate.putString( key, value );
    }

    public void putBoolean( String key, boolean value ) {
        delegate.putBoolean( key, value );
    }

    public void putTextData( String data ) {
        delegate.putTextData( data );
    }
    
}
