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
package org.polymap.rhei.um.providers;

import java.util.List;

import com.google.common.base.Predicate;

import org.polymap.rhei.um.Entity;
import org.polymap.rhei.um.Groupable;
import org.polymap.rhei.um.User;

/**
 * The interface to a backend service provider of user management information.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface UserRepositorySPI {

    public <T extends Entity> Iterable<T> find( Class<T> type, Predicate<T> filter );

    public List<String> groupsOf( Groupable groupable );

    public boolean asignGroup( Groupable user, String group );

    public boolean resignGroup( Groupable user, String group );

    public User newUser();

    /**
     * 
     *
     * @param username
     * @return The user, or null if no such username exists.
     * @throws IllegalStateException If more than one user exosts for the given username.
     */
    public User findUser( String username );

    public void commit();

    public void revert();

}
