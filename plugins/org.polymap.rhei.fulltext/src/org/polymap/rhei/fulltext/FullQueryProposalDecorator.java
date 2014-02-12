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
package org.polymap.rhei.fulltext;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FullQueryProposalDecorator
        extends QueryDecorator {

    public final static String      SEP = " ";
    
    
    public FullQueryProposalDecorator( FullTextIndex next ) {
        super( next );
    }

    @Override
    public Iterable<String> propose( String query, int maxResults )
            throws Exception {
        // search for the last term in the search
        String term = query;
        String prefix = null;
        if (StringUtils.contains( query, SEP )) { 
            prefix = StringUtils.substringBeforeLast( term, SEP );
            term = StringUtils.substringAfterLast( term, SEP );
        }
        // next
        Iterable<String> results = next.propose( term, maxResults );
        //
        final String finalPrefix = prefix;
        return Iterables.transform( results, new Function<String,String>() {
            public String apply( String input ) {
                return Joiner.on( SEP ).skipNulls().join( finalPrefix, input );
            }
        });
    }

}
