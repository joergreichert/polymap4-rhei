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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.FluentIterable;

import org.polymap.core.runtime.Timer;

/**
 * Logs results and times of {@link #propose(String, int)} and {@link #search(String, int)}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LogQueryDecorator
        extends QueryDecorator {

    private static Log log = LogFactory.getLog( LogQueryDecorator.class );


    public LogQueryDecorator( FullTextIndex next ) {
        super( next );
    }

    
    @Override
    public Iterable<String> propose( String query, int maxResults, String field ) throws Exception {
        Timer timer = new Timer();
        log.info( "Propose: " + query + " (field=" + field + ")" );
        Iterable<String> results = next.propose( query, maxResults, field );
        log.info( "Propose: " + FluentIterable.from( results ).size() + " (" + timer.elapsedTime() + "ms)" );
        return results;
    }

    
    @Override
    public Iterable<JSONObject> search( String query, int maxResults ) throws Exception {
        Timer timer = new Timer();
        log.info( "Search: " + query );
        Iterable<JSONObject> results = next.search( query, maxResults );
        log.info( "Search: " + FluentIterable.from( results ).size() + " (" + timer.elapsedTime() + "ms)" );
        return results;
    }
    
}
