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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.FilenameUtils;

import org.polymap.rhei.batik.engine.svg.ImageConfiguration.ReplaceConfiguration;

import org.w3c.dom.svg.SVGDocument;

import com.google.common.base.Strings;

/**
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Svg2Png {

    public void transcode( String pngPath, List<File> svgFiles, List<Scale> scales,
            List<ImageConfiguration> imageConfigurations ) throws TranscoderException, IOException {
        for (File svgFile : svgFiles) {
            try (
                FileInputStream fis = new FileInputStream( svgFile )
            ){
                byte[] bytes = getImageAsBytes( fis );
                Bounds bounds = getInitialSVGBounds( svgFile );
                
                String svgFileName = FilenameUtils.getBaseName( svgFile.getName() );

                if (!pngPath.endsWith( "/" )) {
                    pngPath += "/";
                }

                for (ImageConfiguration imageConfiguration : imageConfigurations) {
                    for (Scale scale : scales) {
                        String baseName = FilenameUtils.getBaseName( svgFileName );
                        String imagePath = pngPath + imageConfiguration.getName() + "/" + scale.name().substring( 1 ) + "/"
                                + baseName + ".png";
                        File pngFile = new File( imagePath.replace( "file:", "" ) );
                        pngFile.getParentFile().mkdirs();
                        
                        transcode( pngFile, bytes, bounds, scale, imageConfiguration );
                    }
                }
            }
        }
    }


    private byte[] getImageAsBytes( InputStream fis ) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read = -1;
        while ((read = fis.read()) != -1) {
            out.write( read );
        }
        out.flush();
        out.close();
        fis.close();
        return out.toByteArray();
    }


    public void transcode( File pngFile, URL svgInput, Scale scale,
            ImageConfiguration imageConfiguration ) throws TranscoderException, IOException {
        byte[] bytes = getImageAsBytes( svgInput.openStream() );
        Bounds bounds = getInitialSVGBounds( svgInput.toString(), new ByteArrayInputStream( bytes ) );
        transcode( pngFile, bytes, bounds, scale, imageConfiguration );
    }


    public Bounds getInitialSVGBounds( File svgFile ) throws IOException {
        try (FileInputStream fis = new FileInputStream( svgFile )) {
            String url = "file://" + svgFile.getAbsolutePath();
            return getInitialSVGBounds( url, fis );
        }
    }


    public Bounds getInitialSVGBounds( String url, InputStream svgInput ) throws IOException {
        try {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory( parser );
            SVGDocument doc = (SVGDocument) f.createDocument( url, svgInput );
            String widthStr = doc.getRootElement().getAttribute( "width" );
            String heightStr = doc.getRootElement().getAttribute( "height" );
            if(!Strings.isNullOrEmpty( widthStr) && !Strings.isNullOrEmpty( heightStr)) {
                int width = Integer.valueOf(doc.getRootElement().getAttribute( "width" ).replace( "px", "" ));
                int height = Integer.valueOf(doc.getRootElement().getAttribute( "height" ).replace( "px", "" ));
                return new Bounds( width, height );
            } else {
                BridgeContext ctx = new BridgeContext( new UserAgentAdapter() );
                GVTBuilder builder = new GVTBuilder();
                GraphicsNode gvtRoot = builder.build( ctx, doc );
                Rectangle2D rc = gvtRoot.getSensitiveBounds();
                if (rc == null) {
                    System.err.println( url + " has no bounding box." );
                    return new Bounds( 0f, 0f );
                }
                else {
                    return new Bounds( Double.valueOf( rc.getWidth() ).floatValue(), Double.valueOf( rc.getHeight() )
                            .floatValue() );
                }
            }
            
        }
        finally {
            svgInput.close();
        }
    }


    private void transcode( File pngFile, byte[] imageBytes, Bounds bounds, Scale scale,
            ImageConfiguration imageConfiguration ) throws TranscoderException, IOException {
        TranscoderInput input = new TranscoderInput( new ByteArrayInputStream( imageBytes ) );
        try {
            PNGTranscoder transcoder = new PNGTranscoder();
            try (FileOutputStream writer = new FileOutputStream( pngFile )) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                TranscoderOutput output = new TranscoderOutput( out );
                Bounds newBounds = new Bounds( scale.getWidth( bounds ), scale.getHeight( bounds ) );
                transcoder.setTranscodingHints( createTranscodingHints( newBounds,
                        imageConfiguration.getDepth() ) );
                transcoder.transcode( input, output );

                writer.write( out.toByteArray() );
                out.close();
                writer.flush();
            }
            BufferedImage originalImage = null;
            try {
                originalImage = ImageIO.read( pngFile );
                BufferedImage bufferedImage = transform( originalImage, imageConfiguration );
                try (FileOutputStream writer = new FileOutputStream( pngFile )) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    TranscoderOutput output = new TranscoderOutput( out );
                    transcoder.writeImage( bufferedImage, output );
                    writer.write( out.toByteArray() );
                    out.close();
                    writer.flush();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            System.err.println( e.getMessage() );
            e.printStackTrace();
            throw e;
        }
    }


    public static BufferedImage transform( BufferedImage img, ImageConfiguration imageConfiguration ) {
        ColorSpace imgCS = img.getColorModel().getColorSpace();
        ColorSpace grayCS = ColorSpace.getInstance( ColorSpace.CS_GRAY );
        ColorConvertOp cop = new ColorConvertOp( imgCS, grayCS, null );
        BufferedImage finalThresholdImage = cop.filter( img, null );
        finalThresholdImage = shiftHue( finalThresholdImage, imageConfiguration );
        return finalThresholdImage;
    }


    private static BufferedImage shiftHue( BufferedImage img, ImageConfiguration imageConfiguration ) {
        int height = img.getHeight();
        int width = img.getWidth();
        int imageType = 0;
        if (imageConfiguration.getColorType() == COLOR_TYPE.MONOCHROM) {
            imageType = BufferedImage.TYPE_BYTE_BINARY;
        }
        else if (imageConfiguration.getColorType() == COLOR_TYPE.GRAY) {
            imageType = BufferedImage.TYPE_BYTE_GRAY;
        }
        else if (imageConfiguration.getColorType() == COLOR_TYPE.RGB) {
            imageType = BufferedImage.TYPE_INT_RGB;
        }
        else {
            imageType = BufferedImage.TYPE_INT_ARGB;
        }

        BufferedImage finalThresholdImage = new BufferedImage( width, height, imageType );

        Float hueDelta = null;
        Float saturationDelta = null;
        Float brightnessDelta = null;
        if (imageConfiguration.getRgb() != null) {
            hueDelta = degreeToPercent( imageConfiguration.getRgb().getHSB()[0] );
            saturationDelta = imageConfiguration.getRgb().getHSB()[1];
            brightnessDelta = imageConfiguration.getRgb().getHSB()[2];
        }
        else {
            hueDelta = imageConfiguration.getAdjHue();
            saturationDelta = imageConfiguration.getAdjSaturation();
            brightnessDelta = imageConfiguration.getAdjBrightness();
        }

        if (hueDelta == null) {
            hueDelta = 0f;
        }
        if (saturationDelta == null) {
            saturationDelta = 0f;
        }
        if (brightnessDelta == null) {
            brightnessDelta = 0f;
        }
        int sRGB, red, green, blue;
        for (int x = 0; x < width; x++) {
            try {
                for (int y = 0; y < height; y++) {
                    int rgb = img.getRGB( x, y );
                    sRGB = 0xff000000 | rgb;
                    red = (sRGB >> 16) & 0xFF;
                    green = (sRGB >> 8) & 0xFF;
                    blue = (sRGB >> 0) & 0xFF;
                    
                    float[] hsb = getHsb( imageConfiguration, red, green, blue );
                    hsb = applyDeltas( imageConfiguration, hueDelta, saturationDelta, brightnessDelta, hsb );
                    hsb = replaceColors( imageConfiguration, red, green, blue, hsb );
                    int newRGB = Color.HSBtoRGB( hsb[0], hsb[1], hsb[2] );
                    finalThresholdImage.setRGB( x, y, newRGB );
                    if(imageType == BufferedImage.TYPE_INT_ARGB) {
                        setAlpha( finalThresholdImage, x, y, rgb );
                        finalThresholdImage = makeColorsTransparent( finalThresholdImage, imageConfiguration, newRGB );
                    }
                }
            }
            catch (Exception e) {
                e.getMessage();
            }
        }

        return finalThresholdImage;
    }


    /**
     * @param imageConfiguration
     * @param color
     * @param newRGB
     * @return
     */
    private static BufferedImage makeColorsTransparent( BufferedImage finalThresholdImage,
            ImageConfiguration imageConfiguration, int sRGB ) {
        int red = (sRGB >> 16) & 0xFF;
        int green = (sRGB >> 8) & 0xFF;
        int blue = (sRGB >> 0) & 0xFF;
        boolean found = imageConfiguration
                .getTransparenceConfigurations()
                .stream()
                .anyMatch( tc -> tc.red == red && tc.green == green && tc.blue == blue );
        if (found) {
            finalThresholdImage = makeColorTransparent( finalThresholdImage, sRGB );
        }
        return finalThresholdImage;
    }


    public static BufferedImage makeColorTransparent( BufferedImage im, int sRGB ) {
        ImageFilter filter = new RGBImageFilter() {

            private int shift                = 0xFF000000;

            public int  rgbToMakeTransparent = sRGB | shift;


            public final int filterRGB( int x, int y, int rgb ) {
                if ((rgb | shift) == rgbToMakeTransparent) {
                    return 0x00FFFFFF & rgb;
                }
                return rgb;
            }
        };
        Image newImage = Toolkit.getDefaultToolkit().createImage( new FilteredImageSource( im.getSource(), filter ) );
        BufferedImage bufferedImage = new BufferedImage( newImage.getWidth( null ), newImage.getHeight( null ),
                BufferedImage.TYPE_INT_ARGB );
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage( newImage, 0, 0, null );
        g2.dispose();
        return bufferedImage;
    }


    private static void setAlpha( BufferedImage finalThresholdImage, int x, int y, int rgb ) {
        int bands = finalThresholdImage.getAlphaRaster().getSampleModel().getNumBands();
        int alpha = (rgb >> 24) & 0xFF;
        for (int b = 0; b < bands; b++) {
            finalThresholdImage.getAlphaRaster().setSample( x, y, b, alpha );
        }
    }


    private static float[] getHsb( ImageConfiguration imageConfiguration, int red, int green, int blue ) {
        float[] hsb = new float[3];
        if (imageConfiguration.isInvert()) {
            Color.RGBtoHSB( 255 - red, 255 - green, 255 - blue, hsb );
        }
        else {
            Color.RGBtoHSB( red, green, blue, hsb );
        }
        return hsb;
    }


    private static float[] replaceColors( ImageConfiguration imageConfiguration, int red, int green, int blue, final float[] currentHsb ) {
        float[] hsb;
        Optional<ReplaceConfiguration> config = imageConfiguration
                .getReplaceConfigurations()
                .stream()
                .filter(
                        rc -> rc.getFrom() != null && rc.getTo() != null && rc.getFrom().red == red
                                && rc.getFrom().green == green && rc.getFrom().blue == blue )
                .findFirst();
        if (config.isPresent()) {
            hsb = config.get().getTo().getHSB();
            hsb[0] = degreeToPercent(hsb[0]);
        }
        else {
            hsb = currentHsb;
        }
        return hsb;
    }


    private static float[] applyDeltas( ImageConfiguration imageConfiguration, Float hueDelta, Float saturationDelta,
            Float brightnessDelta, float[] hsb ) {
        float adjustedHue = 0;
        float adjustedSaturation = 0;
        float adjustedBrightness = 0;
        if (imageConfiguration.isRelative()) {
            adjustedHue = makeInRange( hsb[0] + hueDelta );
            adjustedSaturation = makeInRange( hsb[1] + saturationDelta );
            adjustedBrightness = makeInRange( hsb[2] + brightnessDelta );
        }
        else {
            adjustedHue = hueDelta;
            adjustedSaturation = saturationDelta;
            adjustedBrightness = hsb[2];
        }

        final float[] currentHsb = new float[] { adjustedHue, adjustedSaturation, adjustedBrightness };
        return currentHsb;
    }


    /**
     * @param f
     * @return
     */
    private static float degreeToPercent( float degree ) {
        if (degree > 360) {
            degree = 360;
        }
        else if (degree < 0) {
            degree = 0;
        }
        return degree / 360;
    }


    private static float makeInRange( float adjustedHue ) {
        if (adjustedHue > 1) {
            adjustedHue = 1;
        }
        else if (adjustedHue < 0) {
            adjustedHue = 1;
        }
        return adjustedHue;
    }


    private TranscodingHints createTranscodingHints( Bounds bounds, ColorDepth depth ) {
        TranscodingHints transcoderHints = new TranscodingHints();
        transcoderHints.put( ImageTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE );
        transcoderHints.put( ImageTranscoder.KEY_DOM_IMPLEMENTATION, SVGDOMImplementation.getDOMImplementation() );
        transcoderHints.put( ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, SVGConstants.SVG_NAMESPACE_URI );
        transcoderHints.put( ImageTranscoder.KEY_DOCUMENT_ELEMENT, "svg" );
        transcoderHints.put( ImageTranscoder.KEY_WIDTH, bounds.getWidth() );
        transcoderHints.put( ImageTranscoder.KEY_HEIGHT, bounds.getHeight() );
        if (depth != null) {
            transcoderHints.put( PNGTranscoder.KEY_INDEXED, depth.getBit() );
        }
        return transcoderHints;
    }


    public static class Bounds {

        private final float width;

        private final float height;


        Bounds( float width, float height ) {
            this.width = width;
            this.height = height;
        }


        public float getWidth() {
            return width;
        }


        public float getHeight() {
            return height;
        }
    }


    public enum COLOR_TYPE {
        MONOCHROM, GRAY, RGB, ARGB
    }
}
