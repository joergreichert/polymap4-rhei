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
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * API of a full-text index capable of indexing/searching/storing
 * {@link JSONObject features}.
 * 
 * @see SessionHolder
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface FullTextIndex {

    public static final char[]      SEPARATOR_CHARS = { ' ', ',', ';' };

    public static final String      FIELD_ID = "_id_";
    public static final String      FIELD_TITLE = "name";
    public static final String      FIELD_CATEGORIES = "categories";
    public static final String      FIELD_GEOM = "_geom_";
    public static final String      FIELD_SRS = "_srs_";


    public abstract void close();
    
    public abstract boolean isClosed();
    
    public abstract boolean isEmpty();
    
    public void addQueryDecorator( QueryDecorator decorator );
    
    public abstract Iterable<String> autocomplete( String term, int maxResults, 
            CoordinateReferenceSystem worldCRS ) throws Exception;

    
    /**
     * 
     * 
     * @param query
     * @param maxResults
     * @param worldCRS
     * @return JSONObjects containing the following special fields: {@link #FIELD_ID}
     *         , {@link #FIELD_TITLE}, {@value #FIELD_CATEGORIES},
     *         {@value #FIELD_GEOM} and {@link #FIELD_SRS}. These fields and the
     *         payload fields, except {@value #FIELD_GEOM}, have String values.
     * @throws Exception
     */
    public abstract Iterable<JSONObject> search( String query, int maxResults, 
            CoordinateReferenceSystem worldCRS ) throws Exception;
    
}
