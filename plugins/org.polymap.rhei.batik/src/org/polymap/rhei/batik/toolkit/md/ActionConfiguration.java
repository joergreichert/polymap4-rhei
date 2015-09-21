/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.rhei.batik.toolkit.md;

import java.util.Observable;

import org.eclipse.swt.graphics.Image;
import org.polymap.core.runtime.Callback;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;

/**
 * Configuration of an action to be executed from a toolbar button resp. from a menu
 * entry.
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 */
public class ActionConfiguration
        extends Observable {

    /**
     * name, also used as unique identification of that ActionConfiguration, thus
     * mandatory
     */
    @org.polymap.core.runtime.config.Mandatory
    public Config2<ActionConfiguration,String>  name;

    /**
     * If true, the name should be used as menu or button label. Please note that you
     * should set this property to true, if you don't provide an image, as otherwise
     * the button or menu has no labeling at all.
     */
    @org.polymap.core.runtime.config.Mandatory
    public Config2<ActionConfiguration,Boolean> showName;

    /**
     * Value to control order of button appearance in toolbar. The toolbar is only
     * allowed to show 3 buttons. So if there are more than 3 actions, thus actions
     * will be created as menu items of the third button. So for example when there
     * are 5 actions given, there are 2 buttons for the 2 actions with highest
     * priority, and a third button to offer the other 3 actions as menu items. If
     * two action configurations have the same priority, the action will be ordered
     * alphanumerically by their names.
     */
    @org.polymap.core.runtime.config.Mandatory
    public Config2<ActionConfiguration,Integer> priority;

    /** the image to be used as icon for the menu entry of button */
    public Config2<ActionConfiguration,Image>   image;

    /**
     * the actual action code to perform
     */
    /*
     * here Config2 isn't used as we need the setter method to notify the observers
     * (e.g. enabling the button, if the actuon
     */
    private Callback<?>                         callback = null;

    public Config2<ActionConfiguration,String>  tooltipText;


    public ActionConfiguration() {
        ConfigurationFactory.inject( this );
    }


    public Callback<?> getCallback() {
        return callback;
    }


    public void setCallback( Callback<?> callback ) {
        if (callback != this.callback) {
            this.callback = callback;
            setChanged();
            notifyObservers( callback );
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {
        if (!(obj instanceof ActionConfiguration))
            return false;
        return this.toString().equals( obj.toString() );
    }


    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name.orElse( "<unknown>" );
    }
}
