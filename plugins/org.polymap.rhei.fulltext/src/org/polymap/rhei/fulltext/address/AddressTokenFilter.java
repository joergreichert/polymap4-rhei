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
package org.polymap.rhei.fulltext.address;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.fulltext.indexing.FulltextTokenFilter;

/**
 * Normalizes address fields.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AddressTokenFilter
        implements FulltextTokenFilter {

    private static Log log = LogFactory.getLog( AddressTokenFilter.class );


    @Override
    public String apply( String term ) {
        if (term.endsWith( "str" )) {
            return StringUtils.substringBefore( term, "str" );
        }
        else if (term.endsWith( "strasse" )) {
            return StringUtils.substringBefore( term, "strasse" );
        }
        else if (term.endsWith( "straße" )) {
            return StringUtils.substringBefore( term, "straße" );
        }
        return term;

        //                final char[] buffer = termAtt.termBuffer();
        //                final int length = termAtt.termLength();
        //                for (int i = 0; i < length; i++)
        //                    buffer[i] = Character.toLowerCase( buffer[i] );
    }
    
}
