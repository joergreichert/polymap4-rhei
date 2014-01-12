/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.field;

/**
 * The abstract base class of a 'stackable' validator. This implementation asks the
 * delegate validator first.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DelegatingValidator
        implements IFormFieldValidator {

    private IFormFieldValidator     delegate;
    
    
    protected DelegatingValidator( IFormFieldValidator delegate ) {
        assert delegate != null : "Null delegate is not allowed. Explicitly use a NullValidator.";
        this.delegate = delegate;
    }

    protected abstract String doValidate( Object fieldValue );
    
    protected abstract Object doTransform2Model( Object fieldValue ) throws Exception;

    protected abstract Object doTransform2Field( Object modelValue ) throws Exception;

    @Override
    public final String validate( Object fieldValue ) {
        String result = delegate.validate( fieldValue );
        return result != null ? result : doValidate( fieldValue );
    }


    @Override
    public final Object transform2Model( Object fieldValue ) throws Exception {
        return doTransform2Model( delegate.transform2Model( fieldValue ) );
    }


    @Override
    public Object transform2Field( Object modelValue ) throws Exception {
        return doTransform2Field( delegate.transform2Field( modelValue ) );
    }
    
}
