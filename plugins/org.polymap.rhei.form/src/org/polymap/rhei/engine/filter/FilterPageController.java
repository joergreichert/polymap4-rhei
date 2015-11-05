/* 
 * polymap.org
 * Copyright (C) 2010-2015, Falko Bräutigam. All rights reserved.
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

import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.rhei.engine.DefaultFormFieldDecorator;
import org.polymap.rhei.engine.DefaultFormFieldLabeler;
import org.polymap.rhei.engine.form.BasePageController;
import org.polymap.rhei.field.NullValidator;
import org.polymap.rhei.filter.IFilterPage;
import org.polymap.rhei.filter.IFilterPage2;
import org.polymap.rhei.filter.IFilterPageSite;
import org.polymap.rhei.form.FieldBuilder;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FilterPageController
        extends BasePageController<FilterFieldComposite> 
        implements IFilterPageSite {
    
    private static Log log = LogFactory.getLog( FilterPageController.class );

    private IFilterPage             page;
    
    
    public FilterPageController( IFilterPage page ) {
        this.page = page;
    }
    
    
    @Override
    public synchronized void dispose() {
        if (page != null && page instanceof IFilterPage2) {
            ((IFilterPage2)page).dispose();
        }
        super.dispose();
    }

    
    @Override
    public boolean isDirty() {
        if (page instanceof IFilterPage2) {
            if (((IFilterPage2)page).isDirty()) {
                return true;
            }
        }
        return super.isDirty();
    }
    
    
    @Override
    public boolean isValid() {
        if (page instanceof IFilterPage2) {
            if (!((IFilterPage2)page).isValid()) {
                return false;
            }
        }
        return super.isValid();
    }
    
    
    public Filter doBuildFilter( IProgressMonitor monitor ) throws Exception {
        Filter result = null;
        
        log.info( "doBuildFilter(): ");
        for (FilterFieldComposite field : fields.values()) {
            if (field.isDirty()) {
                Object value = field.store();
                log.info( "    field: name=" + field.getFieldName() + " value=" + value );
            }
        }

        // after form fields, in order to allow subclassed Property instances
        // to be notified of submit
        if (page instanceof IFilterPage2) {
            result = ((IFilterPage2)page).doBuildFilter( result, monitor );
        }

        return result;
    }


    public void doClear() throws Exception {
        for (FilterFieldComposite field : fields.values()) {
            field.setFieldValue( null );
        }
    }
    
    
    // IFormPageSite ****************************
    
    @Override
    public FieldBuilder newFilterField( String propName, Class<?> propType ) {
        return new FieldBuilder() {
            @Override
            protected Class<?> propBinding() {
                return propType;
            }
            @Override
            protected Composite createFormField() {
                FilterFieldComposite result = new FilterFieldComposite( 
                        getEditor(), 
                        FilterPageController.this, 
                        getToolkit(), 
                        propName, propType, 
                        field.get(),
                        new DefaultFormFieldLabeler( labelWidth.get(), label.get() ), 
                        new DefaultFormFieldDecorator(), 
                        validator.orElse( new NullValidator() ) );
                
                fields.put( result.getFieldName(), result );

                Composite fieldComposite = createFieldComposite( parent.orElse( getPageBody() ) );
                result.createComposite( fieldComposite, SWT.NONE );
                return fieldComposite;
            }
        };
    }

    
    protected abstract Composite createFieldComposite( Composite parent );


    @Override
    public void clear() throws Exception {
        doClear();            
    }

}