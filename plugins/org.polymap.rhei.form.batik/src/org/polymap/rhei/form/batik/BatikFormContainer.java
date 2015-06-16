/*
 * polymap.org
 * Copyright (C) 2013-2015, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.form.batik;

import static org.polymap.rhei.batik.app.DefaultToolkit.CSS_PREFIX;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rap.rwt.RWT;

import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.toolkit.ILayoutContainer;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.FormPageContainer;
import org.polymap.rhei.form.IFormPage;
import org.polymap.rhei.internal.form.BaseFieldComposite;

/**
 * A container for Rhei forms. Sub-classes can use the Rhei form API the
 * create forms that are connected to a feature or entity.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BatikFormContainer
        extends FormPageContainer {

    private static Log log = LogFactory.getLog( BatikFormContainer.class );
    
    public static final String  CSS_FORM = CSS_PREFIX + "-form";
    public static final String  CSS_FORM_DISABLED = CSS_PREFIX + "-form-disabled";
    public static final String  CSS_FORMFIELD = CSS_PREFIX + "-formfield";
    public static final String  CSS_FORMFIELD_DISABLED = CSS_PREFIX + "-formfield-disabled";

    private IFormFieldListener  statusAdapter;

    private IFormFieldFactory   fieldFactory;


    public BatikFormContainer( IFormPage page ) {
        super( page );
    }


    /**
     * Creates the UI of this form by calling
     * {@link #createFormContent(org.polymap.rhei.form.IFormPageSite)}.
     * 
     * @param parent The parent under which to create the form UI controls.
     */
    public final Composite createContents( ILayoutContainer parent ) {
        return createContents( parent.getBody() );
    }

    
    @Override
    protected Composite createBody( Composite parent ) {
        return UIUtils.setVariant( super.createBody( parent ), CSS_FORM );
    }


    @Override
    protected void updateEnabled() {
        if (pageBody == null || pageBody.isDisposed()) {
            return;
        }

        Deque<Control> deque = new LinkedList( Collections.singleton( pageBody ) );
        while (!deque.isEmpty()) {
            Control control = deque.pop();
            
            String variant = (String)control.getData( RWT.CUSTOM_VARIANT );
            log.debug( "VARIANT: " + variant + " (" + control.getClass().getSimpleName() + ")" );

            // form fields
            if (variant == null 
                    || variant.equals( CSS_FORMFIELD ) || variant.equals( CSS_FORMFIELD_DISABLED ) 
                    || variant.equals( BaseFieldComposite.CUSTOM_VARIANT_VALUE )) {
                UIUtils.setVariant( control, enabled ? CSS_FORMFIELD : CSS_FORMFIELD_DISABLED  );
            }
            // form
            else if (variant.equals( CSS_FORM ) || variant.equals( CSS_FORM_DISABLED )) {
                UIUtils.setVariant( control, enabled ? CSS_FORM : CSS_FORM_DISABLED  );
            }

            
//            // labeler Label
//            String labelVariant = (String)control.getData( WidgetUtil.CUSTOM_VARIANT );
//            if (control instanceof Label
//                    && (labelVariant.equals( CSS_FORMFIELD ) || labelVariant.equals( CSS_FORMFIELD_DISABLED ))) {
//                control.setFont( enabled 
//                        ? JFaceResources.getFontRegistry().get( JFaceResources.DEFAULT_FONT )
//                        : JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT ) );
//
//                if (!enabled) {
//                    control.setBackground( Graphics.getColor( 0xED, 0xEF, 0xF1 ) );
//                }
//            }
            // Composite
            if (control instanceof Composite) {
                control.setEnabled( enabled );
                
                deque.addAll( Arrays.asList( ((Composite)control).getChildren() ) );                
            }
            variant = (String)control.getData( RWT.CUSTOM_VARIANT );
            log.debug( "      -> " + variant + " (" + control.getClass().getSimpleName() + ")" );
        }
    }

    
    public void setFieldBuilderFactory( IFormFieldFactory factory ) {
        this.fieldFactory = factory;
    }
    
    
    /**
     * Activates an adapter that routes form valid status to the given panel and its
     * status line.
     */
    public void activateStatusAdapter( final IPanelSite panelSite ) {
        assert statusAdapter == null;
        pageController.addFieldListener( statusAdapter = new IFormFieldListener() {
            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    panelSite.setStatus( pageController.isValid() ? Status.OK_STATUS 
                            : new Status( IStatus.WARNING, BatikPlugin.PLUGIN_ID, "Eingaben sind noch nicht vollständig/korrekt." ));
                }
            }
        });
        // init status message
        statusAdapter.fieldChange( new FormFieldEvent( this, this, null, null, IFormFieldListener.VALUE_CHANGE, null, null ) );
    }
    
}
