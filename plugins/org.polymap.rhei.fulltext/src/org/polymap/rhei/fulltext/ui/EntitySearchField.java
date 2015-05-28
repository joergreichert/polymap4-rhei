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
package org.polymap.rhei.fulltext.ui;

import static org.polymap.model2.query.Expressions.or;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.fulltext.FulltextIndex;
import org.polymap.rhei.fulltext.model2.FulltextIndexer;

import org.polymap.model2.Entity;
import org.polymap.model2.query.Expressions;
import org.polymap.model2.query.Query;
import org.polymap.model2.query.grammar.BooleanExpression;
import org.polymap.model2.runtime.UnitOfWork;

/**
 * A search field for Model2 {@link Entity}s which were indexed using the
 * {@link FulltextIndexer} and EntityFeatureTransformer.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class EntitySearchField<T extends Entity>
        extends AbstractSearchField {

    private static Log log = LogFactory.getLog( EntitySearchField.class );
    
    protected FulltextIndex             index;
    
    protected UnitOfWork                uow;
    
    protected Class<T>                  entityClass;
    
    protected Query<T>                  query;
    
    
    public EntitySearchField( Composite parent, FulltextIndex index, UnitOfWork uow, Class<T> entityClass ) {
        super( parent );
        this.index = index;
        this.uow = uow;
        this.entityClass = entityClass;
    }

    
    @Override
    protected void doSearch( String queryString ) throws Exception {
        if (queryString.length() == 0) {
            query = uow.query( entityClass ).where( Expressions.FALSE );         
        }
        else {
            Iterable<JSONObject> rs = index.search( queryString, -1 );

            List<BooleanExpression> ids = new ArrayList( 256 );
            for (JSONObject record : rs) {
                if (record.optString( FulltextIndex.FIELD_ID ).length() > 0) {
                    ids.add( Expressions.id( record.getString( FulltextIndex.FIELD_ID ) ) );
                }
                else {
                    log.warn( "No FIELD_ID in record: " + record );
                }
            }

            // none
            if (ids.isEmpty()) {
                query = uow.query( entityClass ).where( Expressions.FALSE );         
            }
            // one
            else if (ids.size() == 1) {
                query = uow.query( entityClass ).where( ids.get( 0 ) );
            }
            // two
            else if (ids.size() == 2) {
                query = uow.query( entityClass ).where( or( ids.get( 0 ), ids.get( 1 ) ) );
            }
            // more
            else {
                BooleanExpression[] more = ids.subList( 2, ids.size() ).toArray( new BooleanExpression[ids.size()-2] );
                query = uow.query( entityClass ).where( or( ids.get( 0 ), ids.get( 1 ), more ) );
            }
        }
    }

}
