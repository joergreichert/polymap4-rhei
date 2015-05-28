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
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Allows for proposals for query strings consisting of multiple terms. For example:
 * "Renate Bies" is transformed into proposal request "Bies", the results are joined
 * with the prefix and checked to see if the proposal actually finds something.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FullQueryProposalDecorator
        extends QueryDecorator {

    public final static String      SEPARATOR = " ";
    
    
    public FullQueryProposalDecorator( FulltextIndex next ) {
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
            String prefix = substringBeforeLast( query, SEPARATOR );
            String term = substringAfterLast( query, SEPARATOR );

            // no separator found -> just pass through 
            if (StringUtils.isEmpty( term )) { 
                return next.propose( query, maxResults, field );
            }
            // search just for the last term in the search
            else {
                // request more than maxResults proposals if prefix present, as we later
                // filter proposals that are not correct for the given prefix
                Iterable<String> results = next.propose( term, maxResults*3, field );

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
                            return !Iterables.isEmpty( next.search( input, 1 ) );
                        }
                        catch (Exception e) {
                            throw new RuntimeException( e );
                        }
                    }
                }), maxResults );
            }
        }
    }

}
