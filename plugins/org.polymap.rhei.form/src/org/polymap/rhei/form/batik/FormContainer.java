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
import static org.polymap.rhei.internal.form.FormEditorToolkit.CUSTOM_VARIANT_VALUE;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.rap.rwt.RWT;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.toolkit.ILayoutContainer;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormPage;
import org.polymap.rhei.form.IFormPageSite;
import org.polymap.rhei.form.IFormToolkit;
import org.polymap.rhei.internal.form.AbstractFormPageContainer;
import org.polymap.rhei.internal.form.FormEditorToolkit;

/**
 * A container for Rhei forms. Sub-classes can use the Rhei form API the
 * create forms that are connected to a feature or entity.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FormContainer
        implements IFormPage {

    public static final String  CSS_FORM = CSS_PREFIX + "-form";
    public static final String  CSS_FORM_DISABLED = CSS_PREFIX + "-form-disabled";
    public static final String  CSS_FORMFIELD = CSS_PREFIX + "-formfield";
    public static final String  CSS_FORMFIELD_DISABLED = CSS_PREFIX + "-formfield-disabled";

    private static Log log = LogFactory.getLog( FormContainer.class );
    
    private FormEditorToolkit   toolkit;

    private Composite           pageBody;

    private PageContainer       pageContainer;
    
    private IFormFieldListener  statusAdapter;

    private boolean             enabled = true;

    private IFormFieldFactory   fieldFactory;


    /**
     * Creates the user interface fields and controls of this form page.
     * <p>
     * <b>Example code:</b>
     * <pre>
     *    site.setFormTitle( "Title" );
     *    site.getPageBody().setLayout( ColumnLayoutFactory.defaults().create() );
     *    
     *    createField( feature.getProperty( "name" ) ).setLabel( "Name" ).create();
     * </pre>
     *
     * @param site The API to create fields and interact with the framework.
     */
    public abstract void createFormContent( IFormPageSite site );

    
    /**
     * Creates the UI of this form by calling
     * {@link #createFormContent(org.polymap.rhei.form.IFormPageSite)}.
     * 
     * @param parent The parent under which to create the form UI controls.
     */
    public final Composite createContents( ILayoutContainer parent ) {
        return createContents( new Composite( parent.getBody(), SWT.NONE ) );
    }

    
    /**
     * Creates the UI of this form by calling
     * {@link #createFormContent(org.polymap.rhei.form.IFormPageSite)}.
     * 
     * @param parent The parent under which to create the form UI controls.
     */
    public final Composite createContents( Composite body ) {
        toolkit = new FormEditorToolkit( new FormToolkit( UIUtils.sessionDisplay() ) );
        pageBody = body;
        UIUtils.setVariant( pageBody, CSS_FORM );
        pageContainer = new PageContainer( this );

        try {
            createFormContent( pageContainer );
            updateEnabled();
            pageContainer.doLoad( new NullProgressMonitor() );
        }
        catch (Exception e) {
            StatusDispatcher.handleError( "An error occured while creating the new page.", e );
        }
        return pageBody;
    }

    
    public final void createContents( FormContainer parent ) {
        toolkit = parent.toolkit;
        pageBody = parent.pageBody;
        pageContainer = parent.pageContainer;

        createFormContent( pageContainer );
        updateEnabled();
    }

    
    public void dispose() {
        if (pageContainer != null) {
            pageContainer.dispose();
            pageContainer = null;
        }
        if (pageBody != null && !pageBody.isDisposed()) {
            pageBody.dispose();
            pageBody = null;
        }
    }
    
    
    public void setFieldBuilderFactory( IFormFieldFactory factory ) {
        this.fieldFactory = factory;
    }
    
    
    public void setEnabled( boolean enabled ) {
        this.enabled = enabled;
        updateEnabled();
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

            // form fields
            if (variant == null 
                    || variant.equals( CSS_FORMFIELD ) || variant.equals( CSS_FORMFIELD_DISABLED ) 
                    || variant.equals( CUSTOM_VARIANT_VALUE ) || variant.equals( CUSTOM_VARIANT_VALUE )) {
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
    
    
    public void submit() throws Exception {
        pageContainer.submitEditor();
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
        pageContainer.addFieldListener( l );
    }

    public void removeFieldListener( IFormFieldListener l ) {
        pageContainer.removeFieldListener( l );
    }
    
    public boolean isDirty() {
        return pageContainer.isDirty();
    }

    public boolean isValid() {
        return pageContainer.isValid();
    }

    public void reloadEditor() throws Exception {
        pageContainer.reloadEditor();
    }

    public void submitEditor() throws Exception {
        pageContainer.submitEditor();
    }

    public void clearFields() {
        pageContainer.clearFields();
    }

    
    /**
     * Activates an adapter that routes form valid status to the given panel and its
     * status line.
     */
    public void activateStatusAdapter( final IPanelSite panelSite ) {
        assert statusAdapter == null;
        pageContainer.addFieldListener( statusAdapter = new IFormFieldListener() {
            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    panelSite.setStatus( pageContainer.isValid() ? Status.OK_STATUS 
                            : new Status( IStatus.WARNING, BatikPlugin.PLUGIN_ID, "Eingaben sind noch nicht vollstÃ¤ndig/korrekt." ));
                }
            }
        });
        // init status message
        statusAdapter.fieldChange( new FormFieldEvent( this, this, null, null, IFormFieldListener.VALUE_CHANGE, null, null ) );
    }
    
    
    // default implementation of IFormPage **********

    @Override
    public final String getTitle() {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public final Action[] getEditorActions() {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public final String getId() {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public final byte getPriority() {
        throw new RuntimeException( "not yet implemented." );
    }


    /**
     * The container for the one and only page of this form.
     */
    protected class PageContainer
            extends AbstractFormPageContainer {

        public PageContainer( IFormPage page ) {
            super( FormContainer.this, page, "_id_", "_title_" );
            double displayWidth = UIUtils.sessionDisplay().getBounds().width;
            // minimum 110
            double width = 110;
            // plus 10px per 100 pixel display width
            if (displayWidth > 1000) {
                width += (displayWidth - 1000) * 0.1;
            }
            // but not more than 160 :)
            width = Math.min( 150, width );
            log.info( "labelWidth: " + width );
            setLabelWidth( (int)width );
        }

        public Composite getPageBody() {
            return pageBody;
        }

        public IFormToolkit getToolkit() {
            return toolkit;
        }

        public void setFormTitle( String title ) {
            throw new RuntimeException( "not yet implemented." );
        }

        public void setEditorTitle( String title ) {
            throw new RuntimeException( "not yet implemented." );
        }

        public void setActivePage( String pageId ) {
            log.warn( "setActivePage() not supported." );
        }
    }
    
}
