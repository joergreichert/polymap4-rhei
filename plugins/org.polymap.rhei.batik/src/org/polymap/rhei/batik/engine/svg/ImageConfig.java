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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

import org.polymap.rhei.batik.engine.svg.Svg2Png.COLOR_TYPE;

public class ImageConfig {

    private String                   name                = "default";

    private String                   depth               = null;

    private String                   adjHue              = "0";

    private String                   adjSaturation       = "1";

    private String                   adjBrightness       = "0";

    private String                   colorType           = "argb";

    private String                   rgb                 = "FFFFF";

    private boolean                  relative            = false;

    private boolean                  invert              = false;

    private List<ImageConfig.ReplaceConfig>      replaceConfigs      = new ArrayList<ImageConfig.ReplaceConfig>();

    private List<ImageConfig.TransparenceConfig> transparenceConfigs = new ArrayList<ImageConfig.TransparenceConfig>();


    public static class TransparenceConfig {

        String value;


        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }


        /**
         * @param value the value to set
         */
        public void setValue( String value ) {
            this.value = value;
        }
    }


    public static class ReplaceConfig {

        String sourceRGB;

        String targetRGB;


        /**
         * @return the sourceRGB
         */
        public String getSourceRGB() {
            return sourceRGB;
        }


        /**
         * @param sourceRGB the sourceRGB to set
         */
        public void setSourceRGB( String sourceRGB ) {
            this.sourceRGB = sourceRGB;
        }


        /**
         * @return the targetRGB
         */
        public String getTargetRGB() {
            return targetRGB;
        }


        /**
         * @param targetRGB the targetRGB to set
         */
        public void setTargetRGB( String targetRGB ) {
            this.targetRGB = targetRGB;
        }
    }


    public void addReplaceConfig( ImageConfig.ReplaceConfig replaceConfig ) {
        replaceConfigs.add( replaceConfig );
    }


    public List<ImageConfig.TransparenceConfig> getTransparenceConfigs() {
        return transparenceConfigs;
    }


    public void addTransparenceConfig( ImageConfig.TransparenceConfig transparenceConfig ) {
        transparenceConfigs.add( transparenceConfig );
    }


    public List<ImageConfig.ReplaceConfig> getReplaceConfigs() {
        return replaceConfigs;
    }


    /**
     * @return
     */
    RGB getTypedRGB() {
        String rgbStr = getRgb();
        return Svg2PngTask.getTypedRGB( rgbStr );
    }


    /**
     * @return
     */
    COLOR_TYPE getTypedColorType() {
        String colorTypeStr = getColorType();
        if ("monochrom".equalsIgnoreCase( colorTypeStr )) {
            return COLOR_TYPE.MONOCHROM;
        }
        else if ("gray".equalsIgnoreCase( colorTypeStr )) {
            return COLOR_TYPE.GRAY;
        }
        else if ("rgb".equalsIgnoreCase( colorTypeStr )) {
            return COLOR_TYPE.RGB;
        }
        else {
            return COLOR_TYPE.ARGB;
        }
    }


    /**
     * @return
     */
    float getTypedAdjHue() {
        return Float.valueOf( getAdjHue() );
    }


    /**
     * @return
     */
    float getTypedAdjSaturation() {
        return Float.valueOf( getAdjSaturation() );
    }


    /**
     * @return
     */
    float getTypedAdjBrightness() {
        return Float.valueOf( getAdjBrightness() );
    }


    /**
     * @return
     */
    ColorDepth getTypedDepth() {
        if (getDepth() == null) {
            return null;
        }
        else {
            return ColorDepth.getAsDepth( Integer.valueOf( getDepth() ) );
        }
    }


    /**
     * @return the depth
     */
    public String getDepth() {
        return depth;
    }


    /**
     * @param depth the depth to set
     */
    public void setDepth( String depth ) {
        this.depth = depth;
    }


    /**
     * @return the adjHue
     */
    public String getAdjHue() {
        return adjHue;
    }


    /**
     * @param adjHue the adjHue to set
     */
    public void setAdjHue( String adjHue ) {
        this.adjHue = adjHue;
    }


    /**
     * @return the adjSaturation
     */
    public String getAdjSaturation() {
        return adjSaturation;
    }


    /**
     * @param adjSaturation the adjSaturation to set
     */
    public void setAdjSaturation( String adjSaturation ) {
        this.adjSaturation = adjSaturation;
    }


    /**
     * @return the adjBrightness
     */
    public String getAdjBrightness() {
        return adjBrightness;
    }


    /**
     * @param adjBrightness the adjBrightness to set
     */
    public void setAdjBrightness( String adjBrightness ) {
        this.adjBrightness = adjBrightness;
    }


    /**
     * @return the colorType
     */
    public String getColorType() {
        return colorType;
    }


    /**
     * @param colorType the colorType to set
     */
    public void setColorType( String colorType ) {
        this.colorType = colorType;
    }


    /**
     * @return the rgb
     */
    public String getRgb() {
        return rgb;
    }


    /**
     * @param rgb the rgb to set
     */
    public void setRgb( String rgb ) {
        this.rgb = rgb;
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
     * 
     */
    public boolean isRelative() {
        return this.relative;
    }


    /**
     * @param relative the relative to set
     */
    public void setRelative( Boolean relative ) {
        this.relative = relative;
    }


    /**
     * @return the invert
     */
    public boolean isInvert() {
        return invert;
    }


    /**
     * @param invert the invert to set
     */
    public void setInvert( boolean invert ) {
        this.invert = invert;
    }
}