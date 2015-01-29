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

import org.polymap.rhei.batik.IApplicationLayouter;
import org.polymap.rhei.batik.internal.BatikComponentFactory;
import org.polymap.rhei.batik.internal.BrowserSizeServiceHandler;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BatikApplication
        implements EntryPoint {

    private static final Log log = LogFactory.getLog( BatikApplication.class );


    /**
     * The {@link Display} of the session of the current thread. Null, if the
     * current thread has no session. The result is equivalent to
     * {@link Display#getCurrent()} except that the calling thread does need to
     * be the UI thread of the session.
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
     * Return an appropriate shell to parent dialogs on. This will be one of the
     * workbench windows (the active one) should any exist. Otherwise
     * <code>null</code> is returned.
     *
     * @return The shell to parent on or <code>null</code> if there is no
     *         appropriate shell.
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
//        ScopedPreferenceStore prefStore = (ScopedPreferenceStore)PrefUtil.getAPIPreferenceStore();
//        String keyPresentationId = IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID;
//        String presentationId = prefStore.getString( keyPresentationId );

        // security config / login
//        Polymap.instance().login();

        // start Atlas UI
//        try {
            display = PlatformUI.createDisplay();
            
            BrowserSizeServiceHandler.current = display;

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
