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

import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.DefaultString;
import org.polymap.core.ui.ImageRegistryHelper;

import org.polymap.rhei.batik.ant.ImageConfiguration;
import org.polymap.rhei.batik.ant.ImageConfiguration.ReplaceConfiguration;
import org.polymap.rhei.batik.ant.Scale;
import org.polymap.rhei.batik.ant.Svg2Png;
import org.polymap.rhei.batik.ant.Svg2Png.COLOR_TYPE;

/**
 * Provides auto generated PNG icons from SVG source.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SvgImageRegistryHelper
        extends ImageRegistryHelper {

    private static Log log = LogFactory.getLog( SvgImageRegistryHelper.class );
    
    /** Normal image configuration used to create {@link #svgImage(String, String)}. */
    public final static String      NORMAL48 = "normal48";

    /** Normal image configuration used to create {@link #svgImage(String, String)}. */
    public final static String      NORMAL24 = "normal";
    
    /** Image configuration for normale, disabled icons created by {@link #svgImage(String, String)}. */
    public final static String      NORMAL24_DISABLED = "normal-disabled";
    
    /** Image configuration for normale, hovered icons created by {@link #svgImage(String, String)}. */
    public final static String      NORMAL24_HOVERED = "normal-hovered";
    
    
    // instance *******************************************

    @DefaultString( "resources/icons/" )
    public Config2<SvgImageRegistryHelper,String> svgBasePath;
    
    private File                            tempFolder;
    
    private Map<String,SvgConfiguration>    svgConfigs = new HashMap();
    
    
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
        putConfig( NORMAL24, new ReplaceBlackSvgConfiguration( new RGB( 140, 140, 140 ), 24 ) );
        putConfig( NORMAL48, new ReplaceBlackSvgConfiguration( new RGB( 140, 140, 140 ), 48 ) );
    }

    
    public void putConfig( String name, SvgConfiguration config ) {
        config.baseTempFolder = tempFolder;
        config.plugin = plugin;
        svgConfigs.put( name, config );
    }
    
    
    /**
     * 
     *
     * @param path The path to the image inside the bundle. This can be relative to
     *        the bundle root or relative to {@link #svgBasePath}.
     * @param configName
     */
    public Image svgImage( String path, String configName  ) {
        String key = configName + "-" + path;
        Image image = registry.get().get( key );
        if (image == null || image.isDisposed()) {
            registry.get().put( key, createSvgImage( configName, path ) );
            image = registry.get().get( key );
        }
        return image;
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
        if (plugin.getBundle().getResource( path ) == null) {
            path = svgBasePath.get() + path;
        }
        if (plugin.getBundle().getResource( path ) == null) {
            throw new IllegalStateException( "No such SVG file found: " + path );
        }
        
        try {
            return svgConfigs.get( configName ).createImage( path );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    /**
     * 
     */
    public static abstract class SvgConfiguration {
        
        protected File              baseTempFolder;
        
        protected AbstractUIPlugin  plugin;

        
        protected abstract String colorScheme();

        protected abstract Scale scale();

        protected abstract ImageConfiguration imageConfiguration();
        

        protected File tempFolder() {
            File result = new File( new File( baseTempFolder, colorScheme() ), scale().name() );
            result.mkdirs();
            return result;
        }

        
        public ImageDescriptor createImage( String svgPath ) throws TranscoderException, IOException {
            Timer timer = new Timer();
            ImageConfiguration imageConfig = imageConfiguration();
            Scale scale = scale();
            File pngFile = new File( tempFolder(), FilenameUtils.getBaseName( svgPath ) + ".png" );
            URL svgInput = plugin.getBundle().getResource( svgPath );

            new Svg2Png().transcode( pngFile, svgInput, scale, imageConfig );
            ImageDescriptor result = ImageDescriptor.createFromURL( pngFile.toURI().toURL() );
            log.info( "SVG -> " + pngFile.getAbsolutePath() + " (" + timer.elapsedTime() + "ms)" );
            return result;
        }        
    }

    
    /**
     * 
     */
    public static class ReplaceBlackSvgConfiguration
            extends SvgConfiguration {

        private String      colorScheme;
        
        private Scale       scale;

        private RGB         replace;
        
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
