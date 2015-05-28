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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.data.util.Geometries;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CrsTransformDecorator
        extends QueryDecorator {

    private static Log log = LogFactory.getLog( CrsTransformDecorator.class );
    
    private CoordinateReferenceSystem       targetCrs;


    public CrsTransformDecorator( FulltextIndex next, CoordinateReferenceSystem targetCrs ) {
        super( next );
        assert targetCrs != null;
        this.targetCrs = targetCrs;
    }

    
    @Override
    public Iterable<JSONObject> search( String query, int maxResults ) throws Exception {
        Iterable<JSONObject> results = next.search( query, maxResults );
        
        return Iterables.transform( results, new Function<JSONObject,JSONObject>() {
            public JSONObject apply( JSONObject input ) {
                try {
                    Geometry geom = (Geometry)input.opt( FIELD_GEOM );
                    String srs = input.optString( FIELD_SRS );
                    if (geom != null && srs != null) {
                        Geometry transformed = Geometries.transform( geom, Geometries.crs( srs ), targetCrs );
                        input.put( FIELD_GEOM, transformed );
                    }
                    return input;
                }
                catch (RuntimeException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new RuntimeException( e );
                }
            }
        });
    }

}
