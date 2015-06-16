/* 
 * polymap.org
 * Copyright 2010, 2012 Falko Bräutigam, and other contributors as
 * indicated by the @authors tag.
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
package org.polymap.rhei.internal.form;

import org.opengis.feature.Property;

import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldDecorator;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.form.IFormPageSite;
import org.polymap.rhei.form.IFormToolkit;

/**
 * This is the parent Composite of a form field.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FormFieldComposite
        extends BaseFieldComposite {

    private Property                prop;
    

    public FormFieldComposite( Object editor, IFormPageSite pageSite, 
            IFormToolkit toolkit, Property prop, IFormField field,
            IFormFieldLabel labeler, IFormFieldDecorator decorator, IFormFieldValidator validator ) {
        super( editor, pageSite, toolkit, field, labeler, decorator, validator );
        this.prop = prop;
    }
    
    
    public Property getProperty() {
        return prop;
    }

    public Object store() throws Exception {
        super.store();
        return prop.getValue();
    }


    // IFormFieldSite *************************************

    @Override
    public String getFieldName() {
        return prop.getName().getLocalPart();
    }

    @Override
    public Object getFieldValue() throws Exception {
        return validator.transform2Field( prop.getValue() );
    }

    @Override
    public void setFieldValue( Object value ) throws Exception {
        prop.setValue( validator.transform2Model( value ) );
    }

}
