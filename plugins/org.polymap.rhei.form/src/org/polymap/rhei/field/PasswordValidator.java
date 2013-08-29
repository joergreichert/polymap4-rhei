/* 
 * polymap.org
 * Copyright (C) 2013, Falko Br‰utigam. All rights reserved.
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

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br‰utigam</a>
 */
public class PasswordValidator
        implements IFormFieldValidator {

    public static final Pattern pattern = Pattern.compile( 
            "^" +                // start-of-string
            "(?=.*[0-9])" +      // a digit must occur at least once
            "(?=.*[a-z])" +      // a lower case letter must occur at least once
            "(?=.*[A-Z])" +      // an upper case letter must occur at least once
           // "(?=.*[@#$%^&+=])" + // a special character must occur at least once
            "(?=\\S+$)" +        // no whitespace allowed in the entire string
            ".{8,}" +            // anything, at least eight places though
            "$" );               // end-of-string
    
    @Override
    public String validate( Object fieldValue ) {
        Matcher matcher = pattern.matcher( fieldValue != null ? fieldValue.toString() : "_dontMatch_" );
        return !matcher.matches() ? "Ein Passwort muss mindestens enthalten: eine Ziffer, einen Kleinbuchstaben, einen Groﬂbuchstaben und mindestens 8 Zeichen haben" : null; 
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
