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

import org.polymap.core.runtime.IMessages;

import org.polymap.rhei.batik.app.FormContainer;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.um.Address;
import org.polymap.rhei.um.Messages;
import org.polymap.rhei.um.Property;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AddressForm
        extends FormContainer { 

    private static Log log = LogFactory.getLog( AddressForm.class );

    public static final IMessages i18n = Messages.forPrefix( "AddressForm" );

    private IFormEditorPageSite     site;
    
    private Address                 address;

    
    public AddressForm( Address address ) {
        this.address = address;
    }


    public void createFormContent( final IFormEditorPageSite _site ) {
        this.site = _site;
        Composite body = site.getPageBody();
        //body.setLayout( ColumnLayoutFactory.defaults().spacing( 10 ).margins( 20, 20 ).create() );

        // fields
        Property<String> prop = address.street();
        new FormFieldBuilder( body, new PropertyAdapter( prop ) ).setLabel( i18n.get( prop.name() ) )
                .setValidator( new NotNullValidator() ).create();
        
        prop = address.number();
        new FormFieldBuilder( body, new PropertyAdapter( prop ) ).setLabel( i18n.get( prop.name() ) )
                .setValidator( new NotNullValidator() ).create();

        prop = address.postalCode();
        new FormFieldBuilder( body, new PropertyAdapter( prop ) ).setLabel( i18n.get( prop.name() ) )
                .setValidator( new NotNullValidator() ).create();
        
        prop = address.city();
        new FormFieldBuilder( body, new PropertyAdapter( prop ) ).setLabel( i18n.get( prop.name() ) )
                .setValidator( new NotNullValidator() ).create();
        
//        prop = user.firstname();
//        new FormFieldBuilder( body, new PropertyAdapter( prop ) ).setLabel( i18n.get( prop.name() ) ).create();
    }

}
