/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.batik.app;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.polymap.core.runtime.Callback;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.DefaultString;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.ui.ImageRegistryHelper;
import org.polymap.rhei.batik.engine.svg.ImageConfiguration;
import org.polymap.rhei.batik.engine.svg.ImageConfiguration.ReplaceConfiguration;
import org.polymap.rhei.batik.engine.svg.Scale;
import org.polymap.rhei.batik.engine.svg.Svg2Png;
import org.polymap.rhei.batik.engine.svg.Svg2Png.COLOR_TYPE;

import com.google.common.base.Joiner;

/**
 * Provides auto generated PNG icons from SVG source.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SvgImageRegistryHelper
        extends ImageRegistryHelper {

    private static Log         log          = LogFactory.getLog( SvgImageRegistryHelper.class );

    /** Image configuration used to create {@link #svgImage(String, String)}. */
    public final static String NORMAL48     = "normal48";

    /** Image configuration used to create {@link #svgImage(String, String)}. */
    public final static String ACTION48     = "normal48-link";

    /** Normal image configuration used to create {@link #svgImage(String, String)}. */
    public final static String NORMAL12     = "normal12";

    /** Action image configuration used to create {@link #svgImage(String, String)}. */
    public final static String ACTION12     = "normal12-link";

    /** Normal image configuration used to create {@link #svgImage(String, String)}. */
    public final static String NORMAL24     = "normal24";

    /**
     * White/background image configuration used to create
     * {@link #svgImage(String, String)}.
     */
    public final static String WHITE24      = "white24";

    /** Action image configuration used to create {@link #svgImage(String, String)}. */
    public final static String ACTION24     = "normal24-link";

    /** Action image configuration used to create {@link #svgImage(String, String)}. */
    public final static String OK24         = "normal24-ok";

    /** Action image configuration used to create {@link #svgImage(String, String)}. */
    public final static String ALERT24      = "normal24-alert";

    /** Action image configuration used to create {@link #svgImage(String, String)}. */
    public final static String ERROR24      = "normal24-error";

    /**
     * Image configuration for normale, disabled icons created by
     * {@link #svgImage(String, String)}.
     */
    public final static String DISABLED24   = "normal24-disabled";

    /**
     * Image configuration for normale, hovered icons created by
     * {@link #svgImage(String, String)}.
     */
    public final static String HOVER24      = "normal24-hovered";

    /** Normal image configuration used to create {@link #svgImage(String, String)}. */
    public final static String OVR12_ACTION = "ovr12-action";


    /**
     * The quadrant of an overlay created by
     * {@link SvgImageRegistryHelper#svgOverlayedImage(String, String, String, String, Quadrant)}
     * .
     */
    public enum Quadrant {
        TopLeft(IDecoration.TOP_LEFT), TopRight(IDecoration.TOP_RIGHT), BottmLeft(IDecoration.BOTTOM_LEFT), BottomRight(
                IDecoration.BOTTOM_RIGHT);

        public int iDecorationConstant;


        private Quadrant( int iDecorationConstant ) {
            this.iDecorationConstant = iDecorationConstant;
        }
    }

    // instance *******************************************

    @Immutable
    @DefaultString("resources/icons/")
    public Config2<SvgImageRegistryHelper,String> svgBasePath;

    private File                                  tempFolder;

    private Map<String,SvgConfiguration>          svgConfigs = new HashMap<>();


    public SvgImageRegistryHelper( AbstractUIPlugin plugin ) {
        super( plugin );
        ConfigurationFactory.inject( this );

        // temp folder
        try {
            tempFolder = new File( plugin.getStateLocation().toFile(), "svg-icons" );
            tempFolder.mkdirs();
            FileUtils.cleanDirectory( tempFolder );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }

        // default configs
        RGB white = new RGB( 0xFF, 0xFF, 0xFF );
        RGB action = new RGB( 0x5A, 0xA9, 0xDD );
        RGB normal = new RGB( 0x84, 0xA4, 0xC1 ); // new RGB( 150, 150, 150 );
        putConfig( NORMAL12, new ReplaceBlackSvgConfiguration( normal, 16 ) );
        putConfig( NORMAL24, new ReplaceBlackSvgConfiguration( normal, 24 ) );
        putConfig( NORMAL48, new ReplaceBlackSvgConfiguration( normal, 48 ) );
        putConfig( ACTION48, new ReplaceBlackSvgConfiguration( action, 48 ) );
        putConfig( ACTION24, new ReplaceBlackSvgConfiguration( action, 24 ) );
        putConfig( ACTION12, new ReplaceBlackSvgConfiguration( action, 16 ) );
        putConfig( OK24, new ReplaceBlackSvgConfiguration( new RGB( 0x81, 0xCC, 0x39 ), 24 ) );
        putConfig( ALERT24, new ReplaceBlackSvgConfiguration( new RGB( 0xFF, 0xD2, 0x2C ), 24 ) );
        putConfig( ERROR24, new ReplaceBlackSvgConfiguration( new RGB( 0xFF, 0x61, 0x39 ), 24 ) );
        putConfig( WHITE24, new ReplaceBlackSvgConfiguration( white, 24 ) );
        putConfig( OVR12_ACTION, new ReplaceBlackSvgConfiguration( new RGB( 140, 240, 100 ), 16 ) );
    }


    public void putConfig( String name, SvgConfiguration config ) {
        config.baseTempFolder = tempFolder;
        config.plugin = plugin;
        svgConfigs.put( name, config );
    }


    public boolean existsConfig( String name ) {
        return svgConfigs.containsKey( name );
    }


    /**
     * 
     *
     * @param path The path to the image inside the bundle. This can be relative to
     *        the bundle root or relative to {@link #svgBasePath}.
     * @param configName
     * @return Newly generated are cached image. Must no be used outside current user
     *         session!
     */
    public Image svgImage( String path, String configName ) {
        String key = configName + "-" + path;
        Image image = registry.get().get( key );

        if (image == null || image.isDisposed()) {
            registry.get().put( key, createSvgImage( configName, path ) );
            image = registry.get().get( key );
        }
        return image;
    }


    public void svgImage( String path, String configName, Display display, Callback<ImageDescriptor> callback ) {
        Callback<ImageDescriptor> callbackWrapper = new Callback<ImageDescriptor>() {

            @Override
            public void handle( ImageDescriptor result ) {
                callback.handle( result );
                display.asyncExec( new Runnable() {

                    @Override
                    public void run() {
                        registerImage( path, configName, result );
                    }
                } );
            }
        };
        createSvgImage( configName, path, callbackWrapper );
    }


    private void registerImage( String path, String configName, ImageDescriptor result ) {
        String key = configName + "-" + path;
        Image image = registry.get().get( key );

        if (image == null || image.isDisposed()) {
            registry.get().put( key, result );
        }
    }


    public void svgImages( List<String> paths, String configName, Display display,
            Callback<Map<String,ImageDescriptor>> callback ) {
        Callback<Map<String,ImageDescriptor>> callbackWrapper = new Callback<Map<String,ImageDescriptor>>() {

            @Override
            public void handle( Map<String,ImageDescriptor> results ) {
                callback.handle( results );
                display.asyncExec( new Runnable() {

                    @Override
                    public void run() {
                        for (String path : paths) {
                            registerImage( path, configName, results.get( path ) );
                        }
                    }
                } );
            }
        };
        createSvgImage( configName, paths, callbackWrapper );
    }


    /**
     * 
     *
     * @param baseImagePath
     * @param baseConfigName
     * @param ovrImagePath
     * @param ovrConfigName
     * @param quadrant
     * @return Newly generated are cached image. Must no be used outside current user
     *         session!
     */
    public Image svgOverlayedImage( String baseImagePath, String baseConfigName, String ovrImagePath,
            String ovrConfigName, Quadrant quadrant ) {
        String key = Joiner.on( "-" ).join( baseImagePath, baseConfigName, ovrImagePath, ovrConfigName );
        Image image = registry.get().get( key );
        if (image == null || image.isDisposed()) {
            Image baseImage = svgImage( baseImagePath, baseConfigName );
            ImageDescriptor ovrImage = svgImageDescriptor( ovrImagePath, ovrConfigName );
            image = new DecorationOverlayIcon( baseImage, ovrImage, quadrant.iDecorationConstant ).createImage();

            registry.get().put( key, image );
            image = registry.get().get( key );
        }
        return image;
    }


    public void svgOverlayedImage( String baseImagePath, String baseConfigName, String ovrImagePath,
            String ovrConfigName, Quadrant quadrant, Display display, Callback<ImageDescriptor> callback ) {
        String key = Joiner.on( "-" ).join( baseImagePath, baseConfigName, ovrImagePath, ovrConfigName );
        Image image = registry.get().get( key );
        if (image == null || image.isDisposed()) {
            Callback<ImageDescriptor> callbackWrapper = new Callback<ImageDescriptor>() {

                @Override
                public void handle( ImageDescriptor result ) {
                    callback.handle( result );
                    Callback<ImageDescriptor> callbackWrapper2 = new Callback<ImageDescriptor>() {

                        @Override
                        public void handle( ImageDescriptor result ) {
                            callback.handle( result );
                            display.asyncExec( new Runnable() {

                                public void run() {
                                    Image image = new DecorationOverlayIcon( result.createImage(), result,
                                            quadrant.iDecorationConstant ).createImage();
                                    registry.get().put( key, image );
                                };
                            } );
                        }
                    };
                    svgImageDescriptor( ovrImagePath, ovrConfigName, display, callbackWrapper2 );
                }
            };
            svgImage( baseImagePath, baseConfigName, display, callbackWrapper );
        }
    }


    /**
     * 
     *
     * @param path The path to the image inside the bundle. This can be relative to
     *        the bundle root or relative to {@link #svgBasePath}.
     * @param configName
     */
    public ImageDescriptor svgImageDescriptor( String path, String configName ) {
        svgImage( path, configName );

        String key = configName + "-" + path;
        return registry.get().getDescriptor( key );
    }


    public void svgImageDescriptor( String path, String configName, Display display, Callback<ImageDescriptor> callback ) {
        svgImage( path, configName, display, callback );
    }


    public void svgImageDescriptors( List<String> paths, String configName, Display display,
            Callback<Map<String,ImageDescriptor>> callback ) {
        svgImages( paths, configName, display, callback );
    }


    /**
     * 
     *
     * @param configName
     * @param path The path to the image inside the bundle. This can be relative to
     *        the bundle root or relative to {@link #svgBasePath}.
     * @return Newly created {@link Image}.
     */
    protected ImageDescriptor createSvgImage( String configName, String path ) {
        path = mapPath( path );
        try {
            return svgConfigs.get( configName ).createImage( path );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    protected void createSvgImage( String configName, String path, Callback<ImageDescriptor> callback ) {
        path = mapPath( path );
        try {
            svgConfigs.get( configName ).createImage( path, callback );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    private String mapPath( String path ) {
        if (plugin.getBundle().getResource( path ) == null) {
            path = svgBasePath.get() + path;
        }
        if (plugin.getBundle().getResource( path ) == null) {
            throw new IllegalStateException( "No such SVG file found: " + path );
        }
        return path;
    }


    protected void createSvgImage( String configName, List<String> paths, Callback<Map<String,ImageDescriptor>> callback ) {
        String path;
        for (int i = 0; i < paths.size(); i++) {
            path = mapPath( paths.get( i ) );
            paths.set( i, path );
        }
        try {
            svgConfigs.get( configName ).createImages( paths, callback );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    /**
     * 
     */
    public static abstract class SvgConfiguration {

        protected File             baseTempFolder;

        protected AbstractUIPlugin plugin;


        protected abstract String colorScheme();


        protected abstract Scale scale();


        protected abstract ImageConfiguration imageConfiguration();


        protected File tempFolder() {
            File result = new File( new File( baseTempFolder, colorScheme() ), scale().name() );
            result.mkdirs();
            return result;
        }


        public void createImage( String svgPath, Callback<ImageDescriptor> callback ) throws Exception {
            Runnable bgRunnable = new Runnable() {

                @Override
                public void run() {
                    try {
                        ImageDescriptor imageDescriptor = createImage( svgPath );
                        callback.handle( imageDescriptor );
                    }
                    catch (TranscoderException | IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            Thread bgThread = new Thread( bgRunnable );
            bgThread.setDaemon( true );
            bgThread.start();
        }


        public void createImages( List<String> svgPaths, Callback<Map<String,ImageDescriptor>> callback )
                throws Exception {
            Runnable bgRunnable = new Runnable() {

                @Override
                public void run() {
                    try {
                        createImagesAndNotify( svgPaths, callback );
                    }
                    catch (TranscoderException | IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            Thread bgThread = new Thread( bgRunnable );
            bgThread.setDaemon( true );
            bgThread.start();
        }


        public void createImagesAndNotify( List<String> svgPaths, Callback<Map<String,ImageDescriptor>> callback )
                throws TranscoderException, IOException {
            for (String svgPath : svgPaths) {
                ImageDescriptor result = createImage( svgPath );
                Map<String,ImageDescriptor> map = new HashMap<String,ImageDescriptor>();
                map.put( svgPath, result );
                callback.handle( map );
            }
        }


        public ImageDescriptor createImage( String svgPath ) throws TranscoderException, IOException {
            Timer timer = new Timer();
            File pngFile = new File( tempFolder(), FilenameUtils.getBaseName( svgPath ) + ".png" );
            if (!pngFile.exists()) {
                ImageConfiguration imageConfig = imageConfiguration();
                Scale scale = scale();
                URL svgInput = plugin.getBundle().getResource( svgPath );
                new Svg2Png().transcode( pngFile, svgInput, scale, imageConfig );
                log.info( Thread.currentThread() + ", SVG -> " + pngFile.getAbsolutePath() + " (" + timer.elapsedTime()
                        + "ms)" );
            }
            else {
                log.info( Thread.currentThread() + ", SVG -> " + pngFile.getAbsolutePath() + " (cached)" );
            }
            ImageDescriptor result = ImageDescriptor.createFromURL( pngFile.toURI().toURL() );
            return result;
        }
    }


    /**
     * 
     */
    public static class ReplaceBlackSvgConfiguration
            extends SvgConfiguration {

        private String colorScheme;

        private Scale  scale;

        private RGB    replace;


        public ReplaceBlackSvgConfiguration( RGB replace, int dim ) {
            this.replace = replace;
            this.colorScheme = Joiner.on( "-" ).join( replace.red, replace.green, replace.blue );
            this.scale = Scale.getAsScale( Integer.valueOf( dim ) );
        }


        @Override
        protected String colorScheme() {
            return colorScheme;
        }


        @Override
        protected Scale scale() {
            return scale;
        }


        @Override
        protected ImageConfiguration imageConfiguration() {
            ImageConfiguration imageConfig = new ImageConfiguration();
            imageConfig.setName( colorScheme() );
            imageConfig.setRGB( replace );
            ReplaceConfiguration replaceConfig = new ReplaceConfiguration( new RGB( 0, 0, 0 ), replace );
            imageConfig.getReplaceConfigurations().add( replaceConfig );
            imageConfig.setColorType( COLOR_TYPE.ARGB );
            return imageConfig;
        }
    }
}