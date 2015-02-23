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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.i18n.IMessages;

import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.auth.PasswordEncryptor;
import org.polymap.rhei.um.internal.Messages;

/**
 * Changes the password of the given user.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ChangePasswordOperation
        extends AbstractOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( ChangePasswordOperation.class );

    public static final IMessages i18n = Messages.forPrefix( "ChangePasswordOperation" );

    private User            user;
    
    private String          newPassword;

    
    public ChangePasswordOperation( User user, String newPassword ) {
        super( i18n.get( "title" ) );
        this.user = user;
        this.newPassword = newPassword;
    }

    
    public User getUser() {
        return user;
    }


    @Override
    public IStatus execute( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        // password hash
        PasswordEncryptor encryptor = PasswordEncryptor.instance();
        String hash = encryptor.encryptPassword( newPassword );
        user.passwordHash().set( hash );
        log.debug( "Neues Passwort: " + newPassword + " -> " + hash );

        // commit
        UserRepository.instance().commitChanges();
        return Status.OK_STATUS;
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
