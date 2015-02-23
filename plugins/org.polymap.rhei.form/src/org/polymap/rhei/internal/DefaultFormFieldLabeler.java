/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
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
 *
 * $Id: $
 */
package org.polymap.rhei.internal;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldSite;
import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.internal.form.FormEditorToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultFormFieldLabeler
        implements IFormFieldLabel, IFormFieldListener {

    public static final String      CUSTOM_VARIANT_VALUE = "formeditor-label";

    private IFormFieldSite      site;
    
    private String              labelStr;
    
    private int                 maxWidth;

    private Label               label;
    
    private Font                orig;
    
    
    /**
     * Use the field name as labelStr. 
     */
    public DefaultFormFieldLabeler( int maxWidth ) {
        this.maxWidth = maxWidth;
    }

    public DefaultFormFieldLabeler( int maxWidth, String label ) {
        if (label != null && label.equals( NO_LABEL )) {
            this.labelStr = label;
            this.maxWidth = 0;
        }
        else {
            this.labelStr = label;
            this.maxWidth = maxWidth;
        }
    }

    public void init( IFormFieldSite _site ) {
        this.site = _site;    
    }

    public void dispose() {
        site.removeChangeListener( this );
    }

    public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
        Control result = null;
        if (labelStr != null && labelStr.equals( NO_LABEL )) {
            result = label = toolkit.createLabel( parent, "" );            
        }
        else {
            result = new Composite( parent, SWT.NO_FOCUS ) {
                public void setEnabled( boolean enabled ) {
                    UIUtils.setVariant( this, enabled ? CUSTOM_VARIANT_VALUE : CUSTOM_VARIANT_VALUE+"-disabled" );
                }
            };
            ((Composite)result).setLayout( new FormLayout() );
            UIUtils.setVariant( result, CUSTOM_VARIANT_VALUE );
            label = toolkit.createLabel( (Composite)result, 
                    labelStr != null ? labelStr : StringUtils.capitalize( site.getFieldName() ), SWT.WRAP );
            label.setLayoutData( FormDataFactory.filled().top( 0, 4 ).create() );
        }
    
        // focus listener
        site.addChangeListener( this );
        return result;
    }

    public void fieldChange( FormFieldEvent ev ) {
        if (label.isDisposed()) {
            return;
        }
        if (ev.getEventCode() == FOCUS_GAINED) {
            label.setForeground( FormEditorToolkit.labelForegroundFocused );
            orig = label.getFont();
//            label.setFont( JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT ) );
        }
        else if (ev.getEventCode() == FOCUS_LOST) {
            label.setForeground( FormEditorToolkit.labelForeground );
//            label.setFont( orig );
        }
    }
    
    public void setMaxWidth( int maxWidth ) {
        this.maxWidth = maxWidth;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

}
