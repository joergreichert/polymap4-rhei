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
package org.polymap.rhei.table;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import org.polymap.rhei.field.IFormFieldValidator;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class FormColumnLabelProvider
        extends ColumnLabelProvider {
        
    private FormFeatureTableColumn      tableColumn;

    /** Used to validate and transform values to String. */
    private IFormFieldValidator         validator;

    
    public FormColumnLabelProvider( FormFeatureTableColumn tableColumn, IFormFieldValidator validator ) {
        this.tableColumn = tableColumn;
        this.validator = validator;
        assert validator != null;
    }

    
    protected IFormFieldValidator getValidator() {
        return validator;
    }


    @Override
    public String getText( Object elm ) {
        try {
            IFeatureTableElement featureElm = (IFeatureTableElement)elm;
            //log.info( "getText(): fid=" + featureElm.fid() + ", prop=" + prop.getName().getLocalPart() );

            Object value = featureElm.getValue( tableColumn.getName() );
            String transformed = (String)validator.transform2Field( value );
            return transformed != null ? transformed : "";
        }
        catch (Exception e) {
            FormFeatureTableColumn.log.warn( "", e );
            return "Fehler: " + e.getLocalizedMessage();
        }
    }


    @Override
    public String getToolTipText( Object elm ) {
        return elm != null ? getText( elm ) : null;
    }
    
}