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

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
import org.w3c.dom.Document;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class Svg2Png {

    enum COLOR_DEPTH {
        B1(1), B2(2), B4(4), B8(8);

        private final int bit;


        COLOR_DEPTH( int bit ) {
            this.bit = bit;
        }


        private int getBit() {
            return bit;
        }


        /**
         * @param depth
         * @return
         */
        public static COLOR_DEPTH getAsDepth( int depth ) {
            for (COLOR_DEPTH value : values()) {
                if (value.getBit() == depth) {
                    return value;
                }
            }
            throw new IllegalArgumentException( depth + " is an unsupported color depth." );
        }
    }


    enum SCALE {

        P8(8f, 8f), P16(16f, 16f), P32(32f, 32f), P48(48f, 48f), P64(64f, 64f), P128(128f, 128f);

        private final float width;

        private final float height;


        SCALE( float width, float height ) {
            this.width = width;
            this.height = height;
        }


        public float getWidth() {
            return width;
        }


        public float getHeight() {
            return height;
        }


        public static SCALE getAsScale( int number ) {
            for (SCALE value : values()) {
                if (value.getWidth() == number && value.getHeight() == number) {
                    return value;
                }
            }
            throw new IllegalArgumentException( number + " is an unsupported pixel scale." );
        }


        public float getWidth( Bounds bounds ) {
            if (bounds.getWidth() >= getWidth()) {
                if (bounds.getHeight() < bounds.getWidth()) {
                    return getWidth();
                }
                else {
                    return bounds.getWidth() * getWidth() / bounds.getHeight();
                }
            }
            else {
                if (bounds.getHeight() < bounds.getWidth()) {
                    return getWidth();
                }
                else {
                    return getHeight() * bounds.getWidth() / bounds.getHeight();
                }
            }
        }


        public float getHeight( Bounds bounds ) {
            if (bounds.getHeight() >= getHeight()) {
                if (bounds.getWidth() < bounds.getHeight()) {
                    return getHeight();
                }
                else {
                    return bounds.getHeight() * getHeight() / bounds.getWidth();
                }
            }
            else {
                if (bounds.getHeight() > bounds.getWidth()) {
                    return getHeight();
                }
                else if (bounds.getHeight() >= bounds.getWidth()) {
                    return getHeight();
                }
                else {
                    return getWidth() * bounds.getHeight() / bounds.getWidth();
                }
            }
        }
    }


    public void transcode( String pngPath, List<File> svgFiles, List<SCALE> scales,
            List<ImageConfiguration> imageConfigurations ) throws TranscoderException, IOException {
        for (File svgFile : svgFiles) {
            try (FileInputStream fis = new FileInputStream( svgFile )) {
                byte[] bytes = getImageAsBytes( fis );
                Bounds bounds = getInitialSVGBounds( svgFile );
                String svgFileName = FilenameUtils.getBaseName( svgFile.getName() );
                transcode( pngPath, svgFileName, bytes, bounds, scales, imageConfigurations );
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


    public void transcode( String absolutePngPath, String svgFileName, InputStream svgInput, List<SCALE> scales,
            List<ImageConfiguration> imageConfigurations ) throws TranscoderException, IOException {
        byte[] bytes = getImageAsBytes( svgInput );
        Bounds bounds = getInitialSVGBounds( svgFileName, new ByteArrayInputStream( bytes ) );
        transcode( absolutePngPath, svgFileName, bytes, bounds, scales, imageConfigurations );
    }


    public Bounds getInitialSVGBounds( File svgFile ) throws IOException {
        try (FileInputStream fis = new FileInputStream( svgFile )) {
            String url = "file://" + svgFile.getAbsolutePath();
            return getInitialSVGBounds( url, fis );
        }
    }


    public Bounds getInitialSVGBounds( String url, InputStream svgInput ) throws IOException {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory( parser );
        Document doc = f.createDocument( url, svgInput );
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


    private void transcode( String pngPath, String svgFileName, byte[] imageBytes, Bounds bounds, List<SCALE> scales,
            List<ImageConfiguration> imageConfigurations ) throws TranscoderException, IOException {
        for (ImageConfiguration imageConfiguration : imageConfigurations) {
            for (SCALE scale : scales) {
                TranscoderInput input = new TranscoderInput( new ByteArrayInputStream( imageBytes ) );
                try {
                    PNGTranscoder transcoder = new PNGTranscoder();
                    String absolutePngPath = pngPath;
                    if (!absolutePngPath.endsWith( "/" )) {
                        absolutePngPath += "/";
                    }
                    String baseName = FilenameUtils.getBaseName( svgFileName );
                    absolutePngPath += imageConfiguration.getName() + "/" + scale.name().substring( 1 ) + "/"
                            + baseName + ".png";
                    File pngFile = new File( absolutePngPath.replace( "file:", "" ) );
                    pngFile.getParentFile().mkdirs();
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
                    }
                }
                catch (Exception e) {
                    System.err.println( e.getMessage() );
                    e.printStackTrace();
                    throw e;
                }
            }
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
        else {
            imageType = BufferedImage.TYPE_INT_RGB;
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
        for (int x = 0; x < width; x++) {
            try {
                for (int y = 0; y < height; y++) {
                    int rgb = img.getRGB( x, y );
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;

                    float[] hsb = new float[3];

                    Color.RGBtoHSB( red, green, blue, hsb );
                    float adjustedHue = 0;
                    float adjustedSaturation = 0;
                    float adjustedBrightness = 0;
                    if (imageConfiguration.isRelative()) {
                        adjustedHue = makeInRange(hsb[0] + hueDelta);
                        adjustedSaturation = makeInRange(hsb[1] + saturationDelta);
                        adjustedBrightness = makeInRange(hsb[2] + brightnessDelta);
                    }
                    else {
                        adjustedHue = hueDelta;
                        adjustedSaturation = saturationDelta;
                        adjustedBrightness = hsb[2];
                    }
                    int newRGB = Color.HSBtoRGB( adjustedHue, adjustedSaturation, adjustedBrightness );
                    finalThresholdImage.setRGB( x, y, newRGB );
                }
            }
            catch (Exception e) {
                e.getMessage();
            }
        }

        return finalThresholdImage;
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


    private TranscodingHints createTranscodingHints( Bounds bounds, COLOR_DEPTH depth ) {
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


    enum COLOR_TYPE {
        MONOCHROM, GRAY, RGB
    }


    public static class ImageConfiguration {

        private String                       name;

        private COLOR_DEPTH                  depth;

        private Float                        adjHue;

        private Float                        adjSaturation;

        private Float                        adjBrightness;

        private COLOR_TYPE                   colorType;

        private Boolean                      relative = true;

        private org.eclipse.swt.graphics.RGB rgb;


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
        public COLOR_DEPTH getDepth() {
            return depth;
        }


        /**
         * @param depth the depth to set
         */
        public void setDepth( COLOR_DEPTH depth ) {
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
    }
}
