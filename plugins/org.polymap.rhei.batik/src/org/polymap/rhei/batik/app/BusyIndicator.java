/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.toolkit.IBusyIndicator;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BusyIndicator
        implements IBusyIndicator {

    private static Log log = LogFactory.getLog( BusyIndicator.class );
    
    private Composite           parent;


    public BusyIndicator( Composite parent ) {
        this.parent = parent;
    }

    @Override
    public void showWhile( final Runnable task ) {
        try {
            showWhile( new Callable() {
                public Object call() throws Exception {
                    task.run(); return null;
                }
            });
        }
        catch (Exception e) {
            throw new RuntimeException( "Should never happen.", e );
        }
    }

    @Override
    public <T> T showWhile( final Callable<T> task ) throws Exception {
        start();
        
        final AtomicReference resultRef = new AtomicReference();
        display().asyncExec( new Runnable() {
            public void run() {
                try {
                    resultRef.set( task.call() );
                }
                catch (Exception e) {
                    resultRef.set( e );
                }
                finally {
                    end();
                }
            }
        });
        if (resultRef.get() instanceof Exception) {
            throw (Exception)resultRef.get();
        }
        else {
            return (T)resultRef.get();
        }
    }

    protected Display display() {
        return BatikApplication.shellToParentOn().getDisplay();
    }
    
    protected void start() {
        assert parent == null : "Non null parents are not supported yet.";
        display().asyncExec( new Runnable() {
            public void run() {
                BatikApplication.shellToParentOn().setEnabled( false );
            }
        });
//        while (display().readAndDispatch()) {
//            log.info( "readAndDispatch() ..." );
//        }
    }

    protected void end() {
        BatikApplication.shellToParentOn().setEnabled( true );        
    }
    
}
