/* 
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.batik.internal;

import java.util.Arrays;

import com.google.common.base.Predicates;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.PropertyAccessEvent;
import org.polymap.rhei.batik.PropertyAccessEvent.TYPE;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ContextPropertyInstance
        implements ContextProperty {

    private DefaultAppContext       context;
    
    private Class<?>                declaredType;
    
    private String                  scope;
    

    public ContextPropertyInstance( DefaultAppContext context, Class<?> declaredType, String scope ) {
        this.context = context;
        this.declaredType = declaredType;
        this.scope = scope;
    }

    @Override
    public Object get() {
        EventManager.instance().publish( new PropertyAccessEvent( this, TYPE.GET ) );
        return context.getPropertyValue( this );
    }

    @Override
    public Object set( Object value ) {
        EventManager.instance().publish( new PropertyAccessEvent( this, TYPE.SET ) );
        return context.setPropertyValue( this, value );
    }

    @Override
    public Class getDeclaredType() {
        return declaredType;
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public void addListener( Object annotated, final EventFilter... filters ) {
        EventManager.instance().subscribe( annotated, new EventFilter<PropertyAccessEvent>() {
            public boolean apply( PropertyAccessEvent input ) {
                ContextProperty src = input.getSource();
                return src.getDeclaredType().equals( getDeclaredType() )
                        && src.getScope().equals( getScope() )
                        && Predicates.and( Arrays.asList( (EventFilter<PropertyAccessEvent>[])filters ) ).apply( input );
            }
        });
    }

    @Override
    public boolean removeListener( Object annotated ) {
        return EventManager.instance().unsubscribe( annotated );
    }

    @Override
    public String toString() {
        return "ContextPropertyInstance[scope=" + scope + ", type=" + declaredType + "]";
    }
    
}