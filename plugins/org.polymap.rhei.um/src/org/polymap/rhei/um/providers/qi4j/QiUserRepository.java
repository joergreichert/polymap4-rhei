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

import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.grammar.BooleanExpression;
import static org.qi4j.api.query.QueryExpressions.*;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.polymap.core.model.CompletionException;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.runtime.entity.ConcurrentModificationException;

import org.polymap.rhei.um.Entity;
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
    public <T extends Entity> Iterable<T> find( Class<T> type, Predicate<T> filter ) {
        if (type.equals( User.class )) {
            return (Iterable<T>)findEntities( QiUser.class, null, 0, Integer.MAX_VALUE );
        }
        else {
            throw new RuntimeException( "unhandled type: " + type );
        }

    }


    @Override
    public List<String> groupsOf( Groupable groupable ) {
        return ImmutableList.copyOf( ((QiGroupable)groupable)._groups().get() );
    }


    @Override
    public boolean asignGroup( Groupable user, String group ) {
        Collection<String> groups = ((QiGroupable)user)._groups().get();
        if (!Iterables.contains( groups, group )) {
            groups.add( group );
            ((QiGroupable)user)._groups().set( groups );
            return true;
        }
        return false;
    }


    @Override
    public boolean resignGroup( Groupable user, String group ) {
        Collection<String> groups = ((QiGroupable)user)._groups().get();
        if (Iterables.contains( groups, group )) {
            groups.remove( group );
            ((QiGroupable)user)._groups().set( groups );
            return true;
        }
        return false;
    }


    @Override
    public User newUser() {
        return newEntity( QiUser.class, null );
    }


    @Override
    public void deleteUser( User user ) {
        removeEntity( (QiUser)user );
    }


    @Override
    public User findUser( String username ) {
        BooleanExpression expr = eq( templateFor( QiUser.class )._username(), username );
        Query<QiUser> query = findEntities( QiUser.class, expr, 0, 2 );
        if (query.count() > 1) {
            throw new IllegalStateException( "More than one user for username: " + username );
        }
        return query.find();
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
    
    
}
