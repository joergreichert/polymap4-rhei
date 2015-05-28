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

import static com.google.common.collect.Iterables.all;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.limit;
import static com.google.common.collect.Iterables.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * Allows multiple {@link FulltextIndex} instances to be queried.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SearchDispatcher
        implements FulltextIndex {

    private static Log log = LogFactory.getLog( SearchDispatcher.class );

    private List<FulltextIndex>     searchers = new ArrayList();
    
    private ExecutorService         executorService;
    
    
    public SearchDispatcher( FulltextIndex[] indexes ) {
        searchers.addAll( Arrays.asList( indexes ) );
    }

    @Override
    public void close() {
        assert !isClosed() : "FulltextIndex is closed already.";
        for (FulltextIndex index : searchers) {
            index.close();
        }
        searchers = null;
    }
        
    @Override
    public boolean isClosed() {
        return searchers == null;
    }

    
    @Override
    protected void finalize() throws Throwable {
        if (!isClosed()) {
            close();
        }
    }

    @Override
    public boolean isEmpty() {
        return all( searchers, new Predicate<FulltextIndex>() {
            public boolean apply( FulltextIndex input ) { return input.isEmpty(); }
        });
    }

    @Override
    public Iterable<String> propose( final String term, final int maxResults, final String field ) throws Exception {
        // call searches in separate threads
        // XXX score results
        List<Future<List<String>>> results = new ArrayList();
        for (final FulltextIndex searcher : searchers) {
            results.add( executorService.submit( new Callable<List<String>>() {
                @Override
                public List<String> call() throws Exception {
                    log.info( "Searcher started: " + searcher.getClass().getSimpleName() );
                    Iterable<String> records = searcher.propose( term, maxResults, field );
                    
                    // use real list (not Iterables) in order to make sure
                    // this processing is done inside the thread
                    List<String> result = new ArrayList( maxResults );
                    Iterator<String> it = records.iterator();
                    while (it.hasNext() && result.size() <= maxResults) {
                        String record = it.next();
                        // has the record any result anyway?
                        if (StringUtils.containsNone( term, SEPARATOR_CHARS ) 
                                || !search( record, 1 ).iterator().hasNext()) {
                            result.add( record );
                        }
                    }
                    return result;
                }                
            }));
        }
        
        // wait for threads; concat all records
        return concat( transform( results, new Function<Future<List<String>>,List<String>>() {
            public List<String> apply( Future<List<String>> future ) {
                try {
                    return future.get();
                }
                catch (Exception e) {
                    log.warn( "", e );
                    return new ArrayList();
                }
            }
        }));
    }


    @Override
    public Iterable<JSONObject> search( final String term, final int maxResults ) throws Exception {
        // call searches in separate threads
        // XXX score results
        List<Future<Iterable<JSONObject>>> results = new ArrayList();
        for (final FulltextIndex searcher : searchers) {
            results.add( executorService.submit( new Callable<Iterable<JSONObject>>() {
                @Override
                public Iterable<JSONObject> call() throws Exception {
                    log.debug( "Searcher started: " + searcher.getClass().getSimpleName() );
                    return searcher.search( term, maxResults );
                }                
            }));
        }
        
        // wait for threads; concat all SearchResults, limit to maxResults
        return limit( concat( transform( results, new Function<Future<Iterable<JSONObject>>,Iterable<JSONObject>>() {
            public Iterable<JSONObject> apply( Future<Iterable<JSONObject>> future ) {
                try {
                    return future.get();
                }
                catch (Exception e) {
                    log.warn( "", e );
                    return new ArrayList();
                }
            }
        })), maxResults );
    }

}
