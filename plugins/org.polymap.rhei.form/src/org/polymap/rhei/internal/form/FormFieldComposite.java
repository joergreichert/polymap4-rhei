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

import java.util.Objects;

import org.opengis.feature.Property;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldDecorator;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldSite;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NullValidator;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

/**
 * This is the parent Composite of a form field. It consists of an {@link IFormField}
 * , an {@link IFormFieldLabel} and an {@link IFormFieldDecorator}. The
 * FormFieldComposite provides a context via the {@link IFormFieldSite}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FormFieldComposite
        implements IFormFieldSite {

    public static final String      CUSTOM_VARIANT_VALUE = "formeditor-field";
    
    /** Identifies the editor that events belong to. */
    private Object                  editor;
    
    private IFormEditorPageSite     pageSite;
    
    private Property                prop;
    
    private IFormEditorToolkit      toolkit;
    
    private IFormField              field;
    
    private IFormFieldDecorator     decorator;
    
    private IFormFieldLabel         labeler;
    
    /** The validator set by the client code or the {@link NullValidator}. */
    private IFormFieldValidator     validator;
    
    private boolean                 isDirty = false;
    
    /** The current error, externally set or returned by the validator. */
    private String                  errorMsg;
    
    /** Error message set by {@link #setErrorMessage(String)} */
    private String                  externalErrorMsg;


    public FormFieldComposite( Object editor, IFormEditorPageSite pageSite, 
            IFormEditorToolkit toolkit, Property prop, IFormField field,
            IFormFieldLabel labeler, IFormFieldDecorator decorator, IFormFieldValidator validator ) {
        this.editor = editor;
        this.pageSite = pageSite;
        this.prop = prop;
        this.toolkit = toolkit;
        this.field = field;
        this.labeler = labeler;
        this.decorator = decorator;
        this.validator = validator;
    }
    
    
    public Composite createComposite( Composite parent, int style ) {
        final Composite result = toolkit.createComposite( parent, style );
        UIUtils.setVariant( result, CUSTOM_VARIANT_VALUE );
        result.setLayout( new FormLayout() );
        
        labeler.init( this );
        Control labelControl = labeler.createControl( result, toolkit );
        decorator.init( this );
        Control decoControl = decorator.createControl( result, toolkit );
        field.init( this );
        Control fieldControl = field.createControl( result, toolkit );
        int height = SWT.DEFAULT; //fieldControl.computeSize( SWT.DEFAULT, SWT.DEFAULT ).y;
        
        // label
        FormData layoutData = new FormData( labeler.getMaxWidth(), height );
        layoutData.left = new FormAttachment( 0 );
        layoutData.bottom = new FormAttachment( 100 );
        layoutData.top = new FormAttachment( 0 );
        labelControl.setLayoutData( layoutData );
        
        // decorator
        layoutData = new FormData( 19, 20 );
        layoutData.left = new FormAttachment( 100, -19 );
        layoutData.right = new FormAttachment( 100 );
        layoutData.top = new FormAttachment( 0, 0 );
        decoControl.setLayoutData( layoutData );
        
        // field
        layoutData = fieldControl.getLayoutData() != null
                ? (FormData)fieldControl.getLayoutData()
                : new FormData( 50, height );
        layoutData.top = new FormAttachment( 0 );
        layoutData.left = new FormAttachment( labelControl, 5 );
        layoutData.right = new FormAttachment( decoControl, -1 );
        fieldControl.setLayoutData( layoutData );

//        // focus listener
//        addChangeListener( new IFormFieldListener() {
//            Color defaultBg = result.getBackground();
//            public void fieldChange( FormFieldEvent ev ) {
//                if (ev.getEventCode() == FOCUS_GAINED) {
//                    result.setBackground( FormEditorToolkit.backgroundFocused );
//                }
//                else if (ev.getEventCode() == FOCUS_LOST) {
//                    result.setBackground( defaultBg );                    
//                }
//            }
//        });
        
        result.pack( true );
        return result;
    }


    public void dispose() {
        if (field != null) {
            field.dispose();
            field = null;
        }
        if (labeler != null) {
            labeler.dispose();
            labeler = null;
        }
        if (decorator != null) {
            decorator.dispose();
            decorator = null;
        }
    }

    public Property getProperty() {
        return prop;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public boolean isValid() {
        return errorMsg == null;
    }
    
    public void load() throws Exception {
        field.load();
        //isDirty = false;
    }

    public Object store() throws Exception {
        field.store();
        
        // set isDirty and inform decorator
        fireEvent( this, IFormFieldListener.VALUE_CHANGE, getFieldValue() );
        
        return prop.getValue();
    }

    public void setFormFieldValue( Object value ) {
        field.setValue( value );
    }
    
    public void setEnabled( boolean enabled ) {
        field.setEnabled( enabled );
    }

    // IFormFieldSite *************************************
        
    public IFormField getField() {
        return field;
    }


    public String getFieldName() {
        return prop.getName().getLocalPart();
    }

    public Object getFieldValue() throws Exception {
        return validator.transform2Field( prop.getValue() );
    }

    public void setFieldValue( Object value ) throws Exception {
        prop.setValue( validator.transform2Model( value ) );
    }

    public IFormEditorToolkit getToolkit() {
        return toolkit;
    }

    public void addChangeListener( IFormFieldListener l ) {
        EventManager.instance().subscribe( l, new EventFilter<FormFieldEvent>() {
            public boolean apply( FormFieldEvent ev ) {
                return ev.getFormField() == field;
            }
        });
    }
    
    public void removeChangeListener( IFormFieldListener l ) {
        EventManager.instance().unsubscribe( l );
    }
    
    @Override
    public void fireEvent( Object source, int eventCode, Object newFieldValue ) {
        // check isDirty / validator
        if (eventCode == IFormFieldListener.VALUE_CHANGE) {
            try {
                Object fieldValue = getFieldValue();
                isDirty = !Objects.equals( fieldValue, newFieldValue );

                errorMsg = externalErrorMsg != null
                        ? externalErrorMsg
                        : validator.validate( newFieldValue );

                // if valid -> transform to model value
                Object newModelValue = errorMsg == null
                        ? validator.transform2Model( newFieldValue )
                        : null;
                
                pageSite.fireEvent( source, getFieldName(), eventCode, newFieldValue, newModelValue );
            }
            catch (Exception e) {
                throw new RuntimeException( "Exception while validating field value.", e );
            }
        }
    }

    public String getErrorMessage() {
        return errorMsg;
    }

    public void setErrorMessage( String msg ) {
        externalErrorMsg = msg;
    }

}
