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
package org.polymap.rhei.batik;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultInt;
import org.polymap.core.runtime.config.DefaultPropertyConcern;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.config.PropertyInfo;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.IPanelSite.PanelStatus;
import org.polymap.rhei.batik.PanelChangeEvent.EventType;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.LayoutSupplier;

/**
 * Provides the interface between {@link IPanel} client code and the Batik
 * framework. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class PanelSite
        extends Configurable {

    private static Log log = LogFactory.getLog( PanelSite.class );
    
    @Immutable
    public Config2<PanelSite,Integer>       stackPriority;
    
    /**
     * Changes the status of the panel. {@link Status#OK_STATUS} signals that the
     * panel has valid state. If status is not valid then the given message is
     * displayed.
     * <p/>
     * Use status severity as follows:
     * <ul>
     * <li>{@link Status#OK_STATUS} : Everything is ok. No message.</li>
     * <li>{@link IStatus#OK} : An action has been complete successfully. Message gets displayed.</li>
     * <li>{@link IStatus#INFO} : ...</li>
     * <li>{@link IStatus#WARNING} : The user's attention is needed.</li>
     * <li>{@link IStatus#ERROR} : An error/exception occured. An</li>
     * </ul>
     */
    @Mandatory
    @Concern( FireEvent.class )
    @FireEventType( EventType.STATUS )
    public Config2<PanelSite,IStatus>       status;
    
    /**
     * The title of the page. Null specifies that the panel does not show up in
     * the panel navigator bar.
     */
    @Concern( FireEvent.class )
    @FireEventType( EventType.TITLE )
    public Config2<PanelSite,String>        title;

    /**
     * The title and tooltip of the page.
     */
    @Concern( FireEvent.class )
    @FireEventType( EventType.TITLE )
    public Config2<PanelSite,String>        tooltip;
    
    @Concern( FireEvent.class )
    @FireEventType( EventType.TITLE )
    public Config2<PanelSite,Image>         icon;

    @Mandatory
    @DefaultInt( SWT.DEFAULT )
    public Config2<PanelSite,Integer>       preferredWidth;

    @Mandatory
    @DefaultInt( SWT.DEFAULT )
    public Config2<PanelSite,Integer>       maxWidth;

    @Mandatory
    @DefaultInt( SWT.DEFAULT )
    public Config2<PanelSite,Integer>       minWidth;


    /**
     * The entiry path of the panel including the name of the panel as last segment.
     */
    public abstract PanelPath path();
    
    public abstract PanelStatus panelStatus();
    
    public abstract Memento memento();
        
    public abstract IPanelToolkit toolkit();

    public abstract void layout( boolean changed );

    /**
     * Layout preferences should be used by client code in order to fit panel layout
     * into the layout of the application.
     */
    public abstract LayoutSupplier layoutPreferences();
    
    public static class FireEvent
            extends DefaultPropertyConcern {

        /**
         * This is called *before* the {@link Config2} property is set. However, there is no
         * race condition between event handler thread, that might access property value, and
         * the current thread, that sets the property value, because most {@link EventHandler}s
         * are done in display thread.
         */
        @Override
        public Object doSet( Object obj, Config prop, Object newValue ) {
            PropertyInfo info = prop.info();
            PanelSite site = info.getHostObject();
            
            FireEventType a = info.getAnnotation( FireEventType.class );
            assert a != null : "Missing @FireEventType annotation!";
            
            // XXX avoid race conditions; EventManager does not seem to always handle display events properly
            EventManager.instance().publish( new PanelChangeEvent( site, a.value(), newValue ) );
            return newValue;
        }
    }

}
