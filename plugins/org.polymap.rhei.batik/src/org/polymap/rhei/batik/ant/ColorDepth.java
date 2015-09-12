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
package org.polymap.rhei.batik.ant;

public enum ColorDepth {
    B1(1), B2(2), B4(4), B8(8);

    private final int bit;


    ColorDepth( int bit ) {
        this.bit = bit;
    }


    int getBit() {
        return bit;
    }


    /**
     * @param depth
     * @return
     */
    public static ColorDepth getAsDepth( int depth ) {
        for (ColorDepth value : values()) {
            if (value.getBit() == depth) {
                return value;
            }
        }
        throw new IllegalArgumentException( depth + " is an unsupported color depth." );
    }
}