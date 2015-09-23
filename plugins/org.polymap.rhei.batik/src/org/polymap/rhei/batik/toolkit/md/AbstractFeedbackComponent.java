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
package org.polymap.rhei.batik.toolkit.md;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import org.eclipse.rap.rwt.service.ServerPushSession;

import org.polymap.core.runtime.Callback;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.UIUtils;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 */
public abstract class AbstractFeedbackComponent
        extends SelectionAdapter {

    public enum MessageType {
        ERROR("errorbox", "errorlabel"), WARNING("warnbox", "warnlabel"), INFO("infobox", "infolabel"), SUCCESS(
                "successbox", "successlabel");

        private String boxCssName, labelCssName;


        MessageType( String boxCssName, String labelCssName ) {
            this.boxCssName = boxCssName;
            this.labelCssName = labelCssName;
        }


        public String getBoxCssName() {
            return boxCssName;
        }


        public String getLabelCssName() {
            return labelCssName;
        }
    }

    private final Composite comp;

    private final Label     label;

    private final Button    button;

    private Callback<?>     callback = null;


    public AbstractFeedbackComponent( MdToolkit tk, Composite parent, int position, int style ) {
        comp = tk.createComposite( parent, style );
        comp.setLayoutData( FormDataFactory.defaults().top( position ).left( 0 ).right( position + 10 ).bottom( 100 ).create() );
        comp.moveAbove( null );
        FillLayout fillLayout = new FillLayout();
        fillLayout.marginHeight = 10;
        fillLayout.marginWidth = 10;
        comp.setLayout( fillLayout );
        label = tk.createLabel( comp, "", SWT.NONE );
        button = tk.createButton( comp, "", SWT.PUSH );
        button.addSelectionListener( this );
        setVisibleForAll( false );
    }


    private void setVisibleForAll( boolean visible ) {
        setVisibleExceptButton( visible );
        button.setVisible( visible );
    }


    private void setVisibleExceptButton( boolean visible ) {
        comp.setVisible( visible );
        label.setVisible( visible );
    }


    public void showIssue( MessageType messageStyle, String message ) {
        internalShowIssue( messageStyle, message );

        final Display display = Display.getCurrent();

        final ServerPushSession pushSession = new ServerPushSession();
        Runnable bgRunnable = new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep( 2000 );
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                display.asyncExec( new Runnable() {

                    @Override
                    public void run() {
                        if (!label.isDisposed()) {
                            setVisibleForAll( false );
                            pushSession.stop();
                        }
                    }
                } );
            }
        };
        pushSession.start();
        Thread bgThread = new Thread( bgRunnable );
        bgThread.setDaemon( true );
        bgThread.start();
    }


    private void internalShowIssue( MessageType messageStyle, String message ) {
        UIUtils.setVariant( comp, messageStyle.getBoxCssName() );
        UIUtils.setVariant( label, messageStyle.getBoxCssName() );
        label.setText( message );
        button.setVisible( false );
        this.callback = null;
        setVisibleExceptButton( true );
    }


    public void showIssueAndOfferAction( MessageType messageStyle, String message, String actionName,
            @SuppressWarnings("hiding") Callback<?> callback ) {
        internalShowIssue( messageStyle, message );
        button.setText( actionName );
        setVisibleForAll( true );
        this.callback = callback;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events
     * .SelectionEvent)
     */
    @Override
    public void widgetSelected( SelectionEvent e ) {
        if (callback != null) {
            callback.handle( null );
            this.comp.setVisible( false );
        }
    }
}
