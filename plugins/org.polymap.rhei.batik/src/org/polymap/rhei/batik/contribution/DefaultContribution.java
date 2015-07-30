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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;

import org.polymap.core.ui.StatusDispatcher;

import org.polymap.rhei.batik.toolkit.md.MdToolkit;

/**
 * Provides default behaviour for:
 * <ul>
 * <li>filter site to contribute to (see {@link ContributionSiteFilters})</li>
 * <li>create UI elements</li>
 * <li>context propagation, execution and error handling</li>
 * <li>update of UI elements</li>
 * </ul>
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultContribution
        implements IContributionFactory {

    private static Log log = LogFactory.getLog( DefaultContribution.class );

    /**
     * Possible places to contribute to.
     */
    public enum Place {
        FAB, Toolbar
    }
    
    private Predicate<IContributionSite>    filter;
    
    private Set<Place>                      places;

    /**
     * 
     * 
     * @param filter Filter the site to contribute to (see {@link ContributionSiteFilters}).
     * @param places The place(s) of the UI to contribute to.
     */
    public DefaultContribution( Predicate<IContributionSite> filter, Place... places ) {
        assert filter != null;
        this.filter = filter;
        this.places = new HashSet( Arrays.asList( places ) );
    }

    
    protected String getLabel( IContributionSite site ) {
        return null;
    }
    
    
    protected String getTooltip( IContributionSite site ) {
        return null;
    }
    
    
    protected Image getIcon(  IContributionSite site ) {
        return null;
    }
    
    
    protected abstract void execute( IContributionSite site ) throws Exception;

    
    protected void handleError( IContributionSite site, Throwable e ) {
        StatusDispatcher.handleError( "Cannot successfully complete '" + getLabel( site ) + "'", e );
    }

    
    /**
     * Updates previously created/filled UI elements after label, icon, tooltip, etc.
     * has changed.
     */
    protected void update() {
        throw new RuntimeException( "not yet implemented." );        
    }
    
    
    @Override
    public void fillToolbar( IContributionSite site, Object toolbar ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public final void fillFab( IContributionSite site ) {
        if (places.contains( Place.FAB )) {
            site.getContext().propagate( filter );
            if (filter.test( site )) {
                site.getContext().propagate( this );

                Button fab = ((MdToolkit)site.toolkit()).createFab();
                fab.setToolTipText( getTooltip( site ) );
                fab.addSelectionListener( new SelectionAdapter() {
                    @Override
                    public void widgetSelected( SelectionEvent ev ) {
                        try {
                            execute( site );
                        }
                        catch (Exception e) {
                            handleError( site, e );
                        }
                    }
                });
            }
        }
    }
    
}
