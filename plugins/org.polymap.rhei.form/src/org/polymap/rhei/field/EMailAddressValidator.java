/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultString;

/**
 * Validates against a regex pattern that ensures email addresses like: a@pl.de.
 * An empty field value is valid too.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EMailAddressValidator
        extends Configurable
        implements IFormFieldValidator {

    public static final Pattern pattern = Pattern.compile( "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE );
    
    /**
     * The message to be returned if validation fails.
     */
    @DefaultString("Not a valid email address")
    public Config2<EMailAddressValidator,String>    msg;
    
    @Override
    public String validate( Object fieldValue ) {
        if (fieldValue == null || fieldValue.toString().length() == 0) {
            return null;
        }
        else {
            Matcher matcher = pattern.matcher( fieldValue.toString() );
            return !matcher.matches() ? msg.get() /*+ fieldValue*/ : null;
        }
    }

    @Override
    public Object transform2Model( Object fieldValue ) throws Exception {
        return fieldValue;
    }

    @Override
    public Object transform2Field( Object modelValue ) throws Exception {
        return modelValue;
    }
    
}
