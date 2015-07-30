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
package org.polymap.rhei.batik.contribution;

import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultContributionFactory
        implements IContributionFactory {

    private static Log log = LogFactory.getLog( DefaultContributionFactory.class );

    private Predicate<IContributionSite>    filter;

    
    public DefaultContributionFactory( Predicate<IContributionSite> filter ) {
        assert filter != null;
        this.filter = filter;
    }

    
    protected void doFillFAB( IContributionSite site ) {
    }
    

    @Override
    public void fillToolbar( IContributionSite site, Object toolbar ) {
        site.getContext().propagate( this );
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public final void fillFab( IContributionSite site ) {
        site.getContext().propagate( filter );
        if (filter.test( site )) {
            site.getContext().propagate( this );
            doFillFAB( site );
        }
    }
    
}
