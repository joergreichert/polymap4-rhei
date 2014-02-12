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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.StringReader;

import org.geotools.geojson.geom.GeometryJSON;
import org.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Handles a special {@link #boundsPattern} in the search query. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BoundsFilterQueryDecorator
        extends QueryDecorator {

    private static Log log = LogFactory.getLog( BoundsFilterQueryDecorator.class );
    
    /** Pattern: bounds:[^ $]+ */
    public static final Pattern     boundsPattern = Pattern.compile( "bounds:[^ $]+" );

    static final GeometryJSON       jsonDecoder = new GeometryJSON();

    
    public BoundsFilterQueryDecorator( FullTextIndex next ) {
        super( next );
    }

    
    @Override
    public Iterable<JSONObject> search( String query, int maxResults ) throws Exception {
        String boundsJson = null;
        String searchQuery = query;

        // extract bounds:{...} param from query
        Matcher matcher = boundsPattern.matcher( query );
        if (matcher.find()) {
            String boundsParam = query.substring( matcher.start(), matcher.end() );
            boundsJson = StringUtils.substringAfter( boundsParam, "bounds:" );
            searchQuery = StringUtils.remove( searchQuery, boundsParam );
        }
        
        // next
        Iterable<JSONObject> results = next.search( searchQuery, maxResults );
        
        // filter bounds
        if (boundsJson != null) {
            final Geometry bounds = jsonDecoder.read( new StringReader( boundsJson ) );
            
            return Iterables.filter( results, new Predicate<JSONObject>() {
                public boolean apply( JSONObject feature ) {
                    try {
                        Geometry geom = (Geometry)feature.opt( FIELD_GEOM );
                        return geom != null ? bounds.contains( geom ) : true;
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                }
            });
        }
        else {
            return results;
        }
    }
    
}
