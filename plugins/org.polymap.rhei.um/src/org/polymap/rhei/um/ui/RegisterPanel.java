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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.security.UserPrincipal;
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.um.UmPlugin;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.internal.Messages;
import org.polymap.rhei.um.operations.NewUserOperation;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RegisterPanel
        extends DefaultPanel
        implements IPanel, IFormFieldListener {

    private static Log log = LogFactory.getLog( RegisterPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "um", "register" );

    public static final IMessages       i18n = Messages.forPrefix( "RegisterPanel" );

    private Context<UserPrincipal> userPrincipal;

    private IPanelToolkit               tk;
    
    private Button                      okBtn;

    private PersonForm                  personForm;

    private User                        user;

    private Composite                   panelContainer;

    private IPanelSection               welcomeSection;

    private IPanelSection               personSection;
    

    @Override
    public void init() {
        tk = getSite().toolkit();
    }


    @Override
    public void dispose() {
        if (personForm != null) {
            personForm.removeFieldListener( this );
            personForm = null;
        }
    }


    public Composite getPanelContainer() {
        return panelContainer;
    }
    
    public IPanelSection getWelcomeSection() {
        return welcomeSection;
    }
    
    public IPanelSection getPersonSection() {
        return personSection;
    }


    @Override
    public void createContents( Composite parent ) {
        this.panelContainer = parent;
        getSite().setTitle( i18n.get( "title" ) );
        getSite().setIcon( BatikPlugin.instance().imageForName( "resources/icons/user.png" ) );

        // welcome section
        welcomeSection = tk.createPanelSection( parent, i18n.get( "sectionTitle" ) );
        welcomeSection.addConstraint( new PriorityConstraint( 10 ), new MinWidthConstraint( 450, 0 ) );
        tk.createFlowText( welcomeSection.getBody(), i18n.get( "welcomeText" ) );

        // person section
        personSection = tk.createPanelSection( parent, null );
        Composite body = personSection.getBody();
        body.setLayout( ColumnLayoutFactory.defaults().spacing( 5 ).margins( 0, 0 ).create() );

        user = UserRepository.instance().newUser();
        
        personForm = new PersonForm( getSite(), user );
        personForm.createContents( personSection );

        // btn
        okBtn = tk.createButton( personForm.getBody(), i18n.get( "okBtn" ), SWT.PUSH );
        okBtn.setEnabled( false );
        okBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    // create user
                    personForm.submit();

                    IUndoableOperation op = new NewUserOperation( user );
                    OperationSupport.instance().execute( op, true, false, new JobChangeAdapter() {

                        public void done( IJobChangeEvent ev2 ) {
                            if (ev2.getResult().isOK()) {
                                getSite().setStatus( new Status( IStatus.OK, UmPlugin.ID, i18n.get( "okText" ) ) );
                                getContext().closePanel( getSite().getPath() );
                            }
                            else {
                                getSite().setStatus( new Status( IStatus.ERROR, UmPlugin.ID, i18n.get( "errorText", ev2.getResult().getMessage() ) ) );                                
                            }
                        }
                    });                    
                }
                catch (Exception e) {
                    UserRepository.instance().revertChanges();
                    throw new RuntimeException( e );
                }
            }
        });
        
        personForm.addFieldListener( this );
    }

    
    private String      email, name;
    
    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE
                && !okBtn.isDisposed()) {
            okBtn.setEnabled( false );            

            if (ev.getFieldName().equals( "email" )) {
                email = ev.getNewValue();
            }
            if (ev.getFieldName().equals( "name" )) {
                name = ev.getNewValue();
            }
            
            if (personForm.isValid()
                    // XXX "short" login for test :)
                    || name != null && name.startsWith( "@" ) && email != null) {
                getSite().setStatus( Status.OK_STATUS );

                if (UserRepository.instance().findUser( email ) == null) {
                    okBtn.setEnabled( true );
                }
                else {
                    getSite().setStatus( new Status( IStatus.WARNING, UmPlugin.ID, "Der Nutzername existiert bereits: " + email ) );
                }
            }
        }
    }

}
