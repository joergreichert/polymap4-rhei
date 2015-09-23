/*
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.batik;

import java.util.function.Predicate;

import org.polymap.core.runtime.Predicates;

/**
 * Provides predicates to be used as filters in
 * {@link IAppContext#findPanels(Predicate)}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Panels {

    /**
     *
     */
    public static final Predicate<IPanel> all() {
        return Predicates.ALWAYS_TRUE;  //(IPanel panel) -> true;
    }

    /**
     *
     */
    public static final Predicate<IPanel> withPrefix( final PanelPath prefix ) {
        assert prefix != null;
        return (IPanel panel) -> prefix.isPrefixOf( panel.site().path() );
    }

    /**
     *
     */
    public static final Predicate<IPanel> is( final PanelPath path ) {
        assert path != null;
        return (IPanel input) -> path.equals( input.site().path() );
    }

}
