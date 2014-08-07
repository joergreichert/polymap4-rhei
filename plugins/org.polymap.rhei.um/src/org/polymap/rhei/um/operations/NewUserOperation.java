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

import java.text.MessageFormat;

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
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.auth.PasswordEncryptor;
import org.polymap.rhei.um.email.EmailService;
import org.polymap.rhei.um.internal.Messages;

/**
 * Creates the given user in the
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NewUserOperation
        extends AbstractOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( NewUserOperation.class );

    public static final IMessages i18n = Messages.forPrefix( "NewUserOperation" );

    private User            user;

    private String          emailContent = i18n.get( "email" );
    
    private String          emailSubject = i18n.get( "emailSubject" );
    
    
    public NewUserOperation( User user ) {
        super( i18n.get( "title" ) );
        this.user = user;
    }

    public String getEmailContent() {
        return emailContent;
    }
    
    public void setEmailContent( String emailContent ) {
        this.emailContent = emailContent;
    }
    
    public String getEmailSubject() {
        return emailSubject;
    }
    
    public void setEmailSubject( String emailSubject ) {
        this.emailSubject = emailSubject;
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

            // username <= email
            String username = user.email().get();
            assert username != null && username.length() > 0;
            user.username().set( username );
            log.info( "username: " + user.username().get() );

            // commit
            UserRepository.instance().commitChanges();
        
            String salu = user.salutation().get() != null ? user.salutation().get() : "";
            String header = (salu.equalsIgnoreCase( "Herr" ) ? "r " : " ") + salu + " " + user.name().get();
            Email email = new SimpleEmail();
            email.setCharset( "ISO-8859-1" );
            email.addTo( username )
                    .setSubject( emailSubject )
                    .setMsg( new MessageFormat( emailContent, Polymap.getSessionLocale() )
                            .format( new Object[] {header, username, password} ) );
//                    .setMsg( i18n.get( "email", header, username, password ) );
            
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
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public IStatus redo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

}
