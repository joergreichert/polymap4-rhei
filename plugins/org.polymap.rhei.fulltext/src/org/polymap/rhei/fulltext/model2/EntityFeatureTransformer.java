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

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import java.text.NumberFormat;

import org.json.JSONObject;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

import org.eclipse.rap.rwt.RWT;

import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.PlainLazyInit;

import org.polymap.rhei.fulltext.FulltextIndex;
import org.polymap.rhei.fulltext.indexing.FeatureTransformer;

import org.polymap.model2.Entity;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;
import org.polymap.model2.runtime.CompositeStateVisitor;
import org.polymap.model2.runtime.PropertyInfo;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntityFeatureTransformer
        extends CompositeStateVisitor
        implements FeatureTransformer<Entity,JSONObject> {

    private static Log log = LogFactory.getLog( EntityFeatureTransformer.class );

    /**
     * 
     */
    public interface DuplicateHandler extends Function<String[],String> { }
    
    /**
     * Always throws a {@link RuntimeException}. 
     */
    public static final DuplicateHandler EXCEPTION = new DuplicateHandler() {
        @Override
        public String apply( String[] input ) {
            throw new RuntimeException( "Duplicate values are not allowed: " + Arrays.asList( input ) );
        }
    };
    
    /**
     * 
     */
    public static final DuplicateHandler CONCAT = new DuplicateHandler() {
        @Override
        public String apply( String[] input ) {
            return Joiner.on( ' ' ).join( input );
        }
    };
    
    /**
     * 
     */
    public interface FieldNameProvider extends Function<Property,String> { }
    
    public class StandardFieldNameProvider
            implements FieldNameProvider {
        @Override
        public String apply( Property input ) {
            return input.info().getName();
        }
    }
    
    // instance *******************************************
    
    private Lazy<NumberFormat>  nf = new PlainLazyInit( () -> NumberFormat.getInstance( firstNonNull( RWT.getLocale(), Locale.getDefault() ) ) );
    
    private Lazy<FastDateFormat> df = new PlainLazyInit( () -> FastDateFormat.getDateInstance( FastDateFormat.FULL, RWT.getLocale() ) );

    private volatile JSONObject result;
    
    protected boolean           honorQueryableAnnotation = false;
    
    /**
     * By default this is {@link StandardFieldNameProvider}. Change this to affect
     * subsequent call of {@link #putValue(Property, String)}.
     */
    public FieldNameProvider    fieldNameProvider = new StandardFieldNameProvider();
    
    /**
     * By default this is set to {@link #CONCAT}. Change this to affect subsequent
     * calls of {@link #putValue(Property, String)}.
     */
    public DuplicateHandler     duplicateHandler = CONCAT;
    
    
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
        
        try {
            result.put( FulltextIndex.FIELD_ID, entity.id().toString() );
            //result.put( "_type_", entity.getClass().getName() );

            // visit all simple properties
            process( entity );

            log.debug( "   " + result.toString( 2 ) );
            return result;
        }
        finally {
            assert result != null : "Implementation is not multi-threaded currently.";
            result = null;
        }
    }

    
    @Override
    protected void visitProperty( Property prop ) {        
        PropertyInfo info = prop.info();
        if (honorQueryableAnnotation && !info.isQueryable()) {
            log.debug( "   skipping non @Queryable property: " + info.getName() );
            return;
        }
        
        // the hierarchy of propeties may contain properties with same simple name
        Object value = prop.get();

        // null
        if (value == null) {
        }
        // Enum
        else if (value.getClass().isEnum()) {
            putValue( prop, value.toString() );
        }
        // Date
        else if (Date.class.isAssignableFrom( value.getClass() )) {
            putValue( prop, df.get().format( value ) );
        }
        // Number
        else if (Number.class.isAssignableFrom( value.getClass() )) {
            putValue( prop, nf.get().format( value ) );                    
        }
        // Boolean -> if true add prop name instead of 'true|false'
        else if (value.getClass().equals( Boolean.class )) {
            if (((Boolean)value).booleanValue()) {
                putValue( prop, prop.info().getName() );
            }
        }
        // String and other types
        else {
            putValue( prop, value.toString() );
        }
    }
 
    
    protected void putValue( Property prop, String value ) {
        String key = fieldNameProvider.apply( prop ); //prop.getInfo().getName() + "-" + propCount++;
        putValue( key, value );
    }


    protected void putValue( String key, String value ) {
        String currentValue = result.optString( key );
        if (currentValue.length() > 0) {
            value = duplicateHandler.apply( new String[] {currentValue,value} );
        }
        result.put( key, value );
    }
    
}
