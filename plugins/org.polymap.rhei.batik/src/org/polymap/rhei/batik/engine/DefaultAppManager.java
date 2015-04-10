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
package org.polymap.rhei.batik.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.Closer;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.IPanelSite.PanelStatus;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.EventType;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.app.IAppDesign;
import org.polymap.rhei.batik.app.IAppManager;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.LayoutSupplier;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultAppManager
        implements IAppManager {

    private static Log log = LogFactory.getLog( DefaultAppManager.class );

    protected DefaultAppContext         context = new PanelAppContext();

    protected IAppDesign                design;

    /** Cache for panel sites */
    protected Map<PanelPath,PanelSite>  panelSites = new HashMap();
    
    protected PanelPath                 top = PanelPath.ROOT;
    

    @Override
    public void init() {
        design = BatikApplication.instance().getAppDesign();
        
        // open root panel / after main window is created
        UIUtils.sessionDisplay().asyncExec( new Runnable() {
            public void run() {
                openPanel( new PanelIdentifier( "start" ) );
            }
        });
    }


    @Override
    public void close() {
    }


    protected IPanelSite getOrCreateSite( PanelPath path, int stackPriority ) {
        PanelSite result = panelSites.get( path );
        if (result == null) {
            result = new PanelSite( path, stackPriority );
            if (panelSites.put( path, result ) != null) {
                throw new IllegalStateException();
            }
        }
        return result;
    }
    
    
    protected void updatePanelStatus( IPanel panel, PanelStatus panelStatus ) {
        PanelSite panelSite = (PanelSite)panel.getSite();
        PanelStatus previous = panelSite.panelStatus;
        panelSite.panelStatus = panelStatus;
        fireEvent( panel, EventType.LIFECYCLE, panelSite.panelStatus, previous );
    }
    
    
    protected <T> void fireEvent( IPanel panel, EventType eventType, T newValue, T previousValue  ) {
        // XXX avoid race conditions; EventManager does not seem to always handle display events properly
        EventManager.instance().syncPublish( new PanelChangeEvent( panel, eventType, newValue, previousValue ) );        
    }

    
    protected <T extends PanelOp> void runOp( Class<T> type, Consumer<T> initializer ) {
        try {
            T instance = type.newInstance();
            instance.manager = this;
            ConfigurationFactory.inject( instance );
            initializer.accept( instance );
            instance.execute();
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
    
    /**
     * Creates a new {@link IPanel} and its siblings on top of the currently
     * {@link #focusedPanel} and adds them to the context. The panel for the given
     * panelId is then {@link #focusPanel(PanelIdentifier)}.
     *
     * @param panelId
     * @throws IllegalStateException If no panel could be found for the given id.
     */
    @Override
    public IPanel openPanel( final PanelIdentifier panelId ) {
        // create panel and siblings
        runOp( CreatePanelAndSiblingsOp.class, op -> { 
            op.prefix.set( top ); 
            op.panelId.set( panelId );
        });

        // set top 
        top = top.append( panelId );

        // raise top's panel status
        IPanel panel = context.getPanel( top );
        runOp( RaisePanelStatusOp.class, op -> {
            op.panel.set( panel );
            op.targetStatus.set( PanelStatus.FOCUSED );
        });
        return panel;
    }

    
    @Override
    public void hidePanel( PanelPath panelPath ) {
        IPanel panel = context.getPanel( panelPath );
        updatePanelStatus( panel, PanelStatus.INITIALIZED );
    }


    @Override
    public void closePanel( PanelPath panelPath ) {                
        // close all children and siblings
        PanelPath parentPath = panelPath.removeLast( 1 );
        runOp( ClosePanelsOp.class, op -> op.panelPath.set( parentPath ) );

        // set top 
        top = parentPath;

        // raise top's panel status
        IPanel panel = context.getPanel( top );
        runOp( RaisePanelStatusOp.class, op -> {
            op.panel.set( panel );
            op.targetStatus.set( PanelStatus.FOCUSED );
        });
    }

    
    @Override
    public IAppContext getContext() {
        return context;
    }

    
    /**
     * 
     */
    static class UIThreadRunnable
            implements Runnable {
        
        static <T> T exec( boolean async, Callable<T> task ) {
            UIThreadRunnable runnable = new UIThreadRunnable( task );
            if (async) {
                UIUtils.sessionDisplay().asyncExec( runnable );
                return null;
            }
            else if (Display.getCurrent() == null) {
                UIUtils.sessionDisplay().syncExec( runnable );
                return (T)runnable.result;
            }            
            else {
                runnable.run();
                return (T)runnable.result;
            }            
        }
        
        public Callable         delegate;
        public Object           result;

        public UIThreadRunnable( Callable delegate ) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            try {
                result = delegate.call();
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
    }
    

    /**
     * Facade of the global {@link DefaultAppManager#context}.
     */
    class PanelAppContext
            extends DefaultAppContext {
        
        @Override
        public void setUserName( String username ) {
            log.warn( "setUserName(): not implemented yet." );
        }

        @Override
        public void addPreferencesAction( IAction action ) {
            log.warn( "addPreferencesAction(): not implemented yet." );
        }

        @Override
        public IPanel openPanel( final PanelPath panelPath, final PanelIdentifier panelId ) {
            return UIThreadRunnable.exec( false, () -> {
                // close all panels above
                runOp( ClosePanelsOp.class, op -> op.panelPath.set( panelPath ) );
                
                return DefaultAppManager.this.openPanel( panelId );
            });
        }

        @Override
        public void closePanel( final PanelPath panelPath ) {
            UIThreadRunnable.exec( false, () -> { 
                DefaultAppManager.this.closePanel( panelPath ); 
                return null;
            });
        }
    }


    /**
     *
     */
    protected class PanelSite
            implements IPanelSite {

        private PanelPath           path;
        
        private Integer             stackPriority;

        private String              title;
        
        private Image               icon;

        /** Toolbar tools: {@link IAction} or {@link IContributionItem}. */
        private List                tools = new ArrayList();
        
        private IStatus             status = Status.OK_STATUS;
        
        private PanelStatus         panelStatus;
        
        private IPanelToolkit       toolkit;
        
        private int                 preferredWidth = SWT.DEFAULT;


        protected PanelSite( PanelPath path, Integer stackPriority  ) {
            assert path != null;
            this.path = path;
            this.stackPriority = stackPriority;
            this.toolkit = design.createToolkit( path );
        }

        @Override
        protected void finalize() throws Throwable {
            toolkit = Closer.create().closeAndNull( toolkit );
        }

        @Override
        public PanelStatus getPanelStatus() {
            return panelStatus;
        }

        @Override
        public PanelPath getPath() {
            return path;
        }

        @Override        
        public Integer getStackPriority() {
            return stackPriority;
        }

        @Override
        public void setStatus( IStatus status ) {
            IPanel panel = context.getPanel( path );
            IStatus previous = this.status;
            this.status = status;
            fireEvent( panel, EventType.STATUS, this.status, previous );
        }

        @Override
        public IStatus getStatus() {
            return status;
        }

        @Override
        public void addToolbarAction( IAction action ) {
            tools.add( action );
        }

        public List getTools() {
            return tools;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public void setTitle( String title ) {
            String previous = this.title;
            this.title = title;
            IPanel panel = context.getPanel( path );
            if (panel != null) {
                fireEvent( panel, EventType.TITLE, this.title, previous );
            }
            else {
                log.warn( "No panel yet for path: " + path );
            }
        }

        @Override
        public Image getIcon() {
            return icon;
        }
        
        @Override
        public void setIcon( Image icon ) {
            Image previous = this.icon;
            this.icon = icon;
            IPanel panel = context.getPanel( path );
            fireEvent( panel, EventType.TITLE, this.icon, previous );
        }

        @Override
        public IPanelToolkit toolkit() {
            return toolkit;
        }

        @Override
        public void layout( boolean changed ) {
            design.delayedRefresh();
        }

        @Override
        public LayoutSupplier getLayoutPreference() {
            return design.getPanelLayoutPreferences();
        }

        @Override        
        public int getPreferredWidth() {
            return preferredWidth;
        }

        @Override
        public void setPreferredWidth( int preferredWidth ) {
            this.preferredWidth = preferredWidth;
        }
        
    }

}
