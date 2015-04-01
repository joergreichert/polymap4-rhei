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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;

import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldSite;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.form.IFormEditorToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class FormEditingSupport
        extends EditingSupport {

    public static final TableEditorToolkit  DEFAULT_TOOLKIT = new TableEditorToolkit();
    
    private FormFeatureTableColumn      tableColumn;
    
    private IFormField                  field;
    
    private IFormFieldValidator         validator;
    
    
    public FormEditingSupport( ColumnViewer viewer, FormFeatureTableColumn tableColumn, IFormField field, IFormFieldValidator validator ) {
        super( viewer );
        this.tableColumn = tableColumn;
        this.field = field;
        this.validator = validator;
    }

    @Override
    protected boolean canEdit( Object elm ) {
        return true;
    }

    @Override
    protected CellEditor getCellEditor( Object elm ) {
        return new FormCellEditor( (Composite)tableColumn.getViewer().getControl(), (IFeatureTableElement)elm );
    }

    @Override
    protected Object getValue( Object elm ) {
        try {
            IFeatureTableElement featureElm = (IFeatureTableElement)elm;
            Object value = featureElm.getValue( tableColumn.getName() );
            return value;
        }
        catch (Exception e) {
            return "Fehler: " + e.getLocalizedMessage();
        }
    }

    @Override
    protected void setValue( Object elm, Object value ) {
        IFeatureTableElement featureElm = (IFeatureTableElement)elm;
        featureElm.setValue( tableColumn.getName(), value );
        
//        boolean valid = true;
//        tableColumn.markElement( featureElm, true, !valid );
        tableColumn.getViewer().update( elm, null );
    }
    
    
    /**
     * 
     */
    class FormCellEditor
            extends CellEditor {

        private Control                 control;

        private FormFieldSite           fieldSite;
        
        private IFeatureTableElement    elm;

        /** Validated field value set from the UI. */
        private Object                  fieldValue;

        private String                  errorMsg;

        private String                  externalErrorMsg;

        private boolean                 isDirty;

        public FormCellEditor( Composite parent, IFeatureTableElement elm ) {
            super( parent );
            this.elm = elm;
        }

        @Override
        protected Control createControl( Composite parent ) {
            assert control == null;
            this.fieldSite = new FormFieldSite();
            field.init( fieldSite );
            control = field.createControl( parent, DEFAULT_TOOLKIT );
            return control;
        }

        @Override
        protected void doSetValue( Object value ) {
            try {
                // use validator instead of setting directly via field.setValue( value );
                field.load();
            }
            catch (Exception e) {
                throw new RuntimeException( e ); 
            }
        }

        @Override
        protected Object doGetValue() {
            try {
                // use validator instead of setting directly via field.setValue( value );
                field.store();
                
                boolean valid = errorMsg == null;
                tableColumn.markElement( elm, true, !valid );
                return fieldValue;
            }
            catch (Exception e) {
                throw new RuntimeException( e ); 
            }
        }

        @Override
        protected void doSetFocus() {
            control.setFocus();
            // XXX hack as setFocus() is currently missing from IFormField
            if (control instanceof Text) {
                ((Text)control).selectAll();
            }
        }

        /**
         * 
         */
        class FormFieldSite
                implements IFormFieldSite {

            @Override
            public String getFieldName() {
                return tableColumn.getProperty().getName().getLocalPart();
            }

            @Override
            public Object getFieldValue() throws Exception {
                Object value = elm.getValue( tableColumn.getName() );
                return validator.transform2Field( value );
            }

            @Override
            public void setFieldValue( Object value ) throws Exception {
                if (isValid()) {
                    fieldValue = validator.transform2Model( value );
                    elm.setValue( tableColumn.getName(), fieldValue );
                }
            }

            @Override
            public boolean isValid() {
                return errorMsg == null;
            }

            @Override
            public boolean isDirty() {
                return isDirty;
            }

            @Override
            public void addChangeListener( IFormFieldListener l ) {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }

            @Override
            public void removeChangeListener( IFormFieldListener l ) {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }

            @Override
            public void fireEvent( Object source, int eventCode, Object newValue ) {
                Object validatedNewValue = null;

                // check isDirty / validator
                if (eventCode == IFormFieldListener.VALUE_CHANGE) {
                    errorMsg = externalErrorMsg;
                    if (validator != null) {
                        errorMsg = validator.validate( newValue );
                    }
                    if (errorMsg == null) {
                        try {
                            Object value = getFieldValue();
                            if (value == null && newValue == null) {
                                isDirty = false;
                            }
                            else {
                                isDirty = value == null && newValue != null ||
                                        value != null && newValue == null ||
                                        !value.equals( newValue );
                            }
                            validatedNewValue = validator.transform2Model( newValue );
                        }
                        catch (Exception e) {
                            // XXX hmmm... what to do?
                            throw new RuntimeException( e );
                        }
                    }
                    //fieldValue = validatedNewValue;
                }

                FormFieldEvent ev = new FormFieldEvent( 
                        FormEditingSupport.this, source, getFieldName(), field, eventCode, null, validatedNewValue );
                EventManager.instance().syncPublish( ev );
            }

            @Override
            public IFormEditorToolkit getToolkit() {
                throw new RuntimeException( "not yet implemented." );
            }

            @Override
            public String getErrorMessage() {
                return errorMsg;
            }

            @Override
            public void setErrorMessage( String msg ) {
                externalErrorMsg = msg;
            }

        }
    }
    
}
