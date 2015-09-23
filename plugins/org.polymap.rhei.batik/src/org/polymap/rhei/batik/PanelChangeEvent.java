/* 
 * polymap.org
 * Copyright (C) 2013-2014, Falko Bräutigam. All rights reserved.
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

import java.util.Arrays;
import java.util.EventObject;
import org.polymap.rhei.batik.IPanelSite.PanelStatus;

/**
 * Signals changes of an {@link IPanel}
 * <p/>
 * <b>Beware!</b> Event handler methods should declare display=true in order to avoid
 * race condition. Or the event handler code must not rely on property value but uses
 * the new value from event.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PanelChangeEvent<V>
        extends EventObject {

    /** The types of {@link PanelChangeEvent}. */
    public enum EventType {
        /** The {@link IPanelSite#getPanelStatus()} {@link PanelStatus} has changed. */
        LIFECYCLE,
        /** The {@link IPanelSite#getStatus()} has changed. */
        STATUS,
        /** Titel, tooltip or icon has changed. */
        TITLE;

        public boolean isOnOf( EventType... types ) {
            return Arrays.asList( types ).contains( this );
        }
    }
    
    // instance *******************************************
    
    private EventType       type;
    
    private V               newValue;
    
    public PanelChangeEvent( PanelSite source, EventType type, V newValue ) {
        super( source );
        this.type = type;
        this.newValue = newValue;
    }

    @Override
    public PanelSite getSource() {
        return (PanelSite)super.getSource();
    }

    /**
     * The panel that caused the event.
     * 
     * @return Null if the panel is disposed.
     */
    public <T extends IPanel> T getPanel() {
        return (T)BatikApplication.instance().getContext().getPanel( getSource().path() );
    }

    public EventType getType() {
        return type;
    }
    
    public <T> T getNewValue() {
        return (T)newValue;
    }

    public String toString() {
        return getClass().getSimpleName() + "[source=" + source.getClass().getSimpleName() + ", type=" + type + "]";
    }

}
