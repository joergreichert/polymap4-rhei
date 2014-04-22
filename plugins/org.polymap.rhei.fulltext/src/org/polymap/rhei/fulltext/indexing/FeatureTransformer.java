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
package org.polymap.rhei.fulltext.indexing;

import org.json.JSONObject;

import com.google.common.base.Function;

import org.polymap.rhei.fulltext.update.UpdateableFullTextIndex;

/**
 * FeatureTransformers are {@link Function}s that transform a given input object
 * while updating an {@link UpdateableFullTextIndex}. Transformers are chainable.
 * Every Transformer handles a special aspect of a particular input object type. Last
 * transformation step must produce an {@link JSONObject} which is then
 * stored/indexed.
 *
 * @see UpdateableFullTextIndex
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface FeatureTransformer<F,T>
        extends Function<F,T> {

    public static final Object NULL_VALUE = new Object();
}
