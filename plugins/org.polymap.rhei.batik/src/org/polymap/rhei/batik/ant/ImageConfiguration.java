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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.polymap.rhei.batik.ant.Svg2Png.COLOR_TYPE;

public class ImageConfiguration {

    private String                       name;

    private ColorDepth                  depth;

    private Float                        adjHue;

    private Float                        adjSaturation;

    private Float                        adjBrightness;

    private COLOR_TYPE                   colorType;

    private Boolean                      relative                   = true;

    private Boolean                      invert                     = false;

    private org.eclipse.swt.graphics.RGB rgb;

    private List<ImageConfiguration.ReplaceConfiguration>   replaceConfigurations      = new ArrayList<ImageConfiguration.ReplaceConfiguration>();

    private List<RGB>                    transparenceConfigurations = new ArrayList<RGB>();


    public static class ReplaceConfiguration {

        private org.eclipse.swt.graphics.RGB from;

        private org.eclipse.swt.graphics.RGB to;


        /**
         * @param from
         * @param to
         */
        public ReplaceConfiguration( RGB from, RGB to ) {
            super();
            this.from = from;
            this.to = to;
        }


        /**
         * @return the from
         */
        public org.eclipse.swt.graphics.RGB getFrom() {
            return from;
        }


        /**
         * @param from the from to set
         */
        public void setFrom( org.eclipse.swt.graphics.RGB from ) {
            this.from = from;
        }


        /**
         * @return the to
         */
        public org.eclipse.swt.graphics.RGB getTo() {
            return to;
        }


        /**
         * @param to the to to set
         */
        public void setTo( org.eclipse.swt.graphics.RGB to ) {
            this.to = to;
        }
    }


    /**
     * @return the replaceConfigurations
     */
    public List<ImageConfiguration.ReplaceConfiguration> getReplaceConfigurations() {
        return replaceConfigurations;
    }


    /**
     * @return the transparenceConfigurations
     */
    public List<RGB> getTransparenceConfigurations() {
        return transparenceConfigurations;
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }


    /**
     * @param name the name to set
     */
    public void setName( String name ) {
        this.name = name;
    }


    /**
     * @return the depth
     */
    public ColorDepth getDepth() {
        return depth;
    }


    /**
     * @param depth the depth to set
     */
    public void setDepth( ColorDepth depth ) {
        this.depth = depth;
    }


    /**
     * @return the adjHue
     */
    public Float getAdjHue() {
        return adjHue;
    }


    /**
     * @param adjHue the adjHue to set
     */
    public void setAdjHue( Float adjHue ) {
        this.adjHue = adjHue;
    }


    /**
     * @return the adjSaturation
     */
    public Float getAdjSaturation() {
        return adjSaturation;
    }


    /**
     * @param adjSaturation the adjSaturation to set
     */
    public void setAdjSaturation( Float adjSaturation ) {
        this.adjSaturation = adjSaturation;
    }


    /**
     * @return the adjBrightness
     */
    public Float getAdjBrightness() {
        return adjBrightness;
    }


    /**
     * @param adjBrightness the adjBrightness to set
     */
    public void setAdjBrightness( Float adjBrightness ) {
        this.adjBrightness = adjBrightness;
    }


    /**
     * @return the colorType
     */
    public COLOR_TYPE getColorType() {
        return colorType;
    }


    /**
     * @return the rgb
     */
    public org.eclipse.swt.graphics.RGB getRgb() {
        return rgb;
    }


    /**
     * @param colorType the colorType to set
     */
    public void setColorType( COLOR_TYPE colorType ) {
        this.colorType = colorType;
    }


    /**
     * @return the relative
     */
    public Boolean isRelative() {
        return relative;
    }


    /**
     * @param relative the relative to set
     */
    public void setRelative( Boolean relative ) {
        this.relative = relative;
    }


    /**
     * @param typedRGB
     */
    public void setRGB( org.eclipse.swt.graphics.RGB rgb ) {
        this.rgb = rgb;
    }


    /**
     * @return the invert
     */
    public Boolean isInvert() {
        return invert;
    }


    /**
     * @param invert the invert to set
     */
    public void setInvert( Boolean invert ) {
        this.invert = invert;
    }
}