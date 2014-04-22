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

import com.google.common.base.Function;

import org.polymap.rhei.fulltext.update.UpdateableFullTextIndex;

/**
 * A FullTextTokenFilter is a {@link Function} that filters and/or transforms
 * index tokens while updating an {@link UpdateableFullTextIndex}.
 *
 * @see UpdateableFullTextIndex
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface FullTextTokenFilter
        extends Function<String,String> {
    
    /**
     * @return The filtered token
     */
    @Override
    public String apply( String term );

}
