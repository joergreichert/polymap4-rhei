/*
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.window.Window;

import org.eclipse.ui.PlatformUI;

import org.eclipse.rap.rwt.application.EntryPoint;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.IApplicationLayouter;
import org.polymap.rhei.batik.internal.BatikComponentFactory;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BatikApplication
        implements EntryPoint {

    private static final Log log = LogFactory.getLog( BatikApplication.class );


    /**
     * @deprecated Use {@link UIUtils} instead.
     */
    public static Display sessionDisplay() {
        return Polymap.getSessionDisplay();
    }

//    public Point displayDPI() {
//        return sessionDisplay().getDPI();
//    }
//
//    public static Point toPixel() {
//        PixelConverter pc = new PixelConverter( JFaceResources.getDefaultFont() );
//        int width100 = pc.convertWidthInCharsToPixels( 100 );
//        int height100 = pc.convertHeightInCharsToPixels( 100 );
//        return new Point( width100, height100 );
//    }
    
    
    /**
     * @deprecated Use {@link UIUtils} instead.
     */
    public static Shell shellToParentOn() {
        return Polymap.getSessionDisplay().getActiveShell();
    }


    // instance *******************************************

    private Display                     display;

    private IApplicationLayouter        appLayouter;

    private Window                      mainWindow;


    @Override
    public int createUI() {
        // security config / login
//        Polymap.instance().login();

        // start Atlas UI
//        try {
            display = PlatformUI.createDisplay();
            
            log.info( "Display DPI: " + display.getDPI().x + "x" + display.getDPI().y );

            appLayouter = BatikComponentFactory.instance().createApplicationLayouter();
            mainWindow = appLayouter.initMainWindow( display );
            mainWindow.open();

            Shell shell = mainWindow.getShell();
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
            log.info( "Exiting..." );
            display.dispose();
            return PlatformUI.RETURN_OK;
//        }
//        catch (Exception e) {
//            //
//            handleError( CorePlugin.PLUGIN_ID, e.getLocalizedMessage(), e );
//            return PlatformUI.RETURN_OK;
//        }
    }

}
