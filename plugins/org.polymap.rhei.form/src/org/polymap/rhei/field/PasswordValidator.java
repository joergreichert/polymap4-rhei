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

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.DefaultInt;
import org.polymap.core.runtime.config.Mandatory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br‰utigam</a>
 */
public class PasswordValidator
        extends Configurable
        implements IFormFieldValidator {
    
    @Mandatory
    @DefaultBoolean(true)
    public Config2<PasswordValidator,Boolean>   oneDigit;
    
    @Mandatory
    @DefaultBoolean(true)
    public Config2<PasswordValidator,Boolean>   oneLowerCase;
    
    @Mandatory
    @DefaultBoolean(true)
    public Config2<PasswordValidator,Boolean>   oneUpperCase;
    
    @Mandatory
    @DefaultBoolean(false)
    public Config2<PasswordValidator,Boolean>   oneSpecial;
    
    @Mandatory
    @DefaultBoolean(true)
    public Config2<PasswordValidator,Boolean>   noWhitespace;

    @Mandatory
    @DefaultInt(8)
    public Config2<PasswordValidator,Integer>   minLength;

    private Pattern                             pattern;
    

    /**
     * Constraucts a new instance with default settings.
     */
    public PasswordValidator() {
        pattern = Pattern.compile(
                "^" +   
                (oneDigit.get()     ? "(?=.*[0-9])" : "") +
                (oneLowerCase.get() ? "(?=.*[a-z])" : "") +
                (oneUpperCase.get() ? "(?=.*[A-Z])" : "") +
                (oneSpecial.get()   ? "(?=.*[@#$%^&+=])" : "") +
                (noWhitespace.get() ? "(?=\\S+$)" : "") +
                ".{" + minLength.get() + ",}" +
                "$" );
    }

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
