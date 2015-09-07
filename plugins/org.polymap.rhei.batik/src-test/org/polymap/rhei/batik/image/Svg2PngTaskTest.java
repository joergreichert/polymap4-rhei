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

import org.junit.Test;
import org.polymap.rhei.batik.image.Svg2PngTask.ImageConfig;


/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class Svg2PngTaskTest {

    @Test
    public void test_gray() {
        Svg2PngTask task = new Svg2PngTask();
        task.setSvgPath( "src-test/org/polymap/rhei/batik/image" );
        task.setPngPath( "build/result_gray" );
        task.setScale( "128" );
        ImageConfig imageConfig = new ImageConfig();
        //imageConfig.setDepth( "1" );
        imageConfig.setAdjHue( "0" );
        imageConfig.setAdjSaturation( "0" );
        imageConfig.setAdjBrightness( "0" );
        task.addImageConfig( imageConfig );
        task.execute();
    }

    @Test
    public void test_color_via_hue() {
        Svg2PngTask task = new Svg2PngTask();
        task.setSvgPath( "src-test/org/polymap/rhei/batik/image" );
        task.setPngPath( "build/result_color_hue" );
        task.setScale( "128" );
        ImageConfig imageConfig = new ImageConfig();
        //task.setDepth( "1" );
//        task.setAdjHue( "0.05" ); // brown
//        task.setAdjHue( "0.1" ); // yellow
//        task.setAdjHue( "0.2" ); // green
//        task.setAdjHue( "0.3" ); // darkgreen
//        task.setAdjHue( "0.4" ); // bluegreen
//        task.setAdjHue( "0.5" ); // lightblue
//        task.setAdjHue( "0.6" ); // darkblue
//        task.setAdjHue( "0.7" ); // purple
//        task.setAdjHue( "0.8" ); // pink
//        task.setAdjHue( "0.9" ); // pinkred
        imageConfig.setAdjHue( "1.0" ); // red, same as 0.0
        imageConfig.setAdjSaturation( "1" );
        imageConfig.setAdjBrightness( "0" );
        imageConfig.setColorType( "rgb" );
        task.addImageConfig( imageConfig );
        task.execute();
    }

    @Test
    public void test_color_via_rgb() {
        Svg2PngTask task = new Svg2PngTask();
        task.setSvgPath( "src-test/org/polymap/rhei/batik/image" );
        task.setPngPath( "build/result_color_rgb" );
        task.setScale( "128" );
        ImageConfig imageConfig = new ImageConfig();
        // http://www.w3.org/TR/css3-color/#svg-color
        imageConfig.setRgb( "#f0e68c" );
        imageConfig.setRelative( false );
        imageConfig.setColorType( "rgb" );
        task.addImageConfig( imageConfig );
        task.execute();
    }

    @Test
    public void test_lightgray() {
        Svg2PngTask task = new Svg2PngTask();
        task.setSvgPath( "src-test/org/polymap/rhei/batik/image" );
        task.setPngPath( "build/result_lightgray" );
        task.setScale( "128" );
        ImageConfig imageConfig = new ImageConfig();
        //task.setDepth( "1" );
        imageConfig.setAdjHue( "0" );
        imageConfig.setAdjSaturation( "0.5" );
        imageConfig.setAdjBrightness( "0.5" );
        imageConfig.setColorType( "gray" );
        task.addImageConfig( imageConfig );
        task.execute();
    }    

    @Test
    public void test_monochrom() {
        Svg2PngTask task = new Svg2PngTask();
        task.setSvgPath( "src-test/org/polymap/rhei/batik/image" );
        task.setPngPath( "build/result_monochrom" );
        task.setScale( "128" );
        ImageConfig imageConfig = new ImageConfig();
        //task.setDepth( "1" );
        imageConfig.setAdjHue( "0" );
        imageConfig.setAdjSaturation( "0" );
        imageConfig.setAdjBrightness( "0" );
        imageConfig.setColorType( "monochrom" );
        task.addImageConfig( imageConfig );
        task.execute();
    }
}
