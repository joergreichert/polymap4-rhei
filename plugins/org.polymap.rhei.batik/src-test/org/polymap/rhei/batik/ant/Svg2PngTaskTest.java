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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.polymap.rhei.batik.ant.ImageConfig.ReplaceConfig;
import org.polymap.rhei.batik.ant.ImageConfig.TransparenceConfig;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class Svg2PngTaskTest {

    @Test
    public void test_gray() throws IOException {
        String pngPath = "build/result_gray";
        int scale = 128;
        
        Svg2PngTask task = new Svg2PngTask();
        task.setSvgPath( "src-test/org/polymap/rhei/batik/ant" );
        task.setPngPath( pngPath );
        task.setScale( String.valueOf(scale) );
        ImageConfig imageConfig = new ImageConfig();
        // antConfig.setDepth( "1" );
        imageConfig.setColorType( "gray" );
        task.addImageConfig( imageConfig );
        task.execute();

        BufferedImage image = ImageIO.read( new File( pngPath + "/default/128/Dopplr.png" ) );
        
        assertBrightnessAt( image, 5, 17, 0.45f );
        assertHueForAll(image, 0f);
        assertSaturationForAll(image, 0f);
        assertAlphaValueForAll(image, 255);
    }

    @Test
    public void test_color_via_hue() throws IOException {
        String pngPath = "build/result_color_hue";
        int scale = 128;
        
        Svg2PngTask task = new Svg2PngTask();
        task.setSvgPath( "src-test/org/polymap/rhei/batik/ant" );
        task.setPngPath( pngPath );
        task.setScale( String.valueOf(scale) );
        ImageConfig imageConfig = new ImageConfig();
        // task.setDepth( "1" );
        // task.setAdjHue( "0.05" ); // brown
        // task.setAdjHue( "0.1" ); // yellow
        // task.setAdjHue( "0.2" ); // green
        // task.setAdjHue( "0.3" ); // darkgreen
        // task.setAdjHue( "0.4" ); // bluegreen
        // task.setAdjHue( "0.5" ); // lightblue
        // task.setAdjHue( "0.6" ); // darkblue
        // task.setAdjHue( "0.7" ); // purple
        // task.setAdjHue( "0.8" ); // pink
        // task.setAdjHue( "0.9" ); // pinkred
        imageConfig.setAdjHue( "1.0" ); // red, same as 0.0
        imageConfig.setAdjSaturation( "1" );
        imageConfig.setAdjBrightness( "0" );
        imageConfig.setColorType( "rgb" );
        task.addImageConfig( imageConfig );
        task.execute();

        BufferedImage image = ImageIO.read( new File( pngPath + "/default/128/Dopplr.png" ) );
        assertBrightnessAt( image, 5, 17, 0.5f );
        assertHueAt( image, 5, 17, 0.5f );
        assertSaturationAt( image, 5, 17, 0.9f );
        assertAlphaValueForAll(image, 255);
    }


    @Test
    public void test_color_via_rgb() throws IOException {
        String pngPath = "build/result_color_rgb";
        int scale = 128;
        
        Svg2PngTask task = new Svg2PngTask();
        task.setSvgPath( "src-test/org/polymap/rhei/batik/ant" );
        task.setPngPath( pngPath );
        task.setScale( String.valueOf(scale) );
        ImageConfig imageConfig = new ImageConfig();
        // http://www.w3.org/TR/css3-color/#svg-color
        imageConfig.setRgb( "#f0e68c" );
        imageConfig.setRelative( false );
        imageConfig.setColorType( "rgb" );
        task.addImageConfig( imageConfig );
        task.execute();

        BufferedImage image = ImageIO.read( new File( pngPath + "/default/128/Dopplr.png" ) );
        assertBrightnessAt( image, 5, 17, 0.5f );
        assertHueAt( image, 5, 17, 0.15f );
        assertSaturationAt( image, 5, 17, 0.4f );
        assertAlphaValueForAll(image, 255);
    }

    @Test
    public void test_color_via_argb() throws IOException {
        String pngPath = "build/result_color_argb";
        int scale = 128;
        
        Svg2PngTask task = new Svg2PngTask();
        task.setSvgPath( "src-test/org/polymap/rhei/batik/ant" );
        task.setPngPath( pngPath );
        task.setScale( String.valueOf(scale) );
        ImageConfig imageConfig = new ImageConfig();
        // http://www.w3.org/TR/css3-color/#svg-color
        imageConfig.setRgb( "#f0e68c" );
        imageConfig.setRelative( false );
        // have to use argb, as otherwise the alpha channel wouldn't be respected
        imageConfig.setColorType( "argb" );
        task.addImageConfig( imageConfig );
        task.execute();

        BufferedImage image = ImageIO.read( new File( pngPath + "/default/128/ic_delete_48px.png" ) );
        assertAlphaValueAt(image, 26, 21, 60);
        
        assertBrightnessForAll( image, 0f );
        assertHueForAll(image, 0f);
        assertSaturationForAll(image, 0f);
    }

    @Test
    public void test_replace_alpha() throws IOException {
        String pngPath = "build/result_replace";
        int scale = 128;
        
        Svg2PngTask task = new Svg2PngTask();
        task.setSvgPath( "src-test/org/polymap/rhei/batik/ant/ic_delete_48px.svg" );
        task.setPngPath( pngPath );
        task.setScale( String.valueOf(scale) );
        ImageConfig imageConfig = new ImageConfig();
        // http://www.w3.org/TR/css3-color/#svg-color
        imageConfig.setRgb( "#000000" );
        imageConfig.setRelative( false );
        imageConfig.setColorType( "argb" );
        ReplaceConfig replaceConfig = new ReplaceConfig();
        replaceConfig.setSourceRGB( "#000000" );
        replaceConfig.setTargetRGB( "#aaaaaa" );
        imageConfig.addReplaceConfig( replaceConfig );
        TransparenceConfig transparenceConfig = new TransparenceConfig();
        transparenceConfig.setValue( "#ffffff" );
        imageConfig.addTransparenceConfig( transparenceConfig );
        // imageConfig.setInvert( true );
        task.addImageConfig( imageConfig );
        task.execute();

        BufferedImage image = ImageIO.read( new File( pngPath + "/default/128/ic_delete_48px.png" ) );
        assertBrightnessAt( image, 5, 17, 0.66f );
        assertHueAt( image, 5, 17, 0.0f );
        assertSaturationAt( image, 5, 17, 0.0f );
        assertAlphaValueAt(image, 26, 21, 60);
        
        assertBrightnessForAll( image, 0.66f );
        assertHueForAll(image, 0f);
        assertSaturationForAll(image, 0f);
    }


    @Test
    public void test_lightgray() throws IOException {
        String pngPath = "build/result_lightgray";
        int scale = 128;
        
        Svg2PngTask task = new Svg2PngTask();
        task.setSvgPath( "src-test/org/polymap/rhei/batik/ant" );
        task.setPngPath( pngPath );
        task.setScale( String.valueOf(scale) );
        ImageConfig imageConfig = new ImageConfig();
        // task.setDepth( "1" );
        imageConfig.setAdjHue( "0" );
        imageConfig.setAdjSaturation( "0.5" );
        imageConfig.setAdjBrightness( "0.5" );
        imageConfig.setColorType( "gray" );
        task.addImageConfig( imageConfig );
        task.execute();

        BufferedImage image = ImageIO.read( new File( pngPath + "/default/128/Dopplr.png" ) );
        assertBrightnessAt( image, 5, 17, 0.45f );
//        assertHueAt( image, 5, 17, 0.15f );
//        assertSaturationAt( image, 5, 17, 0.4f );

        assertHueForAll(image, 0f);
        assertSaturationForAll(image, 0f);
        assertAlphaValueForAll(image, 255);
    }


    @Test
    public void test_monochrom() throws IOException {
        String pngPath = "build/result_monochrom";
        int scale = 128;
        
        Svg2PngTask task = new Svg2PngTask();
        task.setSvgPath( "src-test/org/polymap/rhei/batik/ant" );
        task.setPngPath( pngPath );
        task.setScale( String.valueOf(scale) );
        ImageConfig imageConfig = new ImageConfig();
        // task.setDepth( "1" );
        imageConfig.setAdjHue( "0" );
        imageConfig.setAdjSaturation( "0" );
        imageConfig.setAdjBrightness( "0" );
        imageConfig.setColorType( "monochrom" );
        task.addImageConfig( imageConfig );
        task.execute();

        BufferedImage image = ImageIO.read( new File( pngPath + "/default/" + scale + "/Dopplr.png" ) );

        assertBrightnessAt( image, 20, 10, 0.0f );
        assertBrightnessAt( image, 23, 13, 1.0f );
        assertHueForAll(image, 0f);
        assertSaturationForAll(image, 0f);
        assertAlphaValueForAll(image, 255);
    }


    private void assertHueAt( BufferedImage image, int x, int y, float value ) {
        assertValueAt(0, image, x, y, value);
    }

    private void assertSaturationAt( BufferedImage image, int x, int y, float value ) {
        assertValueAt(1, image, x, y, value);
    }
    
    private void assertBrightnessAt( BufferedImage image, int x, int y, float value ) {
        assertValueAt(2, image, x, y, value);
    }

    private void assertValueAt( int index, BufferedImage image, int x, int y, float value ) {
        float[] hsb = new float[3];
        Color color = new Color( image.getRGB( x, y ) );
        Color.RGBtoHSB( color.getRed(), color.getGreen(), color.getBlue(), hsb );
        String [] valueStrs = {"hue", "saturation", "brightness"}; 
        Assert.assertEquals( valueStrs[index] + " at " + x + ", " +  y, value, hsb[index], 0.05 );
    }
    
    private void assertAlphaValueAt(BufferedImage image, int x, int y, int value) {
        int alpha = (image.getRGB( x, y ) >> 24) & 0xFF;
        Assert.assertEquals( "Alpha at " + x + ", " +  y, value, alpha );
    }

    private void assertHueForAll( BufferedImage image, float value ) {
        assertValueForAll(0, image, value);
    }

    private void assertSaturationForAll( BufferedImage image, float value ) {
        assertValueForAll(1, image, value);
    }
    
    private void assertBrightnessForAll( BufferedImage image, float value ) {
        assertValueForAll(2, image, value);
    }
    
    private void assertAlphaValueForAll(BufferedImage image, int value) {
        int w = image.getWidth();
        int h = image.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                assertAlphaValueAt( image, x, y, value );
            }
        }
    }

    private void assertValueForAll( int index, BufferedImage image, float value ) {
        int w = image.getWidth();
        int h = image.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                assertValueAt(index, image, x, y, value);
            }
        }
    }

    @Test
    @Ignore("only for debugging")
    public void testAntFile() {
        File buildFile = new File( "svg.build.xml" );
        Project p = new Project();
        p.setUserProperty( "ant.file", buildFile.getAbsolutePath() );
        p.init();
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        p.addReference( "ant.projectHelper", helper );
        helper.parse( p, buildFile );
        p.executeTarget( p.getDefaultTarget() );
    }
}
