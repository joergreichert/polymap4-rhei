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

import org.apache.commons.lang3.StringUtils;

/**
 * Checks if the value id null or empty if instance of String.
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NotEmptyValidator
        implements IFormFieldValidator {

     public String validate( Object fieldValue ) {
         if (fieldValue == null) {
             return "Dieses Feld darf nicht leer sein";
         }
         // used for
         else if (fieldValue instanceof String) {
             String str = (String)fieldValue;
             if (str.length() == 0 || StringUtils.containsOnly( str, " \t\n\r" )) {
                 return "Dieses Feld darf nicht leer sein";
             }
         }
        return null;
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
