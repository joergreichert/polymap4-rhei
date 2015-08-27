/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.um.auth;

import org.polymap.core.security.SecurityContext;
import org.polymap.core.security.StandardConfiguration;

import org.polymap.rhei.um.ui.LoginPanel;

/**
 * Always use {@link SecurityContext#SERVICES_CONFIG_NAME} config name and let
 * {@link LoginPanel} display out own login form.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UmSecurityConfiguration
        extends StandardConfiguration {

    @Override
    public String getConfigName() {
        return SecurityContext.SERVICES_CONFIG_NAME;
    }
    
}
