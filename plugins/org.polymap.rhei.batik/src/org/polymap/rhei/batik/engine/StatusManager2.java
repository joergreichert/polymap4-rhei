/* 
 * polymap.org
 * Copyright (C) 2013-2015, Falko Bräutigam. All rights reserved.
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

import static com.google.common.collect.Iterables.tryFind;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.ProgressProvider;
import org.eclipse.rap.ui.internal.progress.JobManagerAdapter;

import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.EventType;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("restriction")
public class StatusManager2
        extends ContributionItem
        implements IStatusLineManager, IPropertyChangeListener {

    private static Log log = LogFactory.getLog( StatusManager2.class );
    
    private static final int                    MESSAGE_HIDE_TIMEOUT = 8000;
    
    private static final int                    FOREVER = 3600 * 1000;
    
    private static Map<Display,StatusManager2>   instances = Collections.synchronizedMap( new WeakHashMap() );
    
    static {
        Job.getJobManager().setProgressProvider( new ProgressProvider() {
            @Override
            public IProgressMonitor createMonitor( Job job ) {
                // get the StatusManager for the UIJob; 
                // XXX no other Job types are supported from within Batik!
                StatusManager2 manager = null;
                if (job instanceof UIJob) {
                    Display display = ((UIJob)job).getDisplay();
                    manager = instances.get( display );
                }
                
                if (manager != null) {
                    return manager.getProgressMonitor();
                }
                // fallback: workbench; XXX check if workbench is running
                else {
                    return JobManagerAdapter.getInstance().createMonitor( job );                    
                }
//                else {
//                    return new NullProgressMonitor() {
//                        @Override
//                        public void beginTask( String name, int totalWork ) {
//                            log.info( "TASK: " + name + "..." );
//                        }
//                        @Override
//                        public void subTask( String name ) {
//                            log.info( "SUB TASK: " + name + "..." );
//                        }
//                        @Override
//                        public void worked( int work ) {
//                            System.out.print( "." );
//                        }
//                    };
//                }
            }
        });
    }
    
    // instance *******************************************
    
    private DefaultAppManager       appManager;

    private Composite               contents;

    private List<PanelChangeEvent>  pendingStartEvents = new ArrayList();

    private IPanel                  activePanel;

    /** Null if there is no popup displayed currently. */
    private volatile StatusPopup    popup;
    
    /** The status of the currently active panel set by {@link #panelChanged(PanelChangeEvent)}. */
    private IStatus                 panelStatus;
    
    /** The tools/actions of the currently active panel set by {@link #panelChanged(PanelChangeEvent)}. */
    private List                    panelTools;

    private ContributionManager     contribManager = new ContributionManager() {
        public void update( boolean force ) {
            throw new RuntimeException( "not yet implemented." );
        }
    };

    
    public StatusManager2( DefaultAppManager appManager ) {
        this.appManager = appManager;

        instances.put( UIUtils.sessionDisplay(), this );
        
        // register panelChanged()
        appManager.getContext().addListener( this, new EventFilter<PanelChangeEvent>() {
            public boolean apply( PanelChangeEvent ev ) {
                return ev.getType() == EventType.ACTIVATED 
                        || ev.getType() == TYPE.ACTIVATING 
                        || ev.getType() == TYPE.DEACTIVATING 
                        || ev.getType() == TYPE.STATUS;
            }
        });
    }


    protected void createContents( Composite parent ) {
        this.contents = parent;
        
        for (PanelChangeEvent ev : pendingStartEvents) {
            panelChanged( ev );
        }
        pendingStartEvents.clear();
    }
    
    
    @EventHandler(display=true)
    protected void panelChanged( PanelChangeEvent ev ) {
        if (contents == null) {
            pendingStartEvents.add( ev );
            return;
        }
        if (ev.getType() == TYPE.ACTIVATING) {
            // deactivate current panel *** 
            if (popup != null) {
                popup.close();
            }
            // remove property listener
            if (activePanel != null) {
                for (Object tool : ((DefaultPanelSite)activePanel.getSite()).getTools() ) {
                    ((IAction)tool).removePropertyChangeListener( this );
                }
            }
            
            // activate new panel ***
            activePanel = ev.getSource();            
            panelStatus = activePanel.getSite().getStatus();
            panelTools = ((DesktopPanelSite)activePanel.getSite()).getTools();

            // register property listener
            for (Object tool : ((DesktopPanelSite)activePanel.getSite()).getTools() ) {
                ((IAction)tool).addPropertyChangeListener( this );
            }
        }
        else if (ev.getType() == TYPE.ACTIVATED) {
        }
        else if (ev.getType() == TYPE.DEACTIVATING) {
            // XXX in develop-rap1.5 branch DEACTIVATING event is not always send properly
        }
        else if (ev.getType() == TYPE.STATUS) {
            panelStatus = activePanel.getSite().getStatus();
            log.debug( "Panel status changed to: " + panelStatus );
            checkUpdatePopup();
        }
    }


    /** 
     * Called when a tool has been changed. 
     */
    @Override
    public void propertyChange( PropertyChangeEvent ev ) {
        checkUpdatePopup();
    }

    
    private IStatus displayedStatus;

    /**
     * Checks if the {@link #popup} should be visible, create new one if necessary
     * and update UI.
     */
    protected void checkUpdatePopup() {
        boolean anyToolEnabled = false;
        for (Object tool : panelTools) {
            IAction action = (IAction)tool;
            if (action.isEnabled()) {
                anyToolEnabled = true;
                break;
            }
        }
        
        boolean statusNotEmpty = panelStatus != null 
                && panelStatus != displayedStatus
                && panelStatus != Status.OK_STATUS;
        log.debug( "statusNotEmpty=" + statusNotEmpty + ", anyToolEnabled=" + anyToolEnabled );
        
        if (statusNotEmpty || anyToolEnabled) {
            if (popup == null) {
                popup = new StatusPopup();
                log.info( "   -> popup opened" );
            }
            popup.update();
            displayedStatus = panelStatus;

            // auto hide
            if (panelStatus.isOK() && !anyToolEnabled) {
                popup.getShell().getDisplay().timerExec( 8000, new Runnable() {
                    public void run() {
                        checkUpdatePopup();
                    }
                });
            }
        }
        else if (popup != null) {
            popup.close();
            log.debug( "   -> popup closed" );
        }
    }

    
    /**
     * The popup provides the UI presenting the current state (status, tools,
     * progress) to the user. The popup is initialized only when it is visible.
     */
    class StatusPopup
            extends PopupDialog {

        private Label                   messageLabel;

        private Label                   messageIcon;
        
        private Composite               toolsContainer;
        
        private Composite               progressContainer;
        
        private ProgressIndicator       progress;

        private Label                   progressLabel;

        private Label                   toolsSep;

        private Label                   progressSep;

        
        public StatusPopup() {
            super( BatikApplication.shellToParentOn(), SWT.NO_FOCUS, 
                    false, false, false, false, false, null, null );
            StatusManager2.this.popup = this;
            open();
        }

        @Override
        public boolean close() {
            try { 
                return super.close(); 
            }
            finally { 
                messageLabel = messageIcon = progressLabel = toolsSep = progressSep = null;
                toolsContainer = progressContainer = null;
                progress = null;

                StatusManager2.this.popup = null; 
            }
        }

        @Override
        protected void configureShell( Shell shell ) {
            super.configureShell( shell );
            shell.setLayout( FormLayoutFactory.defaults().spacing( 5 ).margins( 8, 0 ).create() );
            shell.setData( WidgetUtil.CUSTOM_VARIANT, "batik-status-tooltip" );
        }
        
        @Override
        protected Control createContents( Composite popupParent ) {
            // status/message
            messageIcon = new Label( popupParent, SWT.NO_FOCUS );
            messageIcon.setLayoutData( FormDataFactory.defaults().top( 0, 3 ).left( 0 ).create() );
            messageIcon.setData( WidgetUtil.CUSTOM_VARIANT, "batik-status-tooltip-icon" );

            messageLabel = new Label( popupParent, SWT.NO_FOCUS );
            messageLabel.setLayoutData( FormDataFactory.filled().top( 0, 7 ).left( messageIcon).clearRight().create() );
            messageLabel.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
            //messageLabel.setData( MarkupValidator.MARKUP_VALIDATION_DISABLED, Boolean.TRUE );
            //messageLabel.setData( WidgetUtil.CUSTOM_VARIANT, "batik-status-tooltip-message" );

            // actions
            toolsSep = new Label( popupParent, SWT.SEPARATOR | SWT.VERTICAL | SWT.NO_FOCUS );
            toolsSep.setLayoutData( FormDataFactory.filled().width( 0 ).create() );
            toolsContainer = new Composite( popupParent, SWT.NO_FOCUS );
            toolsContainer.setLayoutData( FormDataFactory.filled().width( 0 ).create() );
            toolsContainer.setLayout( RowLayoutFactory.fillDefaults().spacing( 3 ).create() );

            // progress
            progressSep = new Label( popupParent, SWT.SEPARATOR | SWT.VERTICAL | SWT.NO_FOCUS );
            progressSep.setLayoutData( FormDataFactory.filled().width( 0 ).create() );
            progressSep.setVisible( false );
            progressContainer = new Composite( popupParent, SWT.NO_FOCUS );
            progressContainer.setLayoutData( FormDataFactory.filled().width( 0 ).create() );
            progressContainer.setLayout( RowLayoutFactory.fillDefaults().spacing( 3 ).create() );
            progressContainer.setVisible( false );

            progressLabel = new Label( progressContainer, SWT.NO_FOCUS );
            progressLabel.setText( "" );
            progress = new ProgressIndicator( progressContainer );
            progress.setLayoutData( RowDataFactory.swtDefaults().hint( 75, SWT.DEFAULT ).create() );
                        
            return popupParent;
        }
        
        /**
         * Updates the UI according to the current panel status (message, tools, progress).
         */
        protected void update() {
            updateStatusMessage();
            updateTools();
            
            // update popup location/size *after* components were updated
            final Shell popupShell = popup.getShell();
            final Display display = popupShell.getDisplay();
            display.asyncExec( new Runnable() {
                @Override
                public void run() {
                    if (!popupShell.isDisposed()) {
                        popupShell.layout( true );
                        Point popupSize = popupShell.computeSize( SWT.DEFAULT, 35 );
                        int displayWidth = display.getClientArea().width;

                        int x = Math.max( 5, (displayWidth-popupSize.x)/2 );
                        popupShell.setBounds( x, 80, popupSize.x, 35 );
                    }
                }                
            });
        }
        
        protected void updateStatusMessage() {
            // process markdown
            final String msg = panelStatus.getMessage();
            //msg = msg != null ? new PegDownProcessor().markdownToHtml( status.getMessage() ) : null;

            if (panelStatus.getSeverity() == IStatus.OK) {
                updateMessage( msg, msg, BatikPlugin.instance().imageForName( "resources/icons/ok-status.gif" ) );
            }
            else if (panelStatus.getSeverity() == IStatus.ERROR) {
                updateMessage( msg, msg, BatikPlugin.instance().imageForName( "resources/icons/field_invalid.gif" ) );
            }
            else if (panelStatus.getSeverity() == IStatus.WARNING) {
                updateMessage( msg, msg, BatikPlugin.instance().imageForName( "resources/icons/warningstate.gif" ) );
            }
            else if (panelStatus.getSeverity() == IStatus.INFO) {
                updateMessage( msg, msg, BatikPlugin.instance().imageForName( "resources/icons/info.png" ) );
            }
            else {
                throw new RuntimeException( "Unhandled status severity: " + panelStatus.getSeverity() );
            }
        }

        private void updateMessage( String msg, String tooltip, Image icon ) {
            messageIcon.setImage( icon );
            messageIcon.setToolTipText( tooltip );
            messageLabel.setText( msg );
        }

        protected void updateTools() {
            // clear children
            for (Control child : toolsContainer.getChildren()) {
                child.dispose();
            }
            
            // create UI updates for every tool
            for (final Object tool : panelTools) {
                // IAction
                if (tool instanceof IAction) {
                    final IAction action = (IAction)tool;
                    // find the btn or create a new one
                    final Control btn = tryFind( asList( toolsContainer.getChildren() ), new Predicate<Control>() {
                        public boolean apply( Control input ) {
                            return input.getData( "tool" ) == tool;
                        }
                    }).or( action.getStyle() == IAction.AS_CHECK_BOX
                            ? new Button( toolsContainer, SWT.CHECK | SWT.NO_FOCUS )
                            : new Button( toolsContainer, SWT.PUSH | SWT.NO_FOCUS ) );

                    ((Button)btn).addSelectionListener( new SelectionAdapter() {
                        public void widgetSelected( SelectionEvent se ) {
                            action.run();
                        }
                    });

                    //btn.setLayoutData( RowDataFactory.swtDefaults().hint( SWT.DEFAULT, 28 ).create() );
                    updateToolButton( action, (Button)btn );
                    btn.setData( "tool", tool );
                }
                else {
                    throw new RuntimeException( "Panel toolbar item type: " + tool );
                }
            }

            // adjust layout data
            if (popup != null) {
                FormDataFactory toolsSepData = FormDataFactory.filled().left( messageLabel ).clearRight();
                FormDataFactory toolsContainerData = FormDataFactory.filled().left( toolsSep ).clearRight();
                if (panelTools.isEmpty()) {
                    toolsSepData.width( 0 );
                    toolsContainerData.width( 0 );
                }
                toolsSep.setLayoutData( toolsSepData.create() );
                toolsContainer.setLayoutData( toolsContainerData.create() );
            }
        }
        
        private void updateToolButton( IAction action, Button btn ) {
            //btn.setData( WidgetUtil.CUSTOM_VARIANT, "atlas-toolbar"  );
            if (action.getDescription() == IPanelSite.SUBMIT) {
                btn.setData( WidgetUtil.CUSTOM_VARIANT, "mosaic-case-submit" );
            }
            btn.setEnabled( action.isEnabled() );

            if (action.getText() != null) {
                btn.setText( action.getText() );
            }
            if (action.getToolTipText() != null) {
                btn.setToolTipText( action.getToolTipText() );
            }
            ImageDescriptor image = action.getImageDescriptor();
            if (image != null) {
                btn.setImage( BatikPlugin.instance().imageForDescriptor( image, action.getText() + "_icon" ) );
            }
        }
        
    };

    
    // IStatusLineManager *********************************
    
    /**
     * 
     */
    class StatusProgressMonitor
            extends NullProgressMonitor {
        
        private String          taskName;
        
        private String          subTaskName;
        
        private int             totalWork;
        
        private int             worked;

        protected void updateProgress() {
//            FormDataFactory progressSepData = FormDataFactory.filled().left( toolsContainer ).clearRight();
//            FormDataFactory progressContainerData = FormDataFactory.filled().top( 0, 5 ).left( progressSep );
//            
//            if (taskName == null) {
//                progressSepData.width( 0 );
//                progressContainerData.width( 0 );
//            }
//            progressSep.setLayoutData( progressSepData.create() );
//            progressContainer.setLayoutData( progressContainerData.create() );
        }

        @Override
        public void beginTask( String name, int total ) {
            this.taskName = name;
            this.totalWork = total;
            this.worked = 0;
            log.info( "TASK: " + name + "..." );
            
//            popup.progressContainer.getDisplay().asyncExec( new Runnable() {
//                public void run() {
//                    popup.progressLabel.setText( StringUtils.abbreviate( taskName, 20 ) );
//                    if (totalWork == IProgressMonitor.UNKNOWN) {
//                        popup.progress.beginAnimatedTask();
//                    }
//                    else {
//                        popup.progress.beginTask( totalWork );
//                    }
//                    updateProgress();
//                    displayPopup( popup.progress, FOREVER );
//                }
//            });
        }

        @Override
        public void subTask( String name ) {
            this.subTaskName = name;
            log.info( "SUB TASK: " + name + "..." );
//            popup.progressContainer.getDisplay().asyncExec( new Runnable() {
//                public void run() {
//                    popup.progressLabel.setText( StringUtils.abbreviate( subTaskName, 20 ) );
//                    popup.progressLabel.getParent().layout( true );
//                }
//            });
        }

        @Override
        public void done() {
            taskName = null;
            subTaskName = null;
            totalWork = 0;
            worked = 0;
            
//            popup.progressContainer.getDisplay().asyncExec( new Runnable() {
//                public void run() {
//                    popup.progressLabel.setText( "Fertig" );
//                    popup.progress.sendRemainingWork();
//
//                    updateProgress();
//                    displayPopup( popup.progress, 1000 );
//                }
//            });
        }
        
        @Override
        public void setTaskName( String name ) {
            this.taskName = name;
//            popup.progressContainer.getDisplay().asyncExec( new Runnable() {
//                public void run() {
//                    popup.progressLabel.setText( StringUtils.abbreviate( taskName, 10 ) );
//                    popup.getShell().layout( true );
//                }
//            });
        }
        
        @Override
        public void worked( final int work ) {
            worked += work;
//            popup.progressContainer.getDisplay().asyncExec( new Runnable() {
//                public void run() {
//                    popup.progress.worked( work );
//                    popup.getShell().layout( true );
//                }
//            });
        }
    }
    
    
    @Override
    public IProgressMonitor getProgressMonitor() {
        return new StatusProgressMonitor();
    }

    @Override
    public boolean isCancelEnabled() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void setCancelEnabled( boolean enabled ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void setErrorMessage( String message ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void setErrorMessage( Image image, String message ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void setMessage( String message ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void setMessage( Image image, String message ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    
    // ContributionItem ***********************************
    
    @Override
    public void fill( Composite parent ) {
        createContents( parent );
    }

    
    // ContributionManager ********************************
    
    public void add( IAction action ) {
        contribManager.add( action );
    }

    public void add( IContributionItem item ) {
        contribManager.add( item );
    }

    public void appendToGroup( String groupName, IAction action ) {
        contribManager.appendToGroup( groupName, action );
    }

    public void appendToGroup( String groupName, IContributionItem item ) {
        contribManager.appendToGroup( groupName, item );
    }

    public IContributionItem find( String id ) {
        return contribManager.find( id );
    }

    public IContributionItem[] getItems() {
        return contribManager.getItems();
    }

    public int getSize() {
        return contribManager.getSize();
    }

    public IContributionManagerOverrides getOverrides() {
        return contribManager.getOverrides();
    }

    public void update( boolean force ) {
        contribManager.update( force );
    }

    public int indexOf( String id ) {
        return contribManager.indexOf( id );
    }

    public void insert( int index, IContributionItem item ) {
        contribManager.insert( index, item );
    }

    public void insertAfter( String ID, IAction action ) {
        contribManager.insertAfter( ID, action );
    }

    public void insertAfter( String ID, IContributionItem item ) {
        contribManager.insertAfter( ID, item );
    }

    public void insertBefore( String ID, IAction action ) {
        contribManager.insertBefore( ID, action );
    }

    public void insertBefore( String ID, IContributionItem item ) {
        contribManager.insertBefore( ID, item );
    }

    public boolean isDirty() {
        return contribManager.isDirty();
    }

    public boolean isEmpty() {
        return contribManager.isEmpty();
    }

    public void markDirty() {
        contribManager.markDirty();
    }

    public void prependToGroup( String groupName, IAction action ) {
        contribManager.prependToGroup( groupName, action );
    }

    public void prependToGroup( String groupName, IContributionItem item ) {
        contribManager.prependToGroup( groupName, item );
    }

    public IContributionItem remove( String ID ) {
        return contribManager.remove( ID );
    }

    public IContributionItem remove( IContributionItem item ) {
        return contribManager.remove( item );
    }

    public void removeAll() {
        contribManager.removeAll();
    }

    public boolean replaceItem( String identifier, IContributionItem replacementItem ) {
        return contribManager.replaceItem( identifier, replacementItem );
    }

    public void setOverrides( IContributionManagerOverrides newOverrides ) {
        contribManager.setOverrides( newOverrides );
    }

}
