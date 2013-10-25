/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.um.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.IMessages;

import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.auth.PasswordEncryptor;
import org.polymap.rhei.um.email.EmailService;
import org.polymap.rhei.um.internal.Messages;

/**
 * Resets the password for the given user and sends an email to the user.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NewPasswordOperation
        extends AbstractOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( NewPasswordOperation.class );

    public static final IMessages i18n = Messages.forPrefix( "NewPasswordOperation" );

    private User            user;

    
    public NewPasswordOperation( User user ) {
        super( i18n.get( "title" ) );
        this.user = user;
    }

    
    public User getUser() {
        return user;
    }


    @Override
    public IStatus execute( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        try {
            // password hash
            PasswordEncryptor encryptor = PasswordEncryptor.instance();
            String password = encryptor.createPassword( 8 );
            String hash = encryptor.encryptPassword( password );
            user.passwordHash().set( hash );
            log.debug( "Neues Passwort: " + password + " -> " + hash );

            // username <= email
            String username = user.email().get();
            assert username != null && username.length() > 0;
            user.username().set( username );
            log.info( "username: " + user.username().get() );

            // commit
            UserRepository.instance().commitChanges();
        
            // XXX email
            String salu = user.salutation().get() != null ? user.salutation().get() : "";
            String header = (salu.equalsIgnoreCase( "Herr" ) ? "r Herr " : " ") + salu + " " + user.name().get();
            Email email = new SimpleEmail()
                    .addTo( username )
                    .setSubject( i18n.get( "emailSubject") )
                    .setMsg( i18n.get( "email", header, username, password ) );

            EmailService.instance().send( email );

            return Status.OK_STATUS;
        }
        catch (EmailException e) {
            throw new ExecutionException( i18n.get( "errorMsg", e.getLocalizedMessage() ), e );
        }        
    }

    
    @Override
    public boolean canUndo() {
        return false;
    }

    @Override
    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public IStatus redo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }

}
