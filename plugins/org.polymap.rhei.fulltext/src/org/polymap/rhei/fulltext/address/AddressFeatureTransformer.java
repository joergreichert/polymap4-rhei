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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Sets;

import org.polymap.rhei.fulltext.indexing.FeatureTransformer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AddressFeatureTransformer
        implements FeatureTransformer<JSONObject,JSONObject> {

    private static Log log = LogFactory.getLog( AddressFeatureTransformer.class );

    public static final String FIELD_STREET = "strasse";
    public static final String FIELD_NUMBER = "nummer";
    public static final String FIELD_NUMBER_X = "nummerx";
    public static final String FIELD_CITY = "ort";
    public static final String FIELD_CITY_X = "ortx";
    public static final String FIELD_POSTALCODE = "plz";
    public static final String FIELD_DISTRICT = "district";

    protected static Set<String> newSet( String... elms ) {
        return Sets.newHashSet( elms );
    }

    public static final Map<String,Set<String>> mapping = new HashMap();
    
    static {
        mapping.put( FIELD_STREET, newSet( "street", "strasse", "straße" ) );
        mapping.put( FIELD_NUMBER, newSet( "number", "nummer", "hnr", "nr" ) );
        mapping.put( FIELD_NUMBER_X, newSet( "number_ext", "nummer_ext", "hnr_zusatz", "nr_zusatz" ) );
        mapping.put( FIELD_CITY, newSet( "city", "stadt", "ort" ) );
        mapping.put( FIELD_CITY_X, newSet( "city_ext", "stadt_zusatz", "ort_zusatz" ) );
        mapping.put( FIELD_DISTRICT, newSet( "district", "urban_district", "ortsteil" ) );
        mapping.put( FIELD_POSTALCODE, newSet( "postalcode", "code", "zip", "zipcode", "plz" ) );
    }
    
    // instance *******************************************
    
    @Override
    public JSONObject apply( JSONObject input ) {
        JSONObject result = new JSONObject();
        for (String key : (Set<String>)input.keySet()) {
            boolean found = false;
            for (Entry<String,Set<String>> synonyms : mapping.entrySet()) {
                if (synonyms.getValue().contains( key )) {
                    result.put( synonyms.getKey(), input.get( key ) );
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.put( key, input.get( key ) );
            }
        }
        return result;
    }
}
