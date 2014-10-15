/* 
 * polymap.org
 * Copyright (C) 2014, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.fulltext.store.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.polymap.core.runtime.recordstore.lucene.StringValueCoder;


/**
 * Interprets each and every field as a String {@link Field}. This *must* be
 * last consulted by {@link ValueCoders}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class AnalyzedStringValueCoder
        extends StringValueCoder {

    public boolean encode( Document doc, String key, Object value, boolean indexed ) {
        if (key.equals( LuceneFullTextIndex.FIELD_ANALYZED )) {
            Field field = (Field)doc.getFieldable( key );
            if (field != null) {
                field.setValue( (String)value );
            }
            else {
                doc.add( new Field( key, (String)value, Store.NO, Index.ANALYZED ) );
            }
            return true;
        }
        else {
            return false;
        }
    }
    

    public Object decode( Document doc, String key ) {
        return doc.get( key );
    }

}
