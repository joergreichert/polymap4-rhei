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
package org.polymap.rhei.fulltext.indexing;

import org.json.JSONObject;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;
import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.data.util.Geometries;
import static org.polymap.rhei.fulltext.FulltextIndex.*;

/**
 * Normalizes features:
 * <ul>
 * <li>add property {@value FulltextIndex#FIELD_CATEGORIES} filled with layer keywords</li>
 * <li>normalize {@link FulltextIndex#FIELD_TITLE}</li>
 * <li>add {@link FulltextIndex#FIELD_GEOM} and {@link FulltextIndex.FIELD_SRS}</li>
 * </ul>
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Feature2JsonTransformer
        implements FeatureTransformer<Feature,JSONObject> {

    private static Log log = LogFactory.getLog( Feature2JsonTransformer.class );

//    private ILayer                  layer;

    
    public Feature2JsonTransformer( /*ILayer layer*/ ) {
//        this.layer = layer;
    }


    @Override
    public JSONObject apply( Feature input ) {
        JSONObject result = new JSONObject();
        
        // fid
        result.put( FIELD_ID, input.getIdentifier().getID() );
        
        // properties
        for (Property prop : input.getProperties()) {
            String propName = prop.getName().getLocalPart();
            Object propValue = prop.getValue();
            // normalize title key
            if (propName.equalsIgnoreCase( FIELD_TITLE )) {
                propName = FIELD_TITLE;
            }
            // null value
            if (propValue == null) {
                propValue = JSONObject.NULL;
            }
            // Geometry
            else if (propValue instanceof Geometry) {
                propName = FIELD_GEOM;
                CoordinateReferenceSystem crs = input.getDefaultGeometryProperty().getType().getCoordinateReferenceSystem();
                result.put( FIELD_SRS, Geometries.srs( crs ) );
            }
//            // ommit empty title
//            propValue = propName == FIELD_TITLE && 
            
            result.put( propName, propValue );
        }
        
        // categories
        if (input.getProperty( FIELD_CATEGORIES ) != null) {
            log.warn( "Feature already has field: " + FIELD_CATEGORIES );
        }
        StringBuilder categories = new StringBuilder( 128 );
//        if (layer != null) {
//            layer.as( Labeled.class ).ifPresent( labeled -> {
//                categories.append( labeled.label.get() ).append( ' ' );
//                categories.append( Joiner.on( ' ' ).join( labeled.keywords ) ).append( ' ' );                
//            } );
//        }
        result.put( FIELD_CATEGORIES, categories.toString() );
        return result;
    }
    
}
