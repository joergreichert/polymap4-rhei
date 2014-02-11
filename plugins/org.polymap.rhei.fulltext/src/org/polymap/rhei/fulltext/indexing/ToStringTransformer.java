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

import java.util.Date;
import java.util.Locale;
import java.util.Set;

import java.text.NumberFormat;

import org.json.JSONObject;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ToStringTransformer
        implements FeatureTransformer<JSONObject,JSONObject> {

    private static Log log = LogFactory.getLog( ToStringTransformer.class );

    private NumberFormat        inf = NumberFormat.getInstance( Locale.GERMANY );
    
    private NumberFormat        fnf = NumberFormat.getInstance( Locale.GERMANY );
    
    private FastDateFormat      df = FastDateFormat.getInstance( "dd.MM.yyyy", Locale.GERMANY );
    
    
    @Override
    public JSONObject apply( JSONObject input ) {
        JSONObject result = new JSONObject();
        for (String key : (Set<String>)input.keySet()) {
            Object value = input.get( key );
            if (value instanceof Date) {
                result.put( key, df.format( value ) );
            }
            else if (value instanceof Integer || value instanceof Long) {
                result.put( key, inf.format( value ) );
            }
            else if (value instanceof Float || value instanceof Double) {
                result.put( key, fnf.format( value ) );
            }
            else if (value instanceof Boolean) {
                result.put( key, (boolean)value ? "ja" : "nein" );
            }
            else if (value instanceof String || value instanceof Geometry) {
                result.put( key, value );
            }
            else if (value == JSONObject.NULL) {
                // ommit?
            }
            else {
                log.warn( "Unknown value type: " + value );
            }
        }
        return result;
    }
}
