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

import java.util.function.Function;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ImageDescription
        extends Configurable {

    public Config2<ImageDescription,String>                            localURL;

    public Config2<ImageDescription,String>                            remoteURL;

    public Config2<ImageDescription,Function<Integer,ImageDescriptor>> imageDescriptorSupplier;


    /**
     * Tries to create an image with minimal bounds. If it is possible, 
     * the image exists.
     * 
     * @return
     */
    public boolean exists() {
        Image image = imageDescriptorSupplier.get().apply( /*Scale.P8*/8 ).createImage();
        boolean exists = image != null;
        if(exists) {
            image.dispose();
        }
        return exists;
    }


    /**
     * As org.polymap.rhei.batik.engine.svg.Scale only supports certain sizes this
     * method returns the image that is at least as big as the desired size, e.g.
     * given size 20, an image descriptor with bounds 24x24 is returned.
     * 
     * @param the desired size for the image
     * @return an image descriptor that at least is as big as the desired size
     */
    public ImageDescriptor getImageForSize( int size ) {
        return imageDescriptorSupplier.get().apply( size );
    }
}
