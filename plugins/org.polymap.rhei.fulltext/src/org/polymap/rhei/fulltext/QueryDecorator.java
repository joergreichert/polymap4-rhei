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
 * Allows to decorate/transform search/proposal queries and results. Query decorators
 * can be applied to existing {@link FullTextIndex} instances on demand when it is
 * needed.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class QueryDecorator
        implements FullTextIndex {

    protected FullTextIndex         next;
    
    
    public QueryDecorator( FullTextIndex next ) {
        this.next = next;
    }
    
    /**
     * Search for completions/proposals for the given string. 
     * <p/>
     * If param <b>field</b> is not null then the proposal does not use the fulltext
     * but the content of just this field. Such content is not filtered and/or
     * transformed but stored as one single term.
     * 
     * @param query
     * @param maxResults The maximun number of entries in the result set.
     * @param field Allows to specify the field to use for proposal. By default
     *        (null) a fulltext search including the content of all fields is
     *        performed.
     */
    @Override
    public Iterable<String> propose( String query, int maxResults, String field )
            throws Exception {
        return next.propose( query, maxResults, field );
    }
    
    /**
     * Do a fulltext search for the given query.
     * <p/>
     * 
     * @param query The query string. Depending on the backend implementation this
     *        might be a <b>complex query</b>! Lucene for example allows to
     *        explicitly specify the fields to use for the query.
     * @param maxResults The maximun number of entries in the result set.
     */
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
