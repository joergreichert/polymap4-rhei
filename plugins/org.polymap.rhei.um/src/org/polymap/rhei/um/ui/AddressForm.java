/* 
 * polymap.org
 * Copyright (C) 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.um.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.IMessages;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.app.FormContainer;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.um.Address;
import org.polymap.rhei.um.Property;
import org.polymap.rhei.um.internal.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AddressForm
        extends FormContainer { 

    private static Log log = LogFactory.getLog( AddressForm.class );

    public static final IMessages i18n = Messages.forPrefix( "AddressForm" );

    private IPanelSite              panelSite;

    private IFormEditorPageSite     site;
    
    private Address                 address;

    
    public AddressForm( IPanelSite panelSite, Address address ) {
        this.panelSite = panelSite;
        this.address = address;
    }


    public void createFormContent( final IFormEditorPageSite _site ) {
        this.site = _site;
        IFormEditorToolkit tk = site.getToolkit();
        Composite body = site.getPageBody();
        //body.setLayout( ColumnLayoutFactory.defaults().spacing( 10 ).margins( 20, 20 ).create() );

        int spacing = panelSite.getLayoutPreference( IPanel.LAYOUT_SPACING_KEY );
        
        // street / number
        Composite str = tk.createComposite( body );
        str.setLayout( new FillLayout( SWT.HORIZONTAL) );

        Property<String> prop = address.street();
        new FormFieldBuilder( str, new PropertyAdapter( prop ) ).setLabel( i18n.get( prop.name() ) )
                .setValidator( new NotNullValidator() )
                .create();  //.setLayoutData( FormDataFactory.filled().right( 75 ).create() );
        
        prop = address.number();
        new FormFieldBuilder( str, new PropertyAdapter( prop ) ).setLabel( IFormFieldLabel.NO_LABEL )
                .setValidator( new NotNullValidator() )
                .create();  //.setLayoutData( FormDataFactory.filled().left( 75 ).create() );

        // postalCode / city
        Composite city = tk.createComposite( body );
        city.setLayout( new FillLayout( SWT.HORIZONTAL) );
        
        prop = address.postalCode();
        new FormFieldBuilder( city, new PropertyAdapter( prop ) ).setLabel( i18n.get( prop.name() ) )
                .setValidator( new NotNullValidator() )
                .create();  //.setLayoutData( FormDataFactory.filled().right( 25 ).create() );

        prop = address.city();
        new FormFieldBuilder( city, new PropertyAdapter( prop ) ).setLabel( IFormFieldLabel.NO_LABEL )
                .setValidator( new NotNullValidator() )
                .create();  //.setLayoutData( FormDataFactory.filled().left( 25 ).create() );
        
//        // country 
//        prop = address.country();
//        new FormFieldBuilder( body, new PropertyAdapter( prop ) ).setLabel( i18n.get( prop.name() ) )
//                .setValidator( new NotNullValidator() ).create();
    }

}
