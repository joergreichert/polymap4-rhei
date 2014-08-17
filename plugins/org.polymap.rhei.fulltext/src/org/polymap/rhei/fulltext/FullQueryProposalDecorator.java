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

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.limit;
import static com.google.common.collect.Iterables.transform;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FullQueryProposalDecorator
        extends QueryDecorator {

    public final static String      SEPARATOR = " ";
    
    
    public FullQueryProposalDecorator( FullTextIndex next ) {
        super( next );
    }

    @Override
    public Iterable<String> propose( String query, int maxResults, String field )
            throws Exception {
        // fields (other than the _analyzed_ field) are stored as single term
        if (field != null) {
            return next.propose( query, maxResults, field );
        }
        else {
            // search just for the last term in the search
            String term = query;
            String prefix = null;
            if (StringUtils.contains( query, SEPARATOR )) { 
                prefix = StringUtils.substringBeforeLast( term, SEPARATOR );
                term = StringUtils.substringAfterLast( term, SEPARATOR );
            }

            // next;
            // request more than maxResults proposals if prefix present, as we later
            // filter proposals that are not correct for the given prefix
            Iterable<String> results = next.propose( term, prefix == null ? maxResults : maxResults*3, field );

            // join prefix and proposal
            final String finalPrefix = prefix;
            results = transform( results, new Function<String,String>() {
                public String apply( String input ) {
                    return Joiner.on( SEPARATOR ).skipNulls().join( finalPrefix, input );
                }
            });

            // check if joined proposal actually finds something
            return limit( filter( results, new Predicate<String>() {
                public boolean apply( String input ) {
                    try {
                        return finalPrefix == null || !Iterables.isEmpty( next.search( input, 1 ) );
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                }
            }), maxResults );
        }
    }

}
