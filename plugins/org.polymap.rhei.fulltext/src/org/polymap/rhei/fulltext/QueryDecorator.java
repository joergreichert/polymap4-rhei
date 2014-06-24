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

/**
 * Allows to decorate/transform search queries and results.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class QueryDecorator
        implements FullTextIndex {

    protected FullTextIndex         next;
    
    
    public QueryDecorator( FullTextIndex next ) {
        this.next = next;
    }

    @Override
    public Iterable<String> propose( String query, int maxResults, String field )
            throws Exception {
        return next.propose( query, maxResults, field );
    }

    @Override
    public Iterable<JSONObject> search( String query, int maxResults )
            throws Exception {
        return next.search( query, maxResults );
    }

    @Override
    public void close() {
        next.close();
    }

    @Override
    public boolean isClosed() {
        return next.isClosed();
    }

    @Override
    public boolean isEmpty() {
        return next.isEmpty();
    }

}
