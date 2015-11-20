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
package org.polymap.rhei.batik.engine;

import java.util.ArrayDeque;
import java.util.Queue;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.Context;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PanelContextInjector
        implements Runnable {

    private static Log log = LogFactory.getLog( PanelContextInjector.class );

    private Object              panel;

    private DefaultAppContext   context;


    public PanelContextInjector( Object panel, DefaultAppContext context ) {
        this.panel = panel;
        this.context = context;
    }

    @Override
    public void run() {
        Queue<Class> types = new ArrayDeque( 16 );
        types.add( panel.getClass() );

        while (!types.isEmpty()) {
            Class type = types.remove();
            if (type.getSuperclass() != null) {
                types.add( type.getSuperclass() );
            }

            for (Field f : type.getDeclaredFields()) {
                // ContextProperty
                if (Context.class.isAssignableFrom( f.getType() )) {
                    f.setAccessible( true );
                    Type ftype = f.getGenericType();
                    if (ftype instanceof ParameterizedType) {
                        // set
                        try {
                            f.set( panel, new ContextPropertyInstance( f, context ) );
                            log.debug( "injected: " + f.getName() + " (" + panel.getClass().getSimpleName() + ")" );
                            continue;
                        }
                        catch (Exception e) {
                            throw new RuntimeException( e );
                        }
                    }
                    else {
                        throw new IllegalStateException( "ContextProperty has no type param: " + f.getName() );
                    }
                }
                
                // @Context annotation
                Scope annotation = f.getAnnotation( Scope.class );
                if (annotation != null) {
                    f.setAccessible( true );
                    throw new UnsupportedOperationException( "Injecting context property as direct member." );

//                    Object value = context.get( f.getType() );
//
//                    try {
//                        f.set( panel, value );
//                        log.info( "injected: " + f.getName() + " <- " + value );
//                    }
//                    catch (Exception e) {
//                        throw new RuntimeException( e );
//                    }
                }
            }
        }
    }
    
}
