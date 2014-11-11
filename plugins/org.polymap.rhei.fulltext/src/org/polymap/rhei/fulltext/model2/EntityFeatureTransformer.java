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
package org.polymap.rhei.fulltext.model2;

import java.util.Date;
import java.text.NumberFormat;

import org.json.JSONObject;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.Queryable;
import org.polymap.core.model2.runtime.CompositeStateVisitor;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.fulltext.FullTextIndex;
import org.polymap.rhei.fulltext.indexing.FeatureTransformer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntityFeatureTransformer
        extends CompositeStateVisitor
        implements FeatureTransformer<Entity,JSONObject> {

    private static Log log = LogFactory.getLog( EntityFeatureTransformer.class );

    private NumberFormat        nf = NumberFormat.getInstance( Polymap.getSessionLocale() );
    
    private FastDateFormat      df = FastDateFormat.getDateInstance( FastDateFormat.FULL, Polymap.getSessionLocale() );

    private int                 propCount;

    private volatile JSONObject result;
    
    private boolean             honorQueryableAnnotation = false;
    
    
    /**
     * True specifies that only Properties annotated as {@link Queryable} are
     * indexed. (default: false)
     * 
     * @return this
     */
    public EntityFeatureTransformer setHonorQueryableAnnotation( boolean honorQueryableAnnotation ) {
        this.honorQueryableAnnotation = honorQueryableAnnotation;
        return this;
    }


    @Override
    public JSONObject apply( Entity entity ) {
        assert result == null : "Implementation is not multi-threaded currently.";
        result = new JSONObject();
        propCount = 0;
        
        try {
            result.put( FullTextIndex.FIELD_ID, entity.id().toString() );
            //result.put( "_type_", entity.getClass().getName() );

            // visit all simple properties
            process( entity );

            log.info( "   " + result.toString( 2 ) );
            return result;
        }
        finally {
            assert result != null : "Implementation is not multi-threaded currently.";
            result = null;
        }
    }

    
    @Override
    protected void visitProperty( Property prop ) {        
        PropertyInfo info = prop.getInfo();
        if (honorQueryableAnnotation && !info.isQueryable()) {
            return;
        }
        
        // the hierarchy of propeties may contain properties with same simple name
        String key = info.getName() + "-" + propCount++;
        Object value = prop.get();

        // null
        if (value == null) {
        }
        // Enum
        else if (value.getClass().isEnum()) {
            result.put( key, value.toString() );
        }
        // Date
        else if (Date.class.isAssignableFrom( value.getClass() )) {
            result.put( key, df.format( value ) );                    
        }
        // Number
        else if (Number.class.isAssignableFrom( value.getClass() )) {
            result.put( key, nf.format( value ) );                    
        }
        // Boolean -> if true add prop name instead of 'true|false'
        else if (value.getClass().equals( Boolean.class )) {
            if (((Boolean)value).booleanValue()) {
                result.put( key, key );
            }
        }
        // String and other types
        else {
            result.put( key, value );
        }
    }
    
}
