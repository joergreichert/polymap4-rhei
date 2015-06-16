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

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.NotEmptyValidator;
import org.polymap.rhei.form.DefaultFormPage;
import org.polymap.rhei.form.IFormPageSite;
import org.polymap.rhei.form.IFormToolkit;
import org.polymap.rhei.um.Address;
import org.polymap.rhei.um.Property;
import org.polymap.rhei.um.internal.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AddressForm
        extends DefaultFormPage {

    private static Log log = LogFactory.getLog( AddressForm.class );

    public static final IMessages i18n = Messages.forPrefix( "AddressForm" );

    private IPanelSite              panelSite;

    private IFormPageSite           site;
    
    private Address                 address;

    
    public AddressForm( IPanelSite panelSite, Address address ) {
        this.panelSite = panelSite;
        this.address = address;
    }


    public void createFormContents( final IFormPageSite _site ) {
        this.site = _site;
        IFormToolkit tk = site.getToolkit();
        Composite body = site.getPageBody();
        //body.setLayout( ColumnLayoutFactory.defaults().spacing( 10 ).margins( 20, 20 ).create() );

        //int spacing = panelSite.getLayoutPreference( IPanel.LAYOUT_SPACING_KEY );
        
        // street / number
        Composite str = tk.createComposite( body );
        str.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
        //str.setLayout( new FillLayout( SWT.HORIZONTAL) );

        Composite field = null;
        Property<String> prop = address.street();
        field = _site.newFormField( new PropertyAdapter( prop ) )
                .parent.put( str )
                .label.put( i18n.get( prop.name() ) )
                .validator.put( new NotEmptyValidator() )
                .create();
        field.setLayoutData( FormDataFactory.filled().right( 75 ).create() );
        
        prop = address.number();
        _site.newFormField( new PropertyAdapter( prop ) )
                .parent.put( str )
                .label.put( IFormFieldLabel.NO_LABEL )
                .validator.put( new NotEmptyValidator() )
                .create()
                .setLayoutData( FormDataFactory.filled().left( field ).create() );

        // postalCode / city
        Composite city = tk.createComposite( body );
        //city.setLayout( new FillLayout( SWT.HORIZONTAL) );
        city.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
        //city.setLayout( RowLayoutFactory.fillDefaults().spacing( 5 ).pack( false ).create() );
        
        prop = address.postalCode();
        field = _site.newFormField( new PropertyAdapter( prop ) )
                .parent.put( city )
                .label.put( i18n.get( prop.name() ) )
                .validator.put( new PlzValidator())
                .create();
        field.setLayoutData( FormDataFactory.filled().right( 50 ).create() );

        prop = address.city();
        _site.newFormField( new PropertyAdapter( prop ) )
                .parent.put( city )
                .label.put( IFormFieldLabel.NO_LABEL )
                .validator.put( new NotEmptyValidator() )
                .create()
                .setLayoutData( FormDataFactory.filled().left( field ).create() );
        
//        // country 
//        prop = address.country();
//        new FormFieldBuilder( body, new PropertyAdapter( prop ) ).setLabel( i18n.get( prop.name() ) )
//                .setValidator( new NotNullValidator() ).create();
        
        body.layout();
    }

}
