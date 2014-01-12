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
package org.polymap.rhei.um.ui;

import org.apache.commons.lang.StringUtils;

import org.polymap.rhei.field.DelegatingValidator;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NullValidator;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PlzValidator
        extends DelegatingValidator {

    /**
     * Construct with a {@link NullValidator} as delegate.
     */
    public PlzValidator() {
        super( new NullValidator() );
    }

    public PlzValidator( IFormFieldValidator delegate ) {
        super( delegate );
    }

    public String doValidate( Object value ) {
        if (value == null) {
            return null;
        }
        String str = (String)value;
        if (str.length() < 5 || str.length() > 5) {
            return "Geben Sie die Postleitzahl mit 5 Stellen an.";
        }
        else if (!StringUtils.containsOnly( str, "0123456789" )) {
            return "Eine PLZ darf nur Ziffern enthalten.";
        }
        return null;
    }

    @Override
    protected Object doTransform2Model( Object fieldValue ) throws Exception {
        return fieldValue;
    }

    @Override
    protected Object doTransform2Field( Object modelValue ) throws Exception {
        return modelValue;
    }
    
}