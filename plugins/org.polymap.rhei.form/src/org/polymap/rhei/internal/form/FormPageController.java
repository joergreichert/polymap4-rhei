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
package org.polymap.rhei.internal.form;

import java.util.HashMap;
import java.util.Map;

import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.rhei.field.NullValidator;
import org.polymap.rhei.form.FieldBuilder;
import org.polymap.rhei.form.IFormPage;
import org.polymap.rhei.form.IFormPage2;
import org.polymap.rhei.form.IFormPageSite;
import org.polymap.rhei.internal.DefaultFormFieldDecorator;
import org.polymap.rhei.internal.DefaultFormFieldLabeler;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FormPageController
        extends BasePageController<FormFieldComposite> 
        implements IFormPageSite {
    
    private static Log log = LogFactory.getLog( FormPageController.class );

    private IFormPage               page;
    
    
    public FormPageController( Object editor, IFormPage page, String id, String title ) {
        super( editor, id, title );
        this.page = page;
    }
    
    
    @Override
    public synchronized void dispose() {
        if (page != null && page instanceof IFormPage2) {
            ((IFormPage2)page).dispose();
        }
        super.dispose();
    }

    
    @Override
    public boolean isDirty() {
        if (page instanceof IFormPage2) {
            if (((IFormPage2)page).isDirty()) {
                return true;
            }
        }
        return super.isDirty();
    }
    
    
    @Override
    public boolean isValid() {
        if (page instanceof IFormPage2) {
            if (!((IFormPage2)page).isValid()) {
                return false;
            }
        }
        return super.isValid();
    }
    
    
    public Map<Property,Object> doSubmit( IProgressMonitor monitor ) throws Exception {
        Map<Property,Object> result = new HashMap();
        
        for (FormFieldComposite field : fields.values()) {
            if (field.isDirty()) {
                Object newValue = field.store();
                Object old = result.put( field.getProperty(), newValue );
                if (old != null) {
                    throw new RuntimeException( "Submitted value already exists for property: " + field.getProperty() );
                }
            }
        }

        // after form fields in order to allow subclassed Property instances
        // to be notified of submit
        if (page instanceof IFormPage2) {
            ((IFormPage2)page).doSubmit( monitor );
        }

        return result;
    }

    
    public void doLoad( IProgressMonitor monitor ) throws Exception {
        if (page instanceof IFormPage2) {
            ((IFormPage2)page).doLoad( monitor );
        }

        try {
            // do not dispatch events while loading
//            blockEvents = true;

            for (FormFieldComposite field : fields.values()) {
                field.load();
            }
        }
        finally {
//            blockEvents = false;
        }
    }

    
    // IFormPageSite ****************************
    
    @Override
    public FieldBuilder newFormField( Property property ) {
        return new FieldBuilder() {
            @Override
            protected Class<?> propBinding() {
                return property.getType().getBinding();
            }
            @Override
            protected Composite createFormField() {
                FormFieldComposite result = new FormFieldComposite( 
                        editor, 
                        FormPageController.this, 
                        getToolkit(), 
                        property, 
                        field.get(),
                        new DefaultFormFieldLabeler( labelWidth, label.get() ), 
                        new DefaultFormFieldDecorator(), 
                        validator.orElse( new NullValidator() ) );
                
                fields.put( result.getFieldName(), result );

                return result.createComposite( parent.orElse( getPageBody() ), SWT.NONE );
            }
        };
    }


    @Override
    public void reloadEditor() throws Exception {
        doLoad( new NullProgressMonitor() );
    }

    
    @Override
    public void submitEditor() throws Exception {
        doSubmit( new NullProgressMonitor() );
    }

}