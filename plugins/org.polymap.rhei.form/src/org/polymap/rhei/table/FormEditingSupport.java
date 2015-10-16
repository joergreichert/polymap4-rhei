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

import java.util.Objects;

import com.google.common.base.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;

import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.PlainLazyInit;
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
            return featureElm.getValue( tableColumn.getName() );
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

        private Color                   defaultBackground;

        private FormFieldSite           fieldSite;
        
        private IFeatureTableElement    elm;

        private String                  errorMsg;

        private String                  externalErrorMsg;

        private boolean                 isDirty;
        
        private Lazy<IFormFieldValidator> initializedValidator = new PlainLazyInit( new Supplier<IFormFieldValidator>() {
            @Override
            public IFormFieldValidator get() {
                if (validator instanceof ITableFieldValidator) {
                    ((ITableFieldValidator)validator).init( elm );
                }
                return validator;
            }
        });

        
        public FormCellEditor( Composite parent, IFeatureTableElement elm ) {
            super( parent );
            this.elm = elm;
        }

        @Override
        protected Control createControl( Composite parent ) {
            assert control == null;
            fieldSite = new FormFieldSite();
            field.init( fieldSite );
            
            control = field.createControl( parent, DEFAULT_TOOLKIT );
            defaultBackground = control.getBackground();
            control.addKeyListener( new KeyAdapter() {
                public void keyReleased( KeyEvent ev ) {
                    if (ev.keyCode == SWT.Selection) {
                        deactivate();
                    }
                    if (ev.character == '\u001b') { // Escape
                        deactivate();
                    }
                }
            });
            return control;
        }

        @Override
        protected void doSetValue( Object value ) {
            try {
                // validate and set value
                field.load();
            }
            catch (Exception e) {
                throw new RuntimeException( e ); 
            }
        }

        @Override
        protected Object doGetValue() {
            try {
                // validate and write value
                field.store();
                
                tableColumn.markElement( elm, fieldSite.isDirty(), !fieldSite.isValid() );
                return fieldSite.getValue();
            }
            catch (Exception e) {
                throw new RuntimeException( e ); 
            }
        }

        @Override
        protected void doSetFocus() {
            control.setFocus();
//            // XXX hack as setFocus() is currently missing from IFormField
//            if (control instanceof Text) {
//                ((Text)control).selectAll();
//            }
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

            protected Object getValue() throws Exception {
                return elm.getValue( tableColumn.getName() );
            }

            @Override
            public Object getFieldValue() throws Exception {
                return initializedValidator.get().transform2Field( getValue() );
            }

            @Override
            public void setFieldValue( Object value ) throws Exception {
                if (isValid()) {
                    Object fieldValue = initializedValidator.get().transform2Model( value );
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
            public void fireEvent( Object source, int eventCode, Object newFieldValue ) {
                // check isDirty / validator
                if (eventCode == IFormFieldListener.VALUE_CHANGE) {
                    try {
                        // isDirty?
                        isDirty = !Objects.equals( getFieldValue(), newFieldValue );

                        // isValid?
                        errorMsg = externalErrorMsg == null ? initializedValidator.get().validate( newFieldValue ) : externalErrorMsg;
                        if (isValid()) {
                            // tranform
                            Object validatedNewValue = validator.transform2Model( newFieldValue );
                            // fire event
                            EventManager.instance().syncPublish( new FormFieldEvent( FormEditingSupport.this, source, 
                                    getFieldName(), field, eventCode, null, validatedNewValue ) );
                        }
                    }
                    catch (Exception e) {
                        throw new RuntimeException( "Exception while validating field value.", e );
                    }
                }
                
                // show status background
                control.setBackground( defaultBackground );
                if (isDirty()) {
                    control.setBackground( FormFeatureTableColumn.DIRTY_BACKGROUND );
                }
                if (!isValid()) {
                    control.setBackground( FormFeatureTableColumn.INVALID_BACKGROUND );
                }
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