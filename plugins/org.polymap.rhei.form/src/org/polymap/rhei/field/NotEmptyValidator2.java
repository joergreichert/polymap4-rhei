/*
 * polymap.org 
 * Copyright (C) 2013-2014 Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.rhei.field;

import java.util.Optional;

/**
 * Checks if the value id null or empty if instance of String.
 * <p/>
 * This is intended to be subclassed by client code, for plain not-empty check use
 * the {@link NotEmptyValidator}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class NotEmptyValidator2<F,M>
        extends FormFieldValidator2<F,M> {

    private NotEmptyValidator<F,M>      delegate = new NotEmptyValidator<F,M>();
    
    
    @Override
    public Optional<String> validate( Optional<F> fieldValue ) {
        return Optional.ofNullable( delegate.validate( fieldValue.get() ) );
    }


    @Override
    public Optional<M> transform2Model( Optional<F> fieldValue ) throws Exception {
        return Optional.ofNullable( delegate.transform2Model( fieldValue.get() ) );
    }


    @Override
    public Optional<F> transform2Field( Optional<M> modelValue ) throws Exception {
        return Optional.ofNullable( delegate.transform2Field( modelValue.get() ) );
    }


}
