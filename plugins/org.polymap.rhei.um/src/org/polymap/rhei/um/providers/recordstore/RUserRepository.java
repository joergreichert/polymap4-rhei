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
package org.polymap.rhei.um.providers.recordstore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;

import org.polymap.core.runtime.recordstore.IRecordStore;

import org.polymap.rhei.um.Entity;
import org.polymap.rhei.um.Groupable;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.providers.UserRepositorySPI;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RUserRepository
        implements UserRepositorySPI {

    private static Log log = LogFactory.getLog( RUserRepository.class );

    private IRecordStore            store;
    
    private Map<String,Entity>      loaded = new HashMap();

    
    @Override
    public <T extends Entity> Iterable<T> find( Class<T> type, Predicate<T> filter ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public List<String> groupsOf( Groupable groupable ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
    
    @Override
    public boolean asignGroup( Groupable user, String group ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public boolean resignGroup( Groupable user, String group ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public User findUser( String username ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    protected void registerEntities( Iterable<Entity> candidates ) {
        for (Entity candidate : candidates) {
            
        }
    }

    @Override
    public User newUser() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void commit() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void revert() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
        
}
