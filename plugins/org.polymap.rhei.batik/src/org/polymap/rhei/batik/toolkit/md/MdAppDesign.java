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
package org.polymap.rhei.batik.toolkit.md;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.internal.graphics.ResourceFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.service.ContextProvider;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.engine.DefaultAppDesign;
import org.polymap.rhei.batik.engine.PageStack;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 * Material design for Batik applications.
 * <p/>
 * Consider theme 'org.polymap.rhei.batik.materialDesign' together with this design.
 *
 * @see <a href="http://www.google.com/design/spec/">Material Design</a>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("restriction")
public class MdAppDesign
        extends DefaultAppDesign {

    private static Log log = LogFactory.getLog( MdAppDesign.class );

    /** The basic font face of the application. By default this is "Roboto". */
    public static final String          FONT_FACE = "Roboto";
    
    /** The SVG config to be used for header und toolbar icons. */
    public static final String          TOOLBAR_ICON_CONFIG = SvgImageRegistryHelper.WHITE24;

    
    /**
     * Predefined font styles.
     * 
     * @see <a href=
     *      "http://www.google.com/design/spec/style/typography.html#typography-styles"
     *      >Typography</a>
     */
    enum FontStyle {
        Body1, Body2, Subhead    
    }
    
    
    /**
     * Calculates pixels for the actual display out of <a href=
     * "http://www.google.com/design/spec/layout/units-measurements.html#units-measurements-pixel-density"
     * >density-independent pixels (dp)</a>.
     *
     * @see dp
     * @param dp Number of density-independent pixels
     * @return Number of pixels for the actual display
     */
    public static int dp( int dp ) {
        return ((MdAppDesign)BatikApplication.instance().getAppDesign())._dp( dp );
    }
    

    /**
     * Get a Font for one of the predefined {@link FontStyle}s.
     *
     * @return Shared instance of the font.
     */
    public static Font font( FontStyle style ) {
        return ((MdAppDesign)BatikApplication.instance().getAppDesign()).fonts.get( style );
    }
    
    
    // instance *******************************************
    
    public int                      dpi;
    
    public Map<FontStyle,Font>      fonts = new HashMap( 32 );
    
    
    @Override
    public Shell createMainWindow( @SuppressWarnings("hiding") Display display ) {
        Shell result = super.createMainWindow( display );
        
        // DPI
        log.info( "DPI: " + display.getDPI().x );
        String userAgent = RWT.getRequest().getHeader( "User-Agent" );
        log.info( "User-Agent: " + userAgent );
        dpi = 96;
        boolean mobile = false;
        if (userAgent.toLowerCase().contains( "mobile" )) {
            dpi = 110;
            mobile = true;
        }
        
        // fonts
        ResourceFactory resourceFactory = ContextProvider.getApplicationContext().getResourceFactory();
        fonts.put( FontStyle.Body1, mobile
                ? resourceFactory.getFont( new FontData( FONT_FACE, 14, SWT.NORMAL ) )
                : resourceFactory.getFont( new FontData( FONT_FACE, 13, SWT.NORMAL ) ) );
        fonts.put( FontStyle.Body2, mobile
                ? resourceFactory.getFont( new FontData( FONT_FACE, 14, SWT.BOLD ) )
                : resourceFactory.getFont( new FontData( FONT_FACE, 13, SWT.BOLD ) ) );
        fonts.put( FontStyle.Subhead, mobile
                ? resourceFactory.getFont( new FontData( FONT_FACE, 17, SWT.NORMAL ) )
                : resourceFactory.getFont( new FontData( FONT_FACE, 15, SWT.NORMAL ) ) );

        return result;
    }


    @Override
    public IPanelToolkit createToolkit( PanelPath panelPath ) {
        PageStack<PanelPath>.Page panelParent = panelsArea.getPage( panelPath );
        return new MdToolkit( panelPath, panelParent );
    }
    
    
    public int _dp( int dp ) {
        // 1 dp is 1 pixel on 160dpi screen
        return dp * dpi / 160;
    }
    
}
