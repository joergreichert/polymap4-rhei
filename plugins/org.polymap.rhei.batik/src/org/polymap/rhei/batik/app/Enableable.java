/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.batik.app;

import org.eclipse.swt.widgets.Button;

import org.eclipse.jface.action.IAction;

/**
 * Common interface for several "enableable" types.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class Enableable<T> {

    public static Enableable<IAction> of( final IAction action ) {
        return new Enableable<IAction>() {
            @Override
            public void setEnabled( boolean enabled ) {
                action.setEnabled( enabled );
            }
        };
    }
    
    public static Enableable<Button> of( final Button btn ) {
        return new Enableable<Button>() {
            @Override
            public void setEnabled( boolean enabled ) {
                btn.setEnabled( enabled );
            }
        };
    }
    
    public abstract void setEnabled( boolean enabled );
    
}
