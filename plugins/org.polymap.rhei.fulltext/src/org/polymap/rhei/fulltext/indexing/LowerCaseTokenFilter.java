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
package org.polymap.rhei.fulltext.indexing;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.LowerCaseFilter;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import org.polymap.rhei.fulltext.FullTextIndex;
import org.polymap.rhei.fulltext.QueryDecorator;

/**
 * Provides a {@link FullTextTokenFilter} and {@link QueryDecorator} to normalize
 * proposal and query strings to lower case. The proposal results are capitalized.
 * 
 * @see LowerCaseFilter
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LowerCaseTokenFilter
        extends QueryDecorator
        implements FullTextTokenFilter {

    /**
     * Ctor for {@link FullTextTokenFilter}.
     */
    public LowerCaseTokenFilter() {
        super( null );
    }

    /**
     * Ctor for {@link QueryDecorator}.
     */
    public LowerCaseTokenFilter( FullTextIndex next ) {
        super( next );
    }


    @Override
    public Iterable<String> propose( String query, int maxResults ) throws Exception {
        Iterable<String> results = next.propose( query.toLowerCase(), maxResults );
        
        return Iterables.transform( results, new Function<String,String>() {
            public String apply( String input ) {
                return StringUtils.capitalize( input );
            }
        });
    }


    @Override
    public String apply( String term ) {
        return term.toLowerCase();
    }
    
}
