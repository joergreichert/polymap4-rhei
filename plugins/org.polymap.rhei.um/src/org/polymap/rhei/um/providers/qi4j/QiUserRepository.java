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
package org.polymap.rhei.um.providers.qi4j;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;

import org.polymap.core.model.CompletionException;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.runtime.entity.ConcurrentModificationException;

import org.polymap.rhei.um.Entity;
import org.polymap.rhei.um.Group;
import org.polymap.rhei.um.Groupable;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.providers.UserRepositorySPI;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class QiUserRepository
        extends QiModule
        implements UserRepositorySPI {

    private static Log log = LogFactory.getLog( QiUserRepository.class );

    /**
     * Get or create the repository for the current user session.
     */
    public static final QiUserRepository instance() {
        return Qi4jPlugin.Session.instance().module( QiUserRepository.class );
    }


    // instance *******************************************

    protected QiUserRepository( QiModuleAssembler assembler ) {
        super( assembler );
    }


    @Override
    public void commit() {
        try {
            commitChanges();
        }
        catch (CompletionException e) {
            throw new RuntimeException( e );
        }
        catch (ConcurrentModificationException e) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public void revert() {
        revertChanges();
    }


    @Override
    public <T extends Entity> List<T> find( Class<T> type, Predicate<T> filter ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public List<Group> groupsOf( Groupable groupable ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public User newUser() {
        return newEntity( QiUser.class, null );
    }
    
}
