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
package org.polymap.rhei.batik.toolkit;

/**
 * The source of layout settings.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class LayoutSupplier {

    public abstract int getMarginLeft();

    public abstract int getMarginRight();

    public abstract int getMarginTop();
    
    public abstract int getMarginBottom();
    
    public abstract int getSpacing();
    
}
