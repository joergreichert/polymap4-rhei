/* 
 * polymap.org
 * Copyright (C) 2015, Falko Br�utigam. All rights reserved.
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
package org.polymap.rhei.form;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;

import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public abstract class FieldBuilder
        extends Configurable {

    private static Log log = LogFactory.getLog( FieldBuilder.class );

    /** The parent to create the field for. If not set the page body is used. */
    public Config2<FieldBuilder,Composite>          parent;
    
    public Config2<FieldBuilder,String>             label;
    
    public Config2<FieldBuilder,String>             tooltip;
    
    public Config2<FieldBuilder,IFormField>         field;
    
    public Config2<FieldBuilder,IFormFieldValidator> validator;
    
    @DefaultBoolean( true )
    public Config2<FieldBuilder,Boolean>            fieldEnabled;

    public Config2<FieldBuilder,Object>             layoutData;

    
    protected abstract Class<?> propBinding();
    
    protected abstract Composite createFormField();
    
    
    public Composite create() {
        if (!field.isPresent()) {
            Class binding = propBinding();
            // Number
            if (Number.class.isAssignableFrom( binding )) {
                field.set( new StringFormField() );
                validator.set( new NumberValidator( binding, Polymap.getSessionLocale() ) );
            }
            // Date
            else if (Date.class.isAssignableFrom( binding )) {
                field.set( new DateTimeFormField() );
            }
            // Boolean
            else if (Boolean.class.isAssignableFrom( binding )) {
                field.set( new CheckboxFormField() );
            }
            // default: String
            else {
                field.set( new StringFormField() );
            }
        }
        Composite result = createFormField();
        
        // layoutData
        if (layoutData.isPresent()) {
            result.setLayoutData( layoutData.get() );
        }
        // tooltip
        tooltip.ifPresent( value -> result.setToolTipText( value ) );
        
        // editable
        if (!fieldEnabled.get()) {
            field.get().setEnabled( false );
            //pageContainer.setFieldEnabled( prop.getName().getLocalPart(), fieldEnabled );
        }
        return result;
    }

}
