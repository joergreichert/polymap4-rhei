/*
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.batik.app;

import static org.polymap.rhei.batik.internal.desktop.DesktopToolkit.CSS_FORM;
import static org.polymap.rhei.batik.internal.desktop.DesktopToolkit.CSS_FORMFIELD;
import static org.polymap.rhei.batik.internal.desktop.DesktopToolkit.CSS_FORMFIELD_DISABLED;
import static org.polymap.rhei.batik.internal.desktop.DesktopToolkit.CSS_FORM_DISABLED;

import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.toolkit.ILayoutContainer;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.internal.form.AbstractFormEditorPageContainer;
import org.polymap.rhei.internal.form.FormEditorToolkit;

/**
 * A container for Rhei forms. Sub-classes can use the Rhei form API the
 * create forms that are connected to features or entities.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FormContainer
        implements IFormEditorPage {

    private static Log log = LogFactory.getLog( FormContainer.class );

    private FormEditorToolkit   toolkit;

    private Composite           pageBody;

    private PageContainer       pageSite;
    
    private IFormFieldListener  statusAdapter;

    private boolean             enabled = true;


    public final Composite createContents( ILayoutContainer parent ) {
        return createContents( new Composite( parent.getBody(), SWT.NONE ) );
    }

    public Composite createContents( Composite body ) {
        toolkit = new FormEditorToolkit( new FormToolkit( Polymap.getSessionDisplay() ) );
        pageBody = body;
        pageBody.setData( WidgetUtil.CUSTOM_VARIANT, CSS_FORM  );
        pageSite = new PageContainer( this );

        try {
            createFormContent( pageSite );
            updateEnabled();
            pageSite.doLoad( new NullProgressMonitor() );
        }
        catch (Exception e) {
            BatikApplication.handleError( BatikPlugin.PLUGIN_ID, "An error occured while creating the new page.", e );
        }
        return pageBody;
    }

    public final void createContents( FormContainer parent ) {
        toolkit = parent.toolkit;
        pageBody = parent.pageBody;
        pageSite = parent.pageSite;

        createFormContent( pageSite );
        updateEnabled();
    }

    public void dispose() {
        if (pageSite != null) {
            pageSite.dispose();
            pageSite = null;
        }
        if (pageBody != null && !pageBody.isDisposed()) {
            pageBody.dispose();
            pageBody = null;
        }
    }
    
    public void setEnabled( boolean enabled ) {
        this.enabled = enabled;
        updateEnabled();
    }
    
    protected void updateEnabled() {
        if (pageBody == null || pageBody.isDisposed()) {
            return;
        }
        Font font = enabled 
                ? JFaceResources.getFontRegistry().get( JFaceResources.DEFAULT_FONT )
                : JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT );
        
        Deque<Control> deque = new LinkedList();
        deque.push( pageBody );
        while (!deque.isEmpty()) {
            Control control = deque.pop();
            if (control instanceof Label) {
                control.setFont( font );
            }
            
            if (control instanceof Composite) {
                control.setEnabled( false );
                
                String variant = (String)control.getData( WidgetUtil.CUSTOM_VARIANT );
                if (variant.equals( CSS_FORM ) || variant.equals( CSS_FORM_DISABLED )) {
                    control.setData( WidgetUtil.CUSTOM_VARIANT, enabled ? CSS_FORM : CSS_FORM_DISABLED  );
                }
                else if (variant.equals( CSS_FORMFIELD ) || variant.equals( CSS_FORMFIELD_DISABLED )) {
                    control.setData( WidgetUtil.CUSTOM_VARIANT, enabled ? CSS_FORMFIELD : CSS_FORMFIELD_DISABLED  );
                }
                        
                for (Control child : ((Composite)control).getChildren()) {
                    deque.push( child );
                }
            }
        }
    }
    
    public void submit() throws Exception {
        pageSite.submitEditor();
    }
    
    public void addFieldListener( IFormFieldListener l ) {
        pageSite.addFieldListener( l );
    }

    public void removeFieldListener( IFormFieldListener l ) {
        pageSite.removeFieldListener( l );
    }

    public boolean isDirty() {
        return pageSite.isDirty();
    }

    public boolean isValid() {
        return pageSite.isValid();
    }

    public void reloadEditor() throws Exception {
        pageSite.reloadEditor();
    }

    public void submitEditor() throws Exception {
        pageSite.submitEditor();
    }

    public void clearFields() {
        pageSite.clearFields();
    }

    /**
     * Activates an adapter that routes form valid status to the given panel and its
     * status line.
     */
    public void activateStatusAdapter( final IPanelSite panelSite ) {
        assert statusAdapter == null;
        pageSite.addFieldListener( statusAdapter = new IFormFieldListener() {
            @Override
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    panelSite.setStatus( pageSite.isValid() ? Status.OK_STATUS 
                            : new Status( IStatus.ERROR, BatikPlugin.PLUGIN_ID, "Eingaben noch nicht vollständig/korrekt." ));
                }
            }
        });
        // init status message
        statusAdapter.fieldChange( new FormFieldEvent( this, this, null, null, IFormFieldListener.VALUE_CHANGE, null, null ) );
    }
    
    
    // default implementation of IFormEditorPage **********

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
     *
     */
    class PageContainer
            extends AbstractFormEditorPageContainer {

        public PageContainer( IFormEditorPage page ) {
            super( FormContainer.this, page, "_id_", "_title_" );
            setLabelWidth( 150 );
        }

        public void createContent() {
            page.createFormContent( this );
        }

        @Override
        public Composite newFormField( Composite parent, Property prop, IFormField field,
                IFormFieldValidator validator, String label ) {
            Composite result = super.newFormField( parent, prop, field, validator, label );
            result.setData( WidgetUtil.CUSTOM_VARIANT, CSS_FORM );
            return result;
        }

        public Composite getPageBody() {
            return pageBody;
        }

        public IFormEditorToolkit getToolkit() {
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

    
    /**
     * This field builder allows to create a new form field. It provides a simple,
     * chainable API that allows to set several aspects of the result. If an aspect
     * is not set then a default is computed.
     */
    public class FormFieldBuilder {
        
        private String              propName;
        
        private Composite           parent;
        
        private String              label;
        
        private String              tooltip;
        
        private Feature             builderFeature;
        
        private Property            prop;
        
        private IFormField          field;
        
        private IFormFieldValidator validator;
        
        private boolean             fieldEnabled = true;

        private Object              layoutData;

        
        public FormFieldBuilder( Property prop ) {
            this.prop = prop;
        }

        public FormFieldBuilder( Composite parent, Property prop ) {
            this.parent = parent;
            this.prop = prop;
        }

        public FormFieldBuilder( Feature feature, String propName ) {
            this.propName = propName;
//            this.label = propName;
            this.builderFeature = feature;
        }

        public FormFieldBuilder( Feature feature, String propName, Composite parent) {
            this( feature, propName );
            this.parent = parent;
        }
        
        public FormFieldBuilder setParent( Composite parent ) {
            this.parent = parent instanceof Section 
                    ? (Composite)((Section)parent).getClient() : parent;
            return this;
        }

        public FormFieldBuilder setProperty( Property prop ) {
            this.prop = prop;
            return this;
        }

        public FormFieldBuilder setFeature( Feature feature ) {
            this.builderFeature = feature;
            return this;
        }
        
        public FormFieldBuilder setLabel( String label ) {
            this.label = label;
            return this;
        }
        
        public FormFieldBuilder setToolTipText( String tooltip ) {
            this.tooltip = tooltip;
            return this;
        }

        public FormFieldBuilder setField( IFormField field ) {
            this.field = field;
            return this;
        }

        public FormFieldBuilder setValidator( IFormFieldValidator validator ) {
            this.validator = validator;
            return this;
        }

        public FormFieldBuilder setEnabled( boolean enabled ) {
            this.fieldEnabled = enabled;
            return this;
        }
        
        public FormFieldBuilder setLayoutData( Object data ) {
            this.layoutData = data;
            return this;
        }
        
        public Composite create() {
            if (parent == null) {
                parent = pageSite.getPageBody();
            }
            if (prop == null) {
                prop = builderFeature.getProperty( propName );
                if (prop == null) {
                    throw new IllegalStateException( "No such property: " + propName );
                }
            }
            if (field == null) {
                Class binding = prop.getType().getBinding();
                // Number
                if (Number.class.isAssignableFrom( binding )) {
                    field = new StringFormField();
                    validator = new NumberValidator( binding, Polymap.getSessionLocale() );
                }
                // Date
                else if (Date.class.isAssignableFrom( binding )) {
                    field = new DateTimeFormField();
                }
                // Boolean
                else if (Date.class.isAssignableFrom( binding )) {
                    field = new CheckboxFormField();
                }
                // default: String
                else {
                    field = new StringFormField();
                }
            }
            Composite result = pageSite.newFormField( parent, prop, field, validator, label );
            // layoutData
            if (layoutData != null) {
                result.setLayoutData( layoutData );
            }
//            else {
//                applyLayout( result );
//            }
            // tooltip
            if (tooltip != null) {
                result.setToolTipText( tooltip );
            }
            // editable
            if (!fieldEnabled) {
                pageSite.setFieldEnabled( prop.getName().getLocalPart(), fieldEnabled );
            }
            return result;
        }
    }

}
