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
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.app.FormContainer;
import org.polymap.rhei.field.EMailAddressValidator;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.um.Address;
import org.polymap.rhei.um.Person;
import org.polymap.rhei.um.Property;
import org.polymap.rhei.um.internal.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PersonForm
        extends FormContainer { 

    private static Log log = LogFactory.getLog( PersonForm.class );

    public static final IMessages i18n = Messages.forPrefix( "PersonForm" );

    private Person                  person;

    private IPanelSite              panelSite;
    
            
    public PersonForm( IPanelSite panelSite, Person person ) {
        this.panelSite = panelSite;
        this.person = person;
    }


    @Override
    public void createFormContent( IFormEditorPageSite site ) {
        Composite body = site.getPageBody();
        if (body.getLayout() == null) {
            body.setLayout( ColumnLayoutFactory.defaults().spacing( 10 ).margins( 20, 20 ).create() );
        }

        // fields
        Property<String> prop = person.name();
        new FormFieldBuilder( body, new PropertyAdapter( prop ) ).setLabel( i18n.get( prop.name() ) )
                .setValidator( new NotNullValidator() ).create().setFocus();
        
        prop = person.firstname();
        new FormFieldBuilder( body, new PropertyAdapter( prop ) ).setLabel( i18n.get( prop.name() ) ).create();
        
        prop = person.email();
        new FormFieldBuilder( body, new PropertyAdapter( prop ) )
                .setLabel( i18n.get( prop.name() ) )
                .setToolTipText( i18n.get( prop.name()+"Tip" ) )
                .setValidator( new EMailAddressValidator() )
                .create();

        prop = person.phone();
        new FormFieldBuilder( body, new PropertyAdapter( prop ) ).setLabel( i18n.get( prop.name() ) ).create();
        
        prop = person.mobilePhone();
        new FormFieldBuilder( body, new PropertyAdapter( prop ) ).setLabel( i18n.get( prop.name() ) ).create();
        
        prop = person.fax();
        new FormFieldBuilder( body, new PropertyAdapter( prop ) ).setLabel( i18n.get( prop.name() ) ).create();
        
        
        // address
        site.getToolkit().createLabel( body, null, SWT.SEPARATOR | SWT.HORIZONTAL );
        Address address = person.address().get();
        new AddressForm( panelSite, address ).createContents( this );
    }

}
