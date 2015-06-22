/* 
 * polymap.org
 * Copyright 2010-2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.engine.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.engine.form.BaseFieldComposite;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldDecorator;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldSite;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.filter.IFilterPageSite;
import org.polymap.rhei.form.IFormToolkit;

/**
 * The filter form specific parent Composite of a form field, consisting of an
 * {@link IFormField}, an {@link IFormFieldLabel} and an
 * {@link IFormFieldDecorator}. The FilterFieldComposite provides them a context
 * via the {@link IFormFieldSite}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FilterFieldComposite
        extends BaseFieldComposite {

    private static Log log = LogFactory.getLog( FilterFieldComposite.class );
    
    private String      propName;
    
    private Class<?>    propType;
    
    private Object      value;


    public FilterFieldComposite( Object editor, IFilterPageSite pageSite, 
            IFormToolkit toolkit, String propName, Class<?> propType,
            IFormField field, IFormFieldLabel labeler, IFormFieldDecorator decorator,
            IFormFieldValidator validator ) {
        super( editor, pageSite, toolkit, field, labeler, decorator, validator );
        this.propName = propName;
        this.propType = propType;
    }
    
    public Object store() throws Exception {
        super.store();
        return value;
    }

    // IFormFieldSite *************************************

    @Override
    public String getFieldName() {
        return propName;
    }

    @Override
    public Object getFieldValue() throws Exception {
        return validator.transform2Field( value );
    }

    @Override
    public void setFieldValue( Object value ) throws Exception {
        this.value = validator.transform2Model( value );
    }

}
