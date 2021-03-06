/*
 * polymap.org
 * Copyright 2011, Falko Br�utigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.rhei.table;

import org.opengis.feature.Feature;

import org.eclipse.jface.viewers.LabelProvider;

import org.polymap.rhei.table.DefaultFeatureTableColumn.DefaultCellLabelProvider;

/**
 * This is the content element of a {@link FeatureTableViewer}.
 * {@link IFeatureContentProvider} provides elements of this type.
 * <p/>
 * The content element are not plain {@link Feature} instances in order to let the
 * feature table handle any combinations of attributes, including complex attributes
 * or even multiple features in one table.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface IFeatureTableElement {

    public String fid();
    
    
    /**
     * Used by {@link DefaultCellLabelProvider} for default labels and by
     * {@link DefaultFeatureTableColumn#newComparator(int)} for sorting.
     * 
     * @param name
     * @return Null signals that String from the {@link LabelProvider} is used for
     *         sorting.
     */
    public Object getValue( String name );

    
    /**
     * Implement to support table in-place editing
     *
     * @param name
     * @param value
     */
    public void setValue( String name, Object value );

}
