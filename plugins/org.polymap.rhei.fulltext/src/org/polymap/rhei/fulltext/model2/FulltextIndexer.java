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

import static org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus.CREATED;
import static org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus.MODIFIED;
import static org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus.REMOVED;

import java.util.List;

import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.store.CloneCompositeStateSupport;
import org.polymap.core.model2.store.CompositeState;
import org.polymap.core.model2.store.StoreDecorator;
import org.polymap.core.model2.store.StoreSPI;
import org.polymap.core.model2.store.StoreUnitOfWork;

import org.polymap.rhei.fulltext.indexing.FeatureTransformer;
import org.polymap.rhei.fulltext.indexing.ToStringTransformer;
import org.polymap.rhei.fulltext.update.UpdateableFullTextIndex;
import org.polymap.rhei.fulltext.update.UpdateableFullTextIndex.Updater;

/**
 * Provides a decorator for an underlying store. This decorator tracks modifications and
 * feed them into an {@link UpdateableFullTextIndex}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FulltextIndexer
        extends StoreDecorator
        implements StoreSPI {

    private static Log log = LogFactory.getLog( FulltextIndexer.class );
    
    private UpdateableFullTextIndex             index;
    
    private List<? extends FeatureTransformer>  transformers = Lists.newArrayList( new EntityFeatureTransformer(), new ToStringTransformer() ); 
    
    
    public FulltextIndexer( UpdateableFullTextIndex index, StoreSPI store ) {
        super( store );
        this.index = index;
    }


    @Override
    public StoreUnitOfWork createUnitOfWork() {
        StoreUnitOfWork suow = store.createUnitOfWork();
        return suow instanceof CloneCompositeStateSupport
                ? new IndexerUnitOfWork2( suow )
                : new IndexerUnitOfWork( suow );
    }


    protected JSONObject transform( Entity feature ) {
        Object transformed = feature;
        for (FeatureTransformer transformer : transformers) {
            transformed = transformer.apply( transformed );
        }
        log.debug( "Transformed: " + transformed.toString() );
        return (JSONObject)transformed;
    }

    
    /**
     * 
     */
    protected class IndexerUnitOfWork
            extends UnitOfWorkDecorator
            implements StoreUnitOfWork {
        
        private Updater             updater;


        public IndexerUnitOfWork( StoreUnitOfWork suow ) {
            super( suow );
        }


        @Override
        public void prepareCommit( Iterable<Entity> loaded ) throws Exception {
            // update fulltext index
            updater = index.prepareUpdate();
            for (Entity entity : loaded) {
                if (entity.status() == CREATED) {
                    updater.store( transform( entity ), false );
                }
                else if (entity.status() == MODIFIED) {
                    updater.store( transform( entity ), true );
                }
                else if (entity.status() == REMOVED) {
                    updater.remove( entity.id().toString() );
                }
            }
            // call delegate
            suow.prepareCommit( loaded );
        }

        
        @Override
        public void commit() {
            assert updater != null;
            updater.apply();
            updater = null;

            suow.commit();
        }


        @Override
        public void rollback() {
            if (updater != null) {
                updater.close();
                updater = null;
            }
            suow.rollback();
        }

    }

    
    /**
     * 
     */
    protected class IndexerUnitOfWork2
            extends IndexerUnitOfWork
            implements CloneCompositeStateSupport {

        public IndexerUnitOfWork2( StoreUnitOfWork suow ) {
            super( suow );
        }

        protected CloneCompositeStateSupport suow() {
            return (CloneCompositeStateSupport)suow;
        }
        
        @Override
        public CompositeState cloneEntityState( CompositeState state ) {
            return suow().cloneEntityState( state ); 
        }

        @Override
        public void reincorparateEntityState( CompositeState state, CompositeState clonedState ) {
            suow().reincorparateEntityState( state, clonedState );
        }
        
    }

}
