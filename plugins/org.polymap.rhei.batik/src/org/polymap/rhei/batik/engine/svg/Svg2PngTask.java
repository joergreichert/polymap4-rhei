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

import org.polymap.rhei.batik.engine.svg.ImageConfiguration.ReplaceConfiguration;

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
            List<ImageConfiguration> imageConfigurations = imageConfigs
                    .stream()
                    .map( config -> {
                        ImageConfiguration imageConfig = new ImageConfiguration();
                        imageConfig.setName( config.getName() );
                        imageConfig.setDepth( config.getTypedDepth() );
                        imageConfig.setAdjHue( config.getTypedAdjHue() );
                        imageConfig.setAdjSaturation( config.getTypedAdjSaturation() );
                        imageConfig.setAdjBrightness( config.getTypedAdjBrightness() );
                        imageConfig.setColorType( config.getTypedColorType() );
                        imageConfig.setRGB( config.getTypedRGB() );
                        imageConfig.setRelative( config.isRelative() );
                        imageConfig.setInvert( config.isInvert() );
                        List<ReplaceConfiguration> list = config
                                .getReplaceConfigs()
                                .stream()
                                .map( rc -> new ReplaceConfiguration( getTypedRGB( rc.getSourceRGB() ), getTypedRGB( rc
                                        .getTargetRGB() ) ) ).collect( Collectors.toList() );
                        imageConfig.getReplaceConfigurations().addAll( list );
                        imageConfig.getTransparenceConfigurations().addAll(
                                config.getTransparenceConfigs().stream().map( tc -> getTypedRGB( tc.getValue() ) )
                                        .collect( Collectors.toList() ) );
                        return imageConfig;
                    } ).collect( Collectors.toList() );

            Function<String,Scale> getScaleOperation = ( String scaleStr ) -> Scale.getAsScale( Integer
                    .valueOf( scaleStr.trim() ) );
            List<Scale> scales = null;
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


    static RGB getTypedRGB( String rgbStr ) {
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
}
