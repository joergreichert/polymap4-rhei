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
package org.polymap.rhei.batik.image;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;
import org.polymap.rhei.batik.image.Svg2Png.Bounds;
import org.polymap.rhei.batik.image.Svg2Png.COLOR_DEPTH;
import org.polymap.rhei.batik.image.Svg2Png.COLOR_TYPE;
import org.polymap.rhei.batik.image.Svg2Png.ImageConfiguration;
import org.polymap.rhei.batik.image.Svg2Png.SCALE;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class Svg2PngTest {
    public static final String EXAMPLE_SVG = "Dopplr.svg";

    private Svg2Png svg2Png = new Svg2Png();

    @Test
    public void testScale_whenBiggerAndWidthGreaterThanHeight() {
        SCALE scale = SCALE.P16;
        Assert.assertEquals( 16f, scale.getWidth( new Bounds( 24f, 22f ) ), 0.5f );
        Assert.assertEquals( 14.7f, scale.getHeight( new Bounds( 24f, 22f ) ), 0.5f );
    }


    @Test
    public void testScale_whenBiggerAndWidthLowerThanHeight() {
        SCALE scale = SCALE.P16;
        Assert.assertEquals( 14.7f, scale.getWidth( new Bounds( 22f, 24f ) ), 0.5f );
        Assert.assertEquals( 16f, scale.getHeight( new Bounds( 22f, 24f ) ), 0.5f );
    }


    @Test
    public void testScale_whenBiggerAndWidthEqualHeight() {
        SCALE scale = SCALE.P16;
        Assert.assertEquals( 16f, scale.getWidth( new Bounds( 24f, 24f ) ), 0.5f );
        Assert.assertEquals( 16f, scale.getHeight( new Bounds( 24f, 24f ) ), 0.5f );
    }


    @Test
    public void testScale_whenSmallerAndWidthGreaterThanHeight() {
        SCALE scale = SCALE.P16;
        Assert.assertEquals( 16f, scale.getWidth( new Bounds( 14f, 12f ) ), 0.5f );
        Assert.assertEquals( 13.7f, scale.getHeight( new Bounds( 14f, 12f ) ), 0.5f );
    }


    @Test
    public void testScale_whenSmallerAndWidthLowerThanHeight() {
        SCALE scale = SCALE.P16;
        Assert.assertEquals( 13.7f, scale.getWidth( new Bounds( 12f, 14f ) ), 0.5f );
        Assert.assertEquals( 16f, scale.getHeight( new Bounds( 12f, 14f ) ), 0.5f );
    }


    @Test
    public void testScale_whenSmallerAndWidthEqualHeight() {
        SCALE scale = SCALE.P16;
        Assert.assertEquals( 16f, scale.getWidth( new Bounds( 14f, 14f ) ), 0.5f );
        Assert.assertEquals( 16f, scale.getHeight( new Bounds( 14f, 14f ) ), 0.5f );
    }


    @Test
    public void testSVGBounds() throws Exception {
        URL svgInputURL = getClass().getResource( EXAMPLE_SVG );
        try (InputStream svgInput = getClass().getResourceAsStream( EXAMPLE_SVG )) {
            Bounds bounds = svg2Png.getInitialSVGBounds( svgInputURL.toString(), svgInput );
            Assert.assertEquals( 588f, bounds.getHeight(), 0.5f );
            Assert.assertEquals( 588f, bounds.getWidth(), 0.5f );
        }
    }


    @Test
    public void testScaleSVG() throws Exception {
        URL svgInputURL = getClass().getResource( EXAMPLE_SVG );
        try (InputStream svgInput = getClass().getResourceAsStream( EXAMPLE_SVG )) {
            ImageConfiguration imageConfig = new ImageConfiguration();
            imageConfig.setDepth( COLOR_DEPTH.B4 );
            imageConfig.setAdjHue( 0.3f );
            imageConfig.setAdjSaturation( 0.8f );
            imageConfig.setAdjBrightness( 0f );
            imageConfig.setColorType( COLOR_TYPE.RGB);
            svg2Png.transcode( svgInputURL.toString().replace( ".svg", ".png" ), EXAMPLE_SVG, svgInput, Collections.singletonList(SCALE.P16), Collections.singletonList(imageConfig) );
            URL pngURL = getClass().getResource( "Dopplr_P16.png" );
            Assert.assertNotNull( pngURL );
            BufferedImage image = ImageIO.read( pngURL );
            Assert.assertEquals( 16f, image.getHeight(), 0.5f );
            Assert.assertEquals( 16f, image.getWidth(), 0.5f );
            Assert.assertEquals( 4, image.getColorModel().getPixelSize() );
        }
    }
}
