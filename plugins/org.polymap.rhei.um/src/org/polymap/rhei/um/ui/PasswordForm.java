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
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.field.NotEmptyValidator;
import org.polymap.rhei.field.PlainValuePropertyAdapter;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.StringFormField.Style;
import org.polymap.rhei.form.DefaultFormPage;
import org.polymap.rhei.form.IFormPageSite;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.internal.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PasswordForm
        extends DefaultFormPage {

    private static Log log = LogFactory.getLog( PasswordForm.class );

    public static final IMessages i18n = Messages.forPrefix( "PasswordForm" );

    private IPanelSite              panelSite;

    private User                    user;
    
            
    public PasswordForm( IPanelSite panelSite, User user ) {
        this.panelSite = panelSite;
        this.user = user;
    }


    @Override
    public void createFormContents( IFormPageSite site ) {
        Composite body = site.getPageBody();
        if (body.getLayout() == null) {
            body.setLayout( ColumnLayoutFactory.defaults().spacing( 10 ).margins( 20, 20 ).create() );
        }
        
        site.newFormField( new PlainValuePropertyAdapter( "pwd1", "" ) )
                .label.put( i18n.get( "password1" ) )
                .field.put( new StringFormField( Style.PASSWORD ) )
//                .setValidator( new PasswordValidator() )
                .validator.put( new NotEmptyValidator() )
                .create();

        site.newFormField( new PlainValuePropertyAdapter( "pwd2", "" ) )
                .label.put( i18n.get( "password2" ) )
                .field.put( new StringFormField( Style.PASSWORD ) )
//                .setValidator( new PasswordValidator() )
                .validator.put( new NotEmptyValidator() )
                .create();
    }

}
