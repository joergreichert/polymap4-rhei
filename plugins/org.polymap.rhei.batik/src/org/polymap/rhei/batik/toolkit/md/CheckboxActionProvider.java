/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.batik.toolkit.md;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.ViewerCell;

import org.polymap.rhei.batik.BatikPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class CheckboxActionProvider
        extends ActionProvider {

    private static Log log = LogFactory.getLog( CheckboxActionProvider.class );

    private Image           selectedImage = BatikPlugin.instance().imageForName( "resources/icons/md/checkbox-marked.png" );

    private Image           unselectedImage = BatikPlugin.instance().imageForName( "resources/icons/md/checkbox-blank-outline.png" );

    protected Map<Object,Boolean>   selected = new HashMap( 32 );
    
    
    public CheckboxActionProvider() {
    }

    
    public CheckboxActionProvider( Image selectedImage, Image unselectedImage ) {
        this.selectedImage = selectedImage;
        this.unselectedImage = unselectedImage;
    }

    
    public boolean isSelected( Object elm ) {
        return selected.get( elm );
    }
    
    
    public void setSelected( Object elm, boolean selected ) {
        this.selected.put( elm, selected );
    }

    
    protected abstract boolean initSelection( MdListViewer viewer, Object elm );

    protected abstract void onSelectionChange( MdListViewer viewer, Object elm );
    
    
    /**
     * This default implementation updates the {@link #isSelected()} state and
     * the image according to this new state. Override
     */
    @Override
    public void perform( MdListViewer viewer, Object elm ) {
        selected.put( elm, !selected.get( elm ) );
        viewer.update( elm, null );
        onSelectionChange( viewer, elm );
    }


    @Override
    public void update( ViewerCell cell ) {
        if (!selected.containsKey( cell.getElement() )) {
            selected.put( cell.getElement(), initSelection( null, cell.getElement() ) );
        }
        cell.setImage( selected.get( cell.getElement() ) ? selectedImage : unselectedImage );
    }
    
}
