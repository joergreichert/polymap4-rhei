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
package org.polymap.rhei.fulltext.store.lucene;

import static com.google.common.collect.Iterables.limit;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.TreeMap;

import java.io.File;
import java.io.IOException;

import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.Version;

import com.google.common.base.Function;

import org.polymap.rhei.fulltext.FulltextIndex;
import org.polymap.rhei.fulltext.update.UpdateableFulltextIndex;

import org.polymap.recordstore.lucene.GeometryValueCoder;
import org.polymap.recordstore.lucene.LuceneRecordQuery;
import org.polymap.recordstore.lucene.LuceneRecordState;
import org.polymap.recordstore.lucene.LuceneRecordStore;
import org.polymap.recordstore.lucene.StringValueCoder;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LuceneFulltextIndex
        extends UpdateableFulltextIndex
        implements FulltextIndex {

    private static Log log = LogFactory.getLog( LuceneFulltextIndex.class );

    /** The Lucene version we are using. */
    public final static Version     LUCENE_VERSION = Version.LUCENE_36;

    public final static String      FIELD_ANALYZED = "_analyzed_";
    
    protected LuceneRecordStore     store;

    private LuceneAnalyzer          analyzer;
    

    public LuceneFulltextIndex( File dir ) throws IOException {
        store = dir != null 
                ? new LuceneRecordStore( dir, false )
                : new LuceneRecordStore();
                
        store.getValueCoders().clear();
        // StringValueCoder is *last*
        store.getValueCoders().addValueCoder( new StringValueCoder() );
        store.getValueCoders().addValueCoder( new AnalyzedStringValueCoder() );
        store.getValueCoders().addValueCoder( new GeometryValueCoder() );
        
        analyzer = new LuceneAnalyzer( this );
        store.setAnalyzer( analyzer );
    }


    @Override
    public void close() {
        if (store != null) {
            store.close();
            store = null;
        }
    }


    @Override
    protected void finalize() throws Throwable {
        close();
    }


    @Override
    public boolean isClosed() {
        return store != null;
    }


    @Override
    public boolean isEmpty() {
        long storeSize = store.storeSizeInByte();
        log.info( "Store size: " + storeSize );
        return storeSize < 100;
    }


    @Override
    public Iterable<String> propose( String term, int maxResults, String field )
            throws Exception {
        // no proposals for empty term
        if (term.length() == 0) {
            return Collections.EMPTY_LIST;
        }
        IndexSearcher searcher = store.getIndexSearcher();
        TermEnum terms = searcher.getIndexReader().terms( 
                new Term( field != null ? field : FIELD_ANALYZED, term ) );
        try {
            // sort descending; accept equal keys
            TreeMap<Integer,String> result = new TreeMap( new Comparator<Integer>() {
                public int compare( Integer o1, Integer o2 ) {
                    return o1.equals( o2 ) ? -1 : -o1.compareTo( o2 );
                }
            });
            // sort
            for (int i=0; i<maxResults*3; i++) {
                String proposalTerm = terms.term().text();
                int docFreq = terms.docFreq();
                if (!proposalTerm.startsWith( term )) {
                    break;
                }
                log.info( "Proposal: term: " + proposalTerm + ", docFreq: " + docFreq );
                result.put( docFreq, proposalTerm );
                if (!terms.next()) {
                    break;
                }
            }
            // take first maxResults
            return limit( result.values(), maxResults );
        }
        catch (Exception e) {
            log.warn( "", e );
            return Collections.EMPTY_LIST;
        }
        finally {
            terms.close();
        }
    }


    @Override
    public Iterable<JSONObject> search( String queryStr, int maxResults )
            throws Exception {
        // parse query;
        // for queries containing ":" use no/simple analyzer as ordinary fields
        // are not analyzed before storing (see StringValueCoder for example) 
        QueryParser parser = isComplexQuery( queryStr )
                ? new QueryParser( LUCENE_VERSION, FIELD_ANALYZED, new NoobAnalyzer( this ) )
                : new ComplexPhraseQueryParser( LUCENE_VERSION, FIELD_ANALYZED, analyzer );
                
        parser.setAllowLeadingWildcard( true );
        parser.setLowercaseExpandedTerms( false );
        parser.setDefaultOperator( QueryParser.AND_OPERATOR );
        Query query = parser.parse( queryStr );
        log.info( "    ===> Lucene query: " + query );

        maxResults = maxResults == -1 || maxResults > LuceneRecordQuery.BIG_BUT_NOT_MAX_VALUE 
                ? LuceneRecordQuery.BIG_BUT_NOT_MAX_VALUE : maxResults;
        
//        Sort asc = new Sort( new SortField( FIELD_TITLE, SortField.STRING ) );
        IndexSearcher searcher = store.getIndexSearcher();
        ScoreDoc[] hits = searcher.search( query, null, maxResults ).scoreDocs;
        
        // transform result: scroreDoc -> JSONObject
        return transform( asList( hits ), new Function<ScoreDoc,JSONObject>() {
            public JSONObject apply( ScoreDoc input ) {
                try {
                    LuceneRecordState record = store.get( input.doc, null );
                    JSONObject result = new JSONObject();                
                    for (Entry<String,Object> entry : record) {
                        result.put( entry.getKey(), entry.getValue() );
                    }
                    return result;
                }
                catch (Exception e) {
                    throw new RuntimeException( e );
                }
            }
        });
    }


    public boolean isComplexQuery( String query ) {
        // XXX ':' might occur inside a "term" 
        return query != null && query.contains( ":" );
    }


    @Override
    public Updater prepareUpdate() {
        return new LuceneUpdater( this );
    }
    
}
