/* 
 * polymap.org
 * Copyright 2010, Falko Br�utigam, and other contributors as indicated
 * by the @authors tag.
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
 *
 * $Id: $
 */
package org.polymap.rhei.form.workbench.form;

import java.util.List;

import org.opengis.feature.Feature;

import org.polymap.rhei.form.IFormEditorPage;

/**
 * 
 * @see FormPageProviderExtension
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 * @version ($Revision$)
 */
public interface IFormPageProvider {

    /**
     * Adds editor pages to the given form editor.
     * 
     * <p>Example:
     * <pre>
     *     List<IFormEditorPage> result = new ArrayList();
     *     if (feature.getType().getName().getLocalPart().equalsIgnoreCase( "antrag" )) {
     *         result.add( new AntragFormEditorPage( feature, formEditor.getFeatureStore() ) );
     *     }
     *     return result;
     * </pre>
     * 
     * @param formEditor
     * @param feature The feature to be displayed in the pages.
     * @return List of newly created pages.
     */
    public List<IFormEditorPage> addPages( FormEditor formEditor, Feature feature );
    
}
