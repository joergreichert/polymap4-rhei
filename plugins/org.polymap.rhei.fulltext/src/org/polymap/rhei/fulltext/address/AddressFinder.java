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
package org.polymap.rhei.fulltext.address;

import java.util.Map;
import java.util.Set;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.polymap.rhei.fulltext.FulltextIndex;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AddressFinder {

    private static Log log = LogFactory.getLog( AddressFinder.class );
    
    private FulltextIndex       index;
    
    private int                 maxResults = -1;
    
    
    public AddressFinder( FulltextIndex index ) {
        this.index = index;
    }

    
    public AddressFinder maxResults( @SuppressWarnings("hiding") int maxResults ) {
        this.maxResults = maxResults;
        return this;
    }

    
    public ReferencedEnvelope findBBox( JSONObject search ) {
        Iterable<JSONObject> results = find( search );
        ReferencedEnvelope bbox = new ReferencedEnvelope();
        for (JSONObject feature : results) {
            Geometry geom = (Geometry)feature.get( FulltextIndex.FIELD_GEOM );
            bbox.expandToInclude( geom.getEnvelopeInternal() );
        }
        return bbox;
    }

    
    /**
     *
     * @param fields Search object containing field names as defined in {@link Address}.
     * @return Result set of {@link JSONObject} instances representing the found addresses.
     */
    public Iterable<JSONObject> find( JSONObject search ) {
        StringBuilder query = new StringBuilder( 256 );
        for (String propName : (Set<String>)search.keySet()) {
            Object value = search.opt( propName );
            if (value != null && ((String)value).length() > 0) {
                query.append( query.length() > 0 ? " AND " : "" );
                query.append( propName ).append( ":\"" ).append( value.toString() ).append( "\"" );
            }
        }
        try {
            log.info( "LUCENE: " + query.toString() );
            return index.search( query.toString(), maxResults );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }

    }
    
    
    /**
     *
     * @param fields Maps field names as defined in {@link Address} to search values.
     * @return Result set of {@link JSONObject} instances representing the found addresses.
     */
    public Iterable<JSONObject> find( Map<String,String> fields ) {
        return find( new JSONObject( fields ) );
    }

}
