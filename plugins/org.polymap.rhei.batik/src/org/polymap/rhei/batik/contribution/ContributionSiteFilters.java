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

import org.polymap.rhei.batik.IPanel;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ContributionSiteFilters {

    private static Log log = LogFactory.getLog( ContributionSiteFilters.class );
    
    
    public static Predicate<IContributionSite> panelType( Class<? extends IPanel> type ) {
        return site -> site.panel().getClass().equals( type );
    }
    

//    public static Predicate<IContributionSite> contextPresent( Class<?> type ) {
//        return new Predicate<IContributionSite>() {
//            Con
//            @Override
//            public boolean test( IContributionSite t ) {
//                // XXX Auto-generated method stub
//                throw new RuntimeException( "not yet implemented." );
//            }
//            
//        };
//    }
    
}
