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
package org.polymap.rhei.field;

import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.polymap.core.runtime.Callback;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ImageDescription
        extends Configurable {
    private static Consumer<Triple<Integer,Display,Callback<Map<String, ImageDescriptor>>>> CALC;

    public Config2<ImageDescription,String>                              localURL;

    public Config2<ImageDescription,String>                              remoteURL;

    public Config2<ImageDescription,Consumer<Triple<Integer,Display,Callback<ImageDescriptor>>>> imageDescriptorCalculator;

    /**
     * As org.polymap.rhei.batik.engine.svg.Scale only supports certain sizes this
     * method returns the image that is at least as big as the desired size, e.g.
     * given size 20, an image descriptor with bounds 24x24 is returned.
     * 
     * @param the desired size for the image
     */
    public void createImageForSize( int size, Display display, Callback<ImageDescriptor> callback ) {
        imageDescriptorCalculator.get().accept( Triple.of( size, display, callback ) );
    }   

    public static void setImageDescriptorsCalculator(
            Consumer<Triple<Integer,Display,Callback<Map<String, ImageDescriptor>>>> calc ) {
        CALC = calc;
    }

    public static void createImagesForSize( int size, Display display, Callback<Map<String, ImageDescriptor>> callback ) {
        CALC.accept( Triple.of( size, display, callback ) );
    }
}
