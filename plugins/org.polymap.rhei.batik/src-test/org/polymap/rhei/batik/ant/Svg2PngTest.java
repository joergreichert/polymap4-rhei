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

import java.util.Collections;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.io.FileUtils;

import org.polymap.rhei.batik.ant.Svg2Png.Bounds;
import org.polymap.rhei.batik.ant.Svg2Png.COLOR_TYPE;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class Svg2PngTest {
    public static final String EXAMPLE_SVG = "Dopplr.svg";
    public static final String EXAMPLE_SVG2 = "ic_delete_48px.svg";

    private Svg2Png svg2Png = new Svg2Png();

    @Test
    public void testScale_whenBiggerAndWidthGreaterThanHeight() {
        Scale scale = Scale.P16;
        Assert.assertEquals( 16f, scale.getWidth( new Bounds( 24f, 22f ) ), 0.5f );
        Assert.assertEquals( 14.7f, scale.getHeight( new Bounds( 24f, 22f ) ), 0.5f );
    }


    @Test
    public void testScale_whenBiggerAndWidthLowerThanHeight() {
        Scale scale = Scale.P16;
        Assert.assertEquals( 14.7f, scale.getWidth( new Bounds( 22f, 24f ) ), 0.5f );
        Assert.assertEquals( 16f, scale.getHeight( new Bounds( 22f, 24f ) ), 0.5f );
    }


    @Test
    public void testScale_whenBiggerAndWidthEqualHeight() {
        Scale scale = Scale.P16;
        Assert.assertEquals( 16f, scale.getWidth( new Bounds( 24f, 24f ) ), 0.5f );
        Assert.assertEquals( 16f, scale.getHeight( new Bounds( 24f, 24f ) ), 0.5f );
    }


    @Test
    public void testScale_whenSmallerAndWidthGreaterThanHeight() {
        Scale scale = Scale.P16;
        Assert.assertEquals( 16f, scale.getWidth( new Bounds( 14f, 12f ) ), 0.5f );
        Assert.assertEquals( 13.7f, scale.getHeight( new Bounds( 14f, 12f ) ), 0.5f );
    }


    @Test
    public void testScale_whenSmallerAndWidthLowerThanHeight() {
        Scale scale = Scale.P16;
        Assert.assertEquals( 13.7f, scale.getWidth( new Bounds( 12f, 14f ) ), 0.5f );
        Assert.assertEquals( 16f, scale.getHeight( new Bounds( 12f, 14f ) ), 0.5f );
    }


    @Test
    public void testScale_whenSmallerAndWidthEqualHeight() {
        Scale scale = Scale.P16;
        Assert.assertEquals( 16f, scale.getWidth( new Bounds( 14f, 14f ) ), 0.5f );
        Assert.assertEquals( 16f, scale.getHeight( new Bounds( 14f, 14f ) ), 0.5f );
    }


    @Test
    public void testSVGBounds() throws Exception {
        URL svgInputURL = getClass().getResource( EXAMPLE_SVG );
        try (InputStream svgInput = getClass().getResourceAsStream( EXAMPLE_SVG )) {
            Bounds bounds = svg2Png.getInitialSVGBounds( svgInputURL.toString(), svgInput );
            Assert.assertEquals( 236f, bounds.getHeight(), 0.5f );
            Assert.assertEquals( 236f, bounds.getWidth(), 0.5f );
        }
    }


    @Test
    public void testScaleSVG() throws Exception {
        ImageConfiguration imageConfig = new ImageConfiguration();
        imageConfig.setName( "default" );
        imageConfig.setDepth( ColorDepth.B4 );
        imageConfig.setAdjHue( 0.3f );
        imageConfig.setAdjSaturation( 0.8f );
        imageConfig.setAdjBrightness( 0f );
        imageConfig.setColorType( COLOR_TYPE.RGB);

        File pngPath = new File( "/tmp/Svg2PngTest/" );
        pngPath.mkdirs();
        FileUtils.cleanDirectory( pngPath );

        File svgFile = new File( pngPath, EXAMPLE_SVG2 );
        FileUtils.copyURLToFile( getClass().getResource( EXAMPLE_SVG2 ), svgFile );

        svg2Png.transcode(
                pngPath.getAbsolutePath(),
                Collections.singletonList( svgFile ),
                Collections.singletonList( Scale.P16 ),
                Collections.singletonList( imageConfig ) );
        
        URL pngURL = getClass().getResource( "default/16/" + EXAMPLE_SVG2.replace( ".svg", ".png" ));
        Assert.assertNotNull( pngURL );
        BufferedImage image = ImageIO.read( pngURL );
        Assert.assertNotNull( image );
        Assert.assertEquals( 16f, image.getHeight(), 0.5f );
        Assert.assertEquals( 12f, image.getWidth(), 0.5f );
        Assert.assertEquals( 1, image.getColorModel().getPixelSize() );
    }
}
