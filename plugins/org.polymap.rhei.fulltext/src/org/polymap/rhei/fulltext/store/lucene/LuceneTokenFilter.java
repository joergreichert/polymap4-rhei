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

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import org.polymap.rhei.fulltext.indexing.FulltextTokenFilter;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LuceneTokenFilter
        extends TokenFilter {

    private TermAttribute                   termAtt;

    private Iterable<FulltextTokenFilter>   filters;

    
    public LuceneTokenFilter( TokenStream in, Iterable<FulltextTokenFilter> filters ) {
        super( in );
        this.filters = filters;
        this.termAtt = addAttribute( TermAttribute.class );
    }


    public final boolean incrementToken() throws IOException {
        if (input.incrementToken()) {

            // XXX use termBuffer() for performance
            String term = termAtt.term();
            String filtered = term;
            for (FulltextTokenFilter filter : filters) {
                filtered = filter.apply( filtered );
            }
            if (filtered != term) {
                termAtt.setTermBuffer( filtered );
                //System.out.println( "   term: " + term + " -> " + newTerm );
            }

            //                final char[] buffer = termAtt.termBuffer();
            //                final int length = termAtt.termLength();
            //                for (int i = 0; i < length; i++)
            //                    buffer[i] = Character.toLowerCase( buffer[i] );

            return true;
        }
        else {
            return false;
        }
    }

}
