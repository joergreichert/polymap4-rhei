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
package org.polymap.rhei.batik.engine.svg;

import org.polymap.rhei.batik.engine.svg.Svg2Png.Bounds;

public enum Scale {

    P8(8f, 8f), P16(16f, 16f), P20(20f, 20f), P24(24f, 24f), P32(32f, 32f), P48(48f, 48f), P64(64f, 64f), P128(128f, 128f);

    private final float width;

    private final float height;


    Scale( float width, float height ) {
        this.width = width;
        this.height = height;
    }


    public float getWidth() {
        return width;
    }


    public float getHeight() {
        return height;
    }


    public static Scale getAsScale( int number ) {
        for (Scale value : values()) {
            if (value.getWidth() == number && value.getHeight() == number) {
                return value;
            }
        }
        throw new IllegalArgumentException( number + " is an unsupported pixel scale." );
    }


    public float getWidth( Bounds bounds ) {
        if (bounds.getWidth() >= getWidth()) {
            if (bounds.getHeight() < bounds.getWidth()) {
                return getWidth();
            }
            else {
                return bounds.getWidth() * getWidth() / bounds.getHeight();
            }
        }
        else {
            if (bounds.getHeight() < bounds.getWidth()) {
                return getWidth();
            }
            else {
                return getHeight() * bounds.getWidth() / bounds.getHeight();
            }
        }
    }


    public float getHeight( Bounds bounds ) {
        if (bounds.getHeight() >= getHeight()) {
            if (bounds.getWidth() < bounds.getHeight()) {
                return getHeight();
            }
            else {
                return bounds.getHeight() * getHeight() / bounds.getWidth();
            }
        }
        else {
            if (bounds.getHeight() > bounds.getWidth()) {
                return getHeight();
            }
            else if (bounds.getHeight() >= bounds.getWidth()) {
                return getHeight();
            }
            else {
                return getWidth() * bounds.getHeight() / bounds.getWidth();
            }
        }
    }
}