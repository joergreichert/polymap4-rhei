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
package org.polymap.rhei.fulltext.test;

import java.util.Date;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeatureType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.MultiLineString;

import org.polymap.rhei.fulltext.address.AddressFeatureTransformer;
import org.polymap.rhei.fulltext.address.AddressTokenFilter;
import org.polymap.rhei.fulltext.indexing.Feature2JsonTransformer;
import org.polymap.rhei.fulltext.indexing.LowerCaseTokenFilter;
import org.polymap.rhei.fulltext.indexing.ToStringTransformer;
import org.polymap.rhei.fulltext.lucene.LuceneFullTextIndex;
import org.polymap.rhei.fulltext.update.UpdateableFullTextIndex;
import org.polymap.rhei.fulltext.update.UpdateableFullTextIndex.Updater;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureTests {

    private static Log log = LogFactory.getLog( FeatureTests.class );

    private static UpdateableFullTextIndex  index;

    private static SimpleFeatureType        schema;

    
    static {
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime.recordstore", "trace" );
    }

    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // index
        index = new LuceneFullTextIndex( null );
        index.addTokenFilter( new AddressTokenFilter() );
        index.addTokenFilter( new LowerCaseTokenFilter() );                    

        // schema
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName( "FeatureType1" );
        builder.setCRS( CRS.decode( "EPSG:4326" ) );
        builder.add( "name", String.class );
        builder.add( "geom", MultiLineString.class, "EPSG:4326" );
        builder.add( "description", String.class );
        builder.add( "date", Date.class );
        builder.add( "isCool", Boolean.class );
        builder.add( "age", Integer.class );
        schema = builder.buildFeatureType();        

        // fill index
        Updater updater = index.prepareUpdate();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder( schema );
        fb.set( "name", "Feature1" );
        fb.set( "geom", null );
        fb.set( "description", "Das ist ein wunderbares feature." );
        fb.set( "date", new Date() );
        fb.set( "isCool", true );
        fb.set( "age", 100 );
        JSONObject feature = new Feature2JsonTransformer( null ).apply( fb.buildFeature( "1" ) );
        feature = new ToStringTransformer().apply( feature );
        feature = new AddressFeatureTransformer().apply( feature );
        log.info( "Transformed feature: " + feature );
        updater.store( feature, false );
        updater.apply();
    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }


    @Test
    public void simpleQueryTest() throws Exception {
        Iterable<JSONObject> results = index.search( "feature", 100 );
        log.info( "Result: " + Iterables.toString( results ) );
        Assert.assertFalse( Iterables.isEmpty( results ) );

        results = index.search( "feature 100", 100 );
        log.info( "Results: " + Iterables.size( results ) );
        Assert.assertFalse( Iterables.isEmpty( results ) );

        results = index.search( "feature 101", 100 );
        log.info( "Results: " + Iterables.size( results ) );
        Assert.assertTrue( Iterables.isEmpty( results ) );
    }
    

    @Test
    public void simpleAutocompleteTest() throws Exception {
        Iterable<String> results = index.propose( "featur", 100, null );
        log.info( "Autocomplete: " + Iterables.toString( results ) );
        Assert.assertFalse( Iterables.isEmpty( results ) );
    }
    
}
