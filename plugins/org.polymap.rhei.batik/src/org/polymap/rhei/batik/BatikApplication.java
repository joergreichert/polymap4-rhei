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
package org.polymap.rhei.batik;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.PlatformUI;

import org.eclipse.rap.rwt.application.EntryPoint;

import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.app.IAppDesign;
import org.polymap.rhei.batik.app.IAppManager;
import org.polymap.rhei.batik.engine.BatikFactory;

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
        return UIUtils.sessionDisplay();
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
     * Use {@link UIUtils} instead.
     */
    public static Shell shellToParentOn() {
        return UIUtils.shellToParentOn();
    }

    private static Map<Display,BatikApplication> instances = new ConcurrentHashMap();
    
    /**
     * The instance of the current thread/session.
     */
    public static BatikApplication instance() {
        return instances.get( UIUtils.sessionDisplay() );
    }

    // instance *******************************************

    private Display                     display;

    private Shell                       mainWindow;

    private IAppManager                 appManager;

    private IAppDesign                  appDesign;


    public IAppManager getAppManager() {
        return appManager;
    }

    public IAppContext getContext() {
        return getAppManager().getContext();
    }

    public IAppDesign getAppDesign() {
        return appDesign;
    }

    @Override
    public int createUI() {
        display = PlatformUI.createDisplay();

        instances.put( display, this );
        log.info( "Display DPI: " + display.getDPI().x + "x" + display.getDPI().y );

        appManager = BatikFactory.instance().createAppManager();
        appDesign = BatikFactory.instance().createAppDesign();
        try {
            appManager.init();
            appDesign.init();

            mainWindow = appDesign.createMainWindow( display );

            // main loop
            while (!mainWindow.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
            log.info( "Exiting..." );
        }
        finally {
            appDesign.close();
            appManager.close();
        }

        instances.remove( display );
        display.dispose();
        return PlatformUI.RETURN_OK;
    }

}
