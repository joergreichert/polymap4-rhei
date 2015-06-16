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
package org.polymap.rhei.form;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.eclipse.rap.rwt.RWT;

import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormPageSite;
import org.polymap.rhei.internal.form.BasePageController;
import org.polymap.rhei.internal.form.FormEditorToolkit;

/**
 * Base class for one-page UI containers like dialog or Batik forms/filters.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class BasePageContainer<P extends IBasePage,C extends BasePageController> {

    private static Log log = LogFactory.getLog( BasePageContainer.class );

    protected P                     page;
    
    protected C                     pageController;
    
    protected FormEditorToolkit     toolkit;

    protected Composite             pageBody;

    protected boolean               enabled = true;

    
    /**
     * Creates the UI of this form.
     * 
     * @param parent The parent under which to create the form UI controls.
     */
    public void createContents( Composite parent ) {
        assert page != null : "";
        assert pageController != null : "";
        toolkit = new FormEditorToolkit( new FormToolkit( UIUtils.sessionDisplay() ) );
        pageBody = createBody( parent );
    }

    
    public Composite getContents() {
        assert pageBody != null : "Call createContents() first!";
        return pageBody;
    }
    
    
    /**
     * Creates the body Composite of the form. This default implementation calls
     * <code>toolkit.createComposite(parent)</code> and sets {@link ColumnLayout} on
     * the resulting Composite. Override to change this behaviour.
     *
     * @param parent
     * @return Newly created body Composite for this form.
     */
    protected Composite createBody( Composite parent ) {
        Composite result = toolkit.createComposite( parent );
        result.setLayout( ColumnLayoutFactory.defaults().spacing( 5 ).columns( 1, 2 ).margins( 5 ).create() );
        return result;
    }

    
    public void dispose() {
        if (pageController != null) {
            pageController.dispose();
            pageController = null;
        }
        if (pageBody != null && !pageBody.isDisposed()) {
            pageBody.dispose();
            pageBody = null;
        }
    }
    
    /**
     * 
     *
     * @param enabled
     * @return this
     */
    public <T extends BasePageContainer<P,C>> T setEnabled( boolean enabled ) {
        this.enabled = enabled;
        updateEnabled();
        return (T)this;
    }
    
    
    protected void updateEnabled() {
        if (pageBody == null || pageBody.isDisposed()) {
            return;
        }

        Deque<Control> deque = new LinkedList( Collections.singleton( pageBody ) );
        while (!deque.isEmpty()) {
            Control control = deque.pop();
            
            String variant = (String)control.getData( RWT.CUSTOM_VARIANT );
            log.debug( "VARIANT: " + variant + " (" + control.getClass().getSimpleName() + ")" );

            log.warn( "!!! NOT YET (RE)IMPLEMENTED !!!" );
//            // form fields
//            if (variant == null 
//                    || variant.equals( CSS_FORMFIELD ) || variant.equals( CSS_FORMFIELD_DISABLED ) 
//                    || variant.equals( CUSTOM_VARIANT_VALUE ) || variant.equals( CUSTOM_VARIANT_VALUE )) {
//                UIUtils.setVariant( control, enabled ? CSS_FORMFIELD : CSS_FORMFIELD_DISABLED  );
//            }
//            // form
//            else if (variant.equals( CSS_FORM ) || variant.equals( CSS_FORM_DISABLED )) {
//                UIUtils.setVariant( control, enabled ? CSS_FORM : CSS_FORM_DISABLED  );
//            }

            
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
    
    
    /**
     * Registers the given listener that is notified about changes of an {@link IFormField}.
     * <p/>
     * The listener is handled by the global {@link EventManager}. The caller has to make
     * sure that there is a (strong) reference as long as the listener is active.
     *
     * @param listener
     * @throws IllegalStateException If the given listener is registered already.
     * @see IFormPageSite#addFieldListener(IFormFieldListener)
     * @see EventManager#subscribe(Object, org.polymap.core.runtime.event.EventFilter...)
     */
    public void addFieldListener( IFormFieldListener l ) {
        pageController.addFieldListener( l );
    }

    
    public void removeFieldListener( IFormFieldListener l ) {
        pageController.removeFieldListener( l );
    }
    
    
    public boolean isDirty() {
        return pageController.isDirty();
    }

    
    public boolean isValid() {
        return pageController.isValid();
    }


    public void clearFields() {
        pageController.clearFields();
    }
    
}
