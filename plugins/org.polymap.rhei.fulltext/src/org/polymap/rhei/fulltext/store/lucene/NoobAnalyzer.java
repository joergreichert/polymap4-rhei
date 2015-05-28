/* 
 * polymap.org
 * Copyright (C) 2009-2014, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.fulltext.store.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.AttributeSource;

/**
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class NoobAnalyzer
        extends Analyzer {

    private LuceneFulltextIndex     index;


    public NoobAnalyzer( LuceneFulltextIndex index ) {
        this.index = index;
    }


    @Override
    public TokenStream tokenStream( String fieldName, Reader reader ) {
        Tokenizer tokenStream = new NoobTokenizer( reader );
        return tokenStream;
//        TokenStream result = new LuceneTokenFilter( tokenStream, index.filters() );
//        return result;
    }

    
    /**
     * 
     */
    final class NoobTokenizer
            extends CharTokenizer {

        public NoobTokenizer( Reader in ) {
            super( LuceneFulltextIndex.LUCENE_VERSION, in );
        }

        public NoobTokenizer( AttributeSource source, Reader in ) {
            super( LuceneFulltextIndex.LUCENE_VERSION, source, in );
        }

        public NoobTokenizer( AttributeFactory factory, Reader in ) {
            super( LuceneFulltextIndex.LUCENE_VERSION, factory, in );
        }

        @Override
        protected boolean isTokenChar( int c ) {
            return true;
        }
    }

}
