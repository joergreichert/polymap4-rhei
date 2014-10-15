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

import static org.polymap.core.model2.query.Expressions.or;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.query.Expressions;
import org.polymap.core.model2.query.ResultSet;
import org.polymap.core.model2.query.grammar.BooleanExpression;
import org.polymap.core.model2.runtime.UnitOfWork;

import org.polymap.rhei.fulltext.FullTextIndex;
import org.polymap.rhei.fulltext.model2.FulltextIndexer;

/**
 * A search field for Model2 {@link Entity}s which were indexed using the
 * {@link FulltextIndexer} and EntityFeatureTransformer.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class EntitySearchField<T extends Entity>
        extends AbstractSearchField {

    private static Log log = LogFactory.getLog( EntitySearchField.class );
    
    protected FullTextIndex             index;
    
    protected UnitOfWork                uow;
    
    protected Class<T>                  entityClass;
    
    protected ResultSet<T>              results;
    
    
    public EntitySearchField( Composite parent, FullTextIndex index, UnitOfWork uow, Class<T> entityClass ) {
        super( parent );
        this.index = index;
        this.uow = uow;
        this.entityClass = entityClass;
    }

    
    @Override
    protected void doSearch( String query ) throws Exception {
        if (query.length() == 0) {
            results = ResultSet.EMPTY;            
        }
        else {
            Iterable<JSONObject> rs = index.search( query, -1 );

            List<BooleanExpression> ids = new ArrayList( 256 );
            for (JSONObject record : rs) {
                ids.add( Expressions.id( record.getString( FullTextIndex.FIELD_ID ) ) );
            }

            // none
            if (ids.isEmpty()) {
                results = ResultSet.EMPTY;
            }
            // one
            else if (ids.size() == 1) {
                results = uow.query( entityClass ).where( ids.get( 0 ) ).execute();
            }
            // two
            else if (ids.size() == 2) {
                results = uow.query( entityClass ).where( or( ids.get( 0 ), ids.get( 1 ) ) ).execute();
            }
            // more
            else {
                BooleanExpression[] more = ids.subList( 2, ids.size() ).toArray( new BooleanExpression[ids.size()-2] );
                results = uow.query( entityClass ).where( or( ids.get( 0 ), ids.get( 1 ), more ) ).execute();
            }
        }
    }

}
