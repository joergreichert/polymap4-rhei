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

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.eclipse.swt.graphics.RGB;

import org.polymap.rhei.batik.ant.Svg2Png.COLOR_DEPTH;
import org.polymap.rhei.batik.ant.Svg2Png.COLOR_TYPE;
import org.polymap.rhei.batik.ant.Svg2Png.ImageConfiguration;
import org.polymap.rhei.batik.ant.Svg2Png.SCALE;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class Svg2PngTask
        extends Task {

    private Svg2Png           svg2Png      = new Svg2Png();

    private String            pngPath;

    private String            svgPath;

    private String            scale        = "16";

    private List<ImageConfig> imageConfigs = new ArrayList<ImageConfig>();


    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {
        try {
            List<ImageConfiguration> imageConfigurations = imageConfigs.stream().map( config -> {
                ImageConfiguration imageConfig = new ImageConfiguration();
                imageConfig.setName( config.getName() );
                imageConfig.setDepth( config.getTypedDepth() );
                imageConfig.setAdjHue( config.getTypedAdjHue() );
                imageConfig.setAdjSaturation( config.getTypedAdjSaturation() );
                imageConfig.setAdjBrightness( config.getTypedAdjBrightness() );
                imageConfig.setColorType( config.getTypedColorType() );
                imageConfig.setRGB( config.getTypedRGB() );
                imageConfig.setRelative( config.isRelative() );
                return imageConfig;
            } ).collect( Collectors.toList() );

            Function<String,SCALE> getScaleOperation = ( String scaleStr ) -> SCALE.getAsScale( Integer
                    .valueOf( scaleStr.trim() ) );
            List<SCALE> scales = null;
            if (getScale().contains( "," )) {
                scales = Arrays.asList( getScale().split( "," ) ).stream()
                        .map( scaleStr -> getScaleOperation.apply( scaleStr ) ).collect( Collectors.toList() );
            }
            else {
                scales = Collections.singletonList( getScaleOperation.apply( getScale() ) );
            }
            File svgInput = getSVGInput();
            List<File> files = new ArrayList<File>();
            if (svgInput.isDirectory()) {
                collectFiles( svgInput, files );
            }
            else {
                files.add( getSVGInput() );
            }
            svg2Png.transcode( getPngPath(), files, scales, imageConfigurations );
        }
        catch (TranscoderException | IOException e) {
            e.printStackTrace();
            throw new BuildException( e.getMessage(), e );
        }
    }


    private void collectFiles( File folder, List<File> files ) {
        Arrays.asList( folder.listFiles() ).stream().forEach( f -> {
            if (f.isDirectory()) {
                collectFiles( f, files );
            }
            else {
                if ("svg".equalsIgnoreCase( FilenameUtils.getExtension( f.getName() ) )) {
                    files.add( f );
                }
            }
        } );
    }


    public void addImageConfig( ImageConfig imageConfig ) {
        imageConfigs.add( imageConfig );
    }


    public static class ImageConfig {

        private String name          = "default";

        private String depth         = null;

        private String adjHue        = "0";

        private String adjSaturation = "1";

        private String adjBrightness = "0";

        private String colorType     = "rgb";

        private String rgb           = "FFFFF";
        
        private boolean relative = false;


        /**
         * @return
         */
        private RGB getTypedRGB() {
            String rgbStr = getRgb();
            if (rgbStr == null) {
                return null;
            }
            if (rgbStr.startsWith( "#" )) {
                rgbStr = rgbStr.substring( 1 );
            }
            if (!rgbStr.startsWith( "0x" )) {
                rgbStr = "0x" + rgbStr;
            }
            Color color = Color.decode( rgbStr );
            return new RGB( color.getRed(), color.getGreen(), color.getBlue() );
        }


        /**
         * @return
         */
        private COLOR_TYPE getTypedColorType() {
            String colorTypeStr = getColorType();
            COLOR_TYPE colorType = null;
            if ("monochrom".equalsIgnoreCase( colorTypeStr )) {
                colorType = COLOR_TYPE.MONOCHROM;
            }
            else if ("gray".equalsIgnoreCase( colorTypeStr )) {
                colorType = COLOR_TYPE.GRAY;
            }
            else if ("rgb".equalsIgnoreCase( colorTypeStr )) {
                colorType = COLOR_TYPE.RGB;
            }
            else {
                colorType = COLOR_TYPE.RGB;
            }
            return colorType;
        }


        /**
         * @return
         */
        private float getTypedAdjHue() {
            float adjHue = Float.valueOf( getAdjHue() );
            return adjHue;
        }


        /**
         * @return
         */
        private float getTypedAdjSaturation() {
            float adjSaturation = Float.valueOf( getAdjSaturation() );
            return adjSaturation;
        }


        /**
         * @return
         */
        private float getTypedAdjBrightness() {
            float adjBrightness = Float.valueOf( getAdjBrightness() );
            return adjBrightness;
        }


        /**
         * @return
         */
        private COLOR_DEPTH getTypedDepth() {
            if (getDepth() == null) {
                return null;
            }
            else {
                int depth = Integer.valueOf( getDepth() );
                return COLOR_DEPTH.getAsDepth( depth );
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
         * @return
         */
        public boolean isRelative( ) {
            return this.relative;
        }

        /**
         * @param relative the relative to set
         */
        public void setRelative( Boolean relative ) {
            this.relative = relative;
        }
    }


    private File getSVGInput() {
        return new File( getSvgPath() );
    }


    /**
     * @return the pngPath
     */
    public String getPngPath() {
        return pngPath;
    }


    /**
     * @param pngPath the pngPath to set
     */
    public void setPngPath( String pngPath ) {
        this.pngPath = pngPath;
    }


    /**
     * @return the svgPath
     */
    public String getSvgPath() {
        return svgPath;
    }


    /**
     * @param svgPath the svgPath to set
     */
    public void setSvgPath( String svgPath ) {
        this.svgPath = svgPath;
    }


    /**
     * @return the scale
     */
    public String getScale() {
        return scale;
    }


    /**
     * @param scale the scale to set
     */
    public void setScale( String scale ) {
        this.scale = scale;
    }
}
