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
package org.polymap.rhei.fulltext.update;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import com.vividsolutions.jts.geom.Geometry;

import org.polymap.rhei.fulltext.FulltextIndex;
import org.polymap.rhei.fulltext.indexing.FeatureTransformer;
import org.polymap.rhei.fulltext.indexing.FulltextTokenFilter;
import org.polymap.rhei.fulltext.indexing.FulltextTokenizer;
import org.polymap.rhei.fulltext.indexing.StandardTokenizer;

/**
 * Update SPI of a full-text index.
 * <p>
 * The {@link #tokenizer()} and filters are used to build the full text index. The
 * raw content of the fields is stored in the index too but the content is not
 * tokenized/analysed/filterd but stored as single term. The behaviour depends on the
 * particular implementation.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class UpdateableFulltextIndex
        implements FulltextIndex {

    protected FulltextTokenizer         tokenizer = new StandardTokenizer();
    
    protected List<FulltextTokenFilter> filters = new ArrayList();
    
    
    /**
     * 
     */
    public UpdateableFulltextIndex addTokenFilter( FulltextTokenFilter filter ) {
        assert !filters.contains( filter );
        filters.add( filter );
        return this;        
    }

    /**
     * 
     */
    public List<FulltextTokenFilter> filters() {
        return filters;
    }

    
    public FulltextTokenizer tokenizer() {
        return tokenizer;
    }

    /**
     * Update this index. The caller is responsible of properly closing the returned
     * Updater in any case.
     * 
     * @return Newly created updater.
     */
    public abstract Updater prepareUpdate();
    
    
    /**
     * 
     */
    public interface Updater
            extends AutoCloseable {
    
        /**
         * Store/update the given feature. At this point features have to be
         * {@link FeatureTransformer transformed} to be simple and containing String
         * and {@link Geometry} attributes only.
         * 
         * @param feature
         * @param update
         * @throws Exception
         */
        public void store( JSONObject feature, boolean update ) throws Exception;

        public void remove( String fid ) throws Exception;
        
        public abstract void apply();

        /**
         * Close this updater and dispose all associated resources. Without
         * {@link #apply()} this rolls back any modifications.
         */
        @Override
        public abstract void close();
        
//        protected Feature transform( Feature feature ) {
//            for (FeatureTransformer transformer : transformers) {
//                feature = transformer.apply( feature );
//            }
//            return feature;
//        }
        
    }
    
}
