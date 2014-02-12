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

import org.json.JSONObject;

import static org.apache.commons.lang.StringUtils.*;

/**
 * Append (Lucene) wildcard '*' to the search query. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WildcardQueryDecorator
        extends QueryDecorator {

    public WildcardQueryDecorator( FullTextIndex next ) {
        super( next );
    }

    @Override
    public Iterable<JSONObject> search( String query, int maxResults ) throws Exception {
        if (containsNone( query, "*?~\":" )
                && !containsIgnoreCase( query, " OR " )
                && !containsIgnoreCase( query, " AND " )) {
            query = query + "*";
        }
        
        return next.search( query, maxResults );
    }
    
}
