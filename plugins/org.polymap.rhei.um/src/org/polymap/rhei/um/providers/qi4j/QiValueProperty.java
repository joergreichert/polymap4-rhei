/*
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;

/**
 * Adapter between user property and entity/Qi4j value property.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class QiValueProperty<T>
        implements org.polymap.rhei.um.Property<T> {

    public static <T> org.polymap.rhei.um.Property<T> create( Property<T> delegate, Property<? extends ValueComposite> valueProperty ) {
        return new QiValueProperty( delegate, valueProperty );
    }

    
    // instance *******************************************

    private Property                delegate;
    
    private Property<ValueComposite> valueProperty;

    private String                  prefix;
    
    private boolean                 readOnly;


    /**
     * 
     * 
     * @param delegate The property of a {@link ValueComposite} to delegate to.
     * @param valueProperty The property of the {@link EntityComposite} that holds the {@link ValueComposite}.
     */
    public QiValueProperty( Property delegate, Property<? extends ValueComposite> valueProperty ) {
        assert valueProperty != null && delegate != null;
        this.valueProperty = (Property<ValueComposite>)valueProperty;
        this.delegate = delegate;
    }

    @Override
    public T get() {
        return (T)delegate.get();
    }

    @Override
    public void set( final T value ) {
        if (!readOnly) {
            ValueComposite oldValue = valueProperty.get();
            ValueBuilder<ValueComposite> vbuilder = oldValue.buildWith();
            final ValueComposite newValue = vbuilder.prototype();
            
            // copy/set properties
            oldValue.state().visitProperties( new StateHolder.StateVisitor() {
                public void visitProperty( QualifiedName name, Object propValue ) {
                    newValue.state().getProperty( name ).set( 
                            name.equals( delegate.qualifiedName() ) ? value : propValue );
                }
            });
            // set value property
            valueProperty.set( vbuilder.newInstance() );
        }
    }
    
    @Override
    public Class type() {
        return (Class<?>)delegate.type();
    }

    @Override
    public String name() {
        String qiName = delegate.qualifiedName().name();
        return qiName.startsWith( "_" ) ? qiName.substring( 1 ) : qiName;
    }

    protected Property delegate() {
        return delegate;
    }
    
}
