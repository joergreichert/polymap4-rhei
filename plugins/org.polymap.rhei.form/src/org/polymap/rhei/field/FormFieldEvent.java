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
package org.polymap.rhei.field;

import java.util.EventObject;

import com.google.common.base.Optional;

import org.polymap.rhei.filter.FilterEditor;

/**
 * Event thrown by a form field and handled by an {@link IFormFieldListener}. 
 *
 * @see IFormFieldListener
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FormFieldEvent
        extends EventObject {

    private Object      editor;
    
    private IFormField  formField;
    
    private int         eventCode;
    
    private Object      newFieldValue;
    
    private Object      newModelValue;

    private String      fieldName;


    public FormFieldEvent( Object editor, Object source, String fieldName, IFormField field, 
            int eventCode, Object newFieldValue, Object newModelValue ) {
        super( source );
        this.editor = editor;
        this.formField = field;
        this.fieldName = fieldName;
        this.eventCode = eventCode;
        this.newFieldValue = newFieldValue;
        this.newModelValue = newModelValue;
    }
    
    /**
     * The editor that this event belongs to.
     *
     * @return {@link org.polymap.rhei.form.workbench.FormEditor} or {@link FilterEditor}
     */
    public Object getEditor() {
        return editor;
    }
    
    public String getFieldName() {
        return fieldName;
    }

    public IFormField getFormField() {
        return formField;
    }

    public int getEventCode() {
        return eventCode;
    }
    
    /**
     * The value that would be stored in the underlying Property on submit. Type and
     * value my differ from the value that was actually entered in the
     * {@link IFormField}, it depends on the {@link IFormFieldValidator} of the form
     * field.
     * 
     * @see #getNewFieldValue()
     * @return An optional value that is present only if the
     *         {@link #getNewFieldValue() new field value} was successfully
     *         validated.
     */
    public <T> Optional<T> getNewModelValue() {
        return Optional.fromNullable( (T)newModelValue );
    }

    /**
     * The value that was entered in the form field. The type and value depend on
     * the {@link IFormField} that is used in this form field, it may differ from
     * type and value of the underlying Property of this form field.
     *
     * @see #getNewModelValue()
     * @return The value as entered in the {@link IFormField}.
     */
    public <T> T getNewFieldValue() {
        return (T)newFieldValue;
    }

    public String toString() {
        return "FormFieldEvent[source=" + source.getClass().getSimpleName() + 
                ", eventCode=" + eventCode + 
                ", formField=" + formField.getClass().getSimpleName() +
                ", newFieldValue=" + newFieldValue + 
                ", newModelValue=" + newModelValue + "]";
    }

}
