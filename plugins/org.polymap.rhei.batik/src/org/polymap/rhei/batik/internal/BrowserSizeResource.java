/* 
 * polymap.org
 * Copyright (C) 2013, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.batik.internal;

import org.eclipse.rap.ui.resources.IResource;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BrowserSizeResource
        implements IResource {

    public final String getLocation() {
        return "org/polymap/rhei/batik/internal/browsersize.js";
    }

    public final String getCharset() {
        return "UTF8";
    }

    public final ClassLoader getLoader() {
        return this.getClass().getClassLoader();
    }

    public final boolean isJSLibrary() {
        return true;
    }

    public final boolean isExternal() {
        return false;
    }

}
