/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.IMessageProvider;

import org.polymap.core.security.AbstractLoginDialog;

/**
 * Handles the callbacks to show an UI for the LoginModule.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LoginDialog
        extends AbstractLoginDialog {

//    private static final ImageDescriptor titleImageDescriptor = 
//            CorePlugin.imageDescriptor( "icons/polymap_logo2_app.png" ); //$NON-NLS-1$


    public LoginDialog() {
        this( Display.getDefault().getActiveShell() );
    }


    protected LoginDialog( Shell parentShell ) {
        super( parentShell );
    }


    protected Point getInitialSize() {
        return new Point( 320, 220 );
    }


    protected Control createDialogArea( Composite parent ) {
        Composite dialogarea = new Composite( parent, SWT.NONE );
        dialogarea.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        
        createCallbackHandlers( dialogarea );

        // Layout
        FormLayout layout = new FormLayout();
        layout.marginWidth = 5;
        layout.marginHeight = 5;
        dialogarea.setLayout( layout );
        
//        setTitleImage( titleImageDescriptor.createImage() );
        
        return dialogarea;
    }


    private void createCallbackHandlers( Composite composite ) {
        Callback[] callbacks = getCallbacks();
        for (int i = 0; i < callbacks.length; i++) {
            Callback callback = callbacks[i];
            if (callback instanceof TextOutputCallback) {
                createTextoutputHandler( composite, (TextOutputCallback)callback );
            }
            else if (callback instanceof NameCallback) {
                createNameHandler( composite, (NameCallback)callback );
            }
            else if (callback instanceof PasswordCallback) {
                createPasswordHandler( composite, (PasswordCallback)callback );
            }
        }
    }


    private void createPasswordHandler( Composite composite, final PasswordCallback callback ) {
        Label label = new Label( composite, SWT.NONE );
        label.setText( callback.getPrompt() );
        final Text passwordText = new Text( composite, SWT.SINGLE | SWT.LEAD | SWT.PASSWORD
                | SWT.BORDER );
        
        passwordText.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent event ) {
                callback.setPassword( passwordText.getText().toCharArray() );
            }
        } );

        // layout
        FormData labelData = new FormData( 80, SWT.DEFAULT );
        labelData.top = new FormAttachment( 0, 32 );
        labelData.left = new FormAttachment( 0, 0 );
        //labelData.right = new FormAttachment( 100, 0);
        label.setLayoutData( labelData );

        FormData textData = new FormData();
        textData.top = new FormAttachment( 0, 30 );
        textData.left = new FormAttachment( label, 5 );
        textData.right = new FormAttachment( 100, 0 );
        passwordText.setLayoutData( textData );
    }


    private void createNameHandler( Composite composite, final NameCallback callback ) {
        Label label = new Label( composite, SWT.NONE );
        label.setText( callback.getPrompt() );
        final Text text = new Text( composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER );
        text.setFocus();

        text.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent event ) {
                callback.setName( text.getText() );
            }
        } );
        
        // layout
        FormData labelData = new FormData( 80, SWT.DEFAULT );
        labelData.top = new FormAttachment( 0, 2 );
        labelData.left = new FormAttachment( 0, 0 );
        //labelData.right = new FormAttachment( 100, 0);
        label.setLayoutData( labelData );

        FormData textData = new FormData();
        textData.top = new FormAttachment( 0, 0 );
        textData.left = new FormAttachment( label, 5 );
        textData.right = new FormAttachment( 100, 0 );
        text.setLayoutData( textData );
    }


    private void createTextoutputHandler( Composite composite, TextOutputCallback callback ) {
        int messageType = callback.getMessageType();
        int dialogMessageType = IMessageProvider.NONE;
        switch (messageType) {
            case TextOutputCallback.INFORMATION:
                dialogMessageType = IMessageProvider.INFORMATION;
                break;
            case TextOutputCallback.WARNING:
                dialogMessageType = IMessageProvider.WARNING;
                break;
            case TextOutputCallback.ERROR:
                dialogMessageType = IMessageProvider.ERROR;
                break;
        }
        setMessage( callback.getMessage(), dialogMessageType );
    }


    public void internalHandle() {
    }
    
}
