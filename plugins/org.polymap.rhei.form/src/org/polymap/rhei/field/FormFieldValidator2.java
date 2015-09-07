/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.field;

import java.util.Optional;

/**
 * A {@link IFormFieldValidator} that allows to work with {@link Optional} field and
 * model values instead of <code>null</code>.
 *
 * @see Validators
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FormFieldValidator2<F,M>
        implements IFormFieldValidator<F,M> {

    /**
     * Check the given user provided value for validity.
     * 
     * @param value
     * @return The optional error message, or empty if if the value is invalid.
     */
    public abstract Optional<String> validate( Optional<F> fieldValue );
    
    public final String validate( F fieldValue ) {
        return validate( Optional.ofNullable( fieldValue ) ).get();
    }
    
    /**
     * Transforms the given user input value to model value.
     * 
     * @param fieldValue The user input value to transform, might be null.
     * @return Transformed value, or empty if fieldValue is empty.
     * @throws Exception
     */
    public abstract Optional<M> transform2Model( Optional<F> fieldValue ) throws Exception;
    
    public final M transform2Model( F fieldValue ) throws Exception {
        return transform2Model( Optional.ofNullable( fieldValue ) ).get();
    }
    
    /**
     * 
     *
     * @param modelValue
     * @return The transformed value.
     * @throws Exception
     */
    public abstract Optional<F> transform2Field( Optional<M> modelValue ) throws Exception;

    public final F transform2Field( M modelValue ) throws Exception {
        return transform2Field( Optional.ofNullable( modelValue ) ).get();
    }
    
}
