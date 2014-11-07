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

import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.runtime.CompositeStateVisitor;
import org.polymap.core.model2.runtime.PropertyInfo;

import org.polymap.rhei.fulltext.FullTextIndex;
import org.polymap.rhei.fulltext.indexing.FeatureTransformer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class EntityFeatureTransformer
        implements FeatureTransformer<Entity,JSONObject> {

    private static Log log = LogFactory.getLog( EntityFeatureTransformer.class );

    
    @Override
    public JSONObject apply( Entity entity ) {
        final JSONObject result = new JSONObject();
        
        result.put( FullTextIndex.FIELD_ID, entity.id().toString() );
        result.put( "_type_", entity.getClass().getName() );
        
        // visit all simple properties
        new CompositeStateVisitor() {
            int propCount = 0;
            
            @Override
            protected void visitProperty( Property prop ) {
                PropertyInfo info = prop.getInfo();
                if (info.getType() == String.class || info.getType() == Date.class) {
                    // the hierarchy of propeties may contain properties with same simple name
                    String key = prop.getInfo().getName() + "-" + propCount++;
                    Class type = prop.getInfo().getType();
                    // Boolean -> if true add prop name instead of 'true|false'
                    if (type.equals( Boolean.class )) {
                        if (((Boolean)prop.get()).booleanValue()) {
                            result.put( key, key );
                        }
                    }
                    // other types
                    else {
                        result.put( key, prop.get() );
                    }
                }
            }
        }.process( entity );
        
        return result;
    }
    
}
