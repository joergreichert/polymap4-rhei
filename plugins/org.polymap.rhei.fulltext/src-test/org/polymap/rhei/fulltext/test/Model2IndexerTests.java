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

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.polymap.core.model2.CollectionProperty;
import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.runtime.ValueInitializer;
import org.polymap.core.model2.store.recordstore.RecordStoreAdapter;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

import org.polymap.rhei.fulltext.address.AddressTokenFilter;
import org.polymap.rhei.fulltext.indexing.LowerCaseTokenFilter;
import org.polymap.rhei.fulltext.lucene.LuceneFullTextIndex;
import org.polymap.rhei.fulltext.model2.FulltextIndexer;
import org.polymap.rhei.fulltext.update.UpdateableFullTextIndex;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Model2IndexerTests {

    private static Log log = LogFactory.getLog( Model2IndexerTests.class );

    private static UpdateableFullTextIndex  index;

    private static LuceneRecordStore        store;

    private static EntityRepository         repo;

    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // index
        index = new LuceneFullTextIndex( null );
        index.addTokenFilter( new AddressTokenFilter() );
        index.addTokenFilter( new LowerCaseTokenFilter() );
        
        store = new LuceneRecordStore();
        repo = EntityRepository.newConfiguration()
                .setStore(
                        new FulltextIndexer( index,
                        new RecordStoreAdapter( store ) ) )
                .setEntities( new Class[] {IndexedEntity.class} )
                .create();

    }


    @Test
    public void simpleTest() throws Exception {
        UnitOfWork uow = repo.newUnitOfWork();
        uow.createEntity( IndexedEntity.class, null, new ValueInitializer<IndexedEntity>() {
            @Override
            public IndexedEntity initialize( IndexedEntity proto ) throws Exception {
                proto.name.set( "name1" );
                proto.one.createValue( new ValueInitializer<IndexedComposite>() {
                    @Override
                    public IndexedComposite initialize( IndexedComposite proto2 ) throws Exception {
                        proto2.name.set( "oneName1" );
                        return proto2;
                    }
                });
                proto.more.createElement( new ValueInitializer<IndexedComposite>() {
                    @Override
                    public IndexedComposite initialize( IndexedComposite proto2 ) throws Exception {
                        proto2.name.set( "moreName1" );
                        return proto2;
                    }
                });
                proto.more.createElement( new ValueInitializer<IndexedComposite>() {
                    @Override
                    public IndexedComposite initialize( IndexedComposite proto2 ) throws Exception {
                        proto2.name.set( "Ulrike Philipp" );
                        return proto2;
                    }
                });
                return proto;
            }
        });
        uow.commit();
        
        //
        Iterable<JSONObject> rs = index.search( "name1", 100 );
        Assert.assertEquals( 1, Iterables.size( rs ) );

        rs = index.search( "oneName1", 100 );
        Assert.assertEquals( 1, Iterables.size( rs ) );

        rs = index.search( "Ulrike", 100 );
        Assert.assertEquals( 1, Iterables.size( rs ) );

        rs = index.search( "Ulrike AND name1", 100 );
        Assert.assertEquals( 1, Iterables.size( rs ) );

        rs = index.search( "Ulrike AND nicht_da", 100 );
        Assert.assertEquals( 0, Iterables.size( rs ) );
    }
    

    /**
     * 
     */
    public static class IndexedEntity
            extends Entity {
        
        protected Property<String>                      name;
        
        protected Property<IndexedComposite>            one;
        
        protected CollectionProperty<IndexedComposite>  more;
        
    }
    
    
    /**
     * 
     */
    public static class IndexedComposite
            extends Composite {

        protected Property<String>                      name;
        
    }

}
