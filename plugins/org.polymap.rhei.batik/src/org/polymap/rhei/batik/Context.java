/* 
 * polymap.org
 * Copyright (C) 2013-2014, Polymap GmbH. All rights reserved.
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

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.polymap.core.runtime.event.EventFilter;

/**
 * Defines and provides access to an {@link IAppContext} property. Instances of
 * {@link IPanel} are automatically injected with context properties instances.
 * Client code can do this for other objects using
 * {@link IAppContext#propagate(Object)}.
 * <p/>
 * The value of a context property is shared by all instances within the same scope!
 * By default the <b>scope</b> of the property is the <b>package</b> of the
 * {@link IPanel} class. The {@link Scope} annotation can be used to set/change the
 * scope.
 * 
 * @see Scope
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface Context<T> {

    public boolean isPresent();
    
    public void ifPresent( Consumer<T> consumer );
    
    public T get();
    
    public T getOrWait( int time, TimeUnit unit );
    
    public T set( T value );
    
    
    /**
     * Atomically sets the value to the given updated value if the current value
     * {@link Object#equals(Object)} the expected value.
     * 
     * @param expect the expected value
     * @param update the new value
     * @return true if successful. False return indicates that the actual value was
     *         not equal to the expected value.
     */
    public boolean compareAndSet( T expect , T update );
    
    public Class<T> getDeclaredType();
    
    public String getScope();

    public void addListener( Object annotated, EventFilter<PropertyAccessEvent>... filters );

    public boolean removeListener( Object annotated );
}
