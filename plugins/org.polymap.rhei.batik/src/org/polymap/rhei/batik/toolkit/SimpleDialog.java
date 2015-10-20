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
package org.polymap.rhei.batik.toolkit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.ui.UIUtils;

/**
 * 
 * @see <a href="http://www.google.com/design/spec/components/dialogs.html">Material Design</a>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SimpleDialog
        extends Dialog {

    private static Log log = LogFactory.getLog( SimpleDialog.class );

    public static final String              OK_ID = String.valueOf( IDialogConstants.OK_ID );
    public static final String              CANCEL_ID = String.valueOf( IDialogConstants.CANCEL_ID );
    
    public Config2<SimpleDialog,String>     title;

    public Config2<SimpleDialog,Control>    centerOn;

    protected List<IAction>                 actions = new ArrayList();

    private Consumer<Composite>             contentsBuilder;
    
    
    protected SimpleDialog() {
        this( UIUtils.shellToParentOn() );
    }
    
    
    protected SimpleDialog( Shell parentShell ) {
        super( parentShell );
        ConfigurationFactory.inject( this );
    }

    
    /**
     * Sets the content of this dialog. The parent {@link Composite} has
     * {@link FillLayout} set. Change this if needed. The build should add children
     * directyl to the parent without an intermediate Composite.
     *
     * @param builder
     */
    public void setContents( Consumer<Composite> builder ) {
        this.contentsBuilder = builder;
    }


    /**
     * Adds a action to the button bar of this dialog.
     */
    public void addAction( IAction action ) {
        actions.add( action );    
    }


    @Override
    protected void initializeBounds() {
        super.initializeBounds();

        // center
        centerOn.ifPresent( parent -> {
            Shell shell = getShell();
            Rectangle bounds = parent.getBounds ();
            Rectangle rect = shell.getBounds ();
            int x = bounds.x + (bounds.width - rect.width) / 2 ;
            int y = bounds.y + (bounds.height - rect.height) / 2 ;
            shell.setLocation( x, y );
        });
    }


    @Override
    protected void configureShell( Shell newShell ) {
        super.configureShell( newShell );
        // title
        title.ifPresent( t -> newShell.setText( t ) );        
    }

    
    @Override
    protected Control createDialogArea( Composite parent ) {
        assert contentsBuilder != null : "No contents builder! Call setContents() before opening the dialog.";
        Composite area = (Composite)super.createDialogArea( parent );
        
        // allow other than GridLayout
        Composite composite = new Composite( area, SWT.NONE );
        composite.setLayout( new FillLayout( SWT.VERTICAL ) );
        contentsBuilder.accept( composite );
        return area;
    }


    @Override
    protected void createButtonsForButtonBar( Composite parent ) {
        actions.forEach( action -> {
            ((GridLayout)parent.getLayout()).numColumns++;
            
//            assert action.getId() != null : "Dialog actions must have a unique id set.";
//            int id = Integer.parseInt( action.getId() );

            Button btn = new Button( parent, SWT.PUSH );
            UIUtils.setVariant( btn, "dialog" );
            btn.setText( action.getText() );
            btn.setToolTipText( action.getToolTipText() );
//            btn.setData( new Integer( id ) );
            btn.addSelectionListener( new SelectionAdapter() {
                @Override
                public void widgetSelected( SelectionEvent ev ) {
                    action.run();
                }
            } );
//            if (defaultButton) {
//                Shell shell = parent.getShell();
//                if (shell != null) {
//                    shell.setDefaultButton( btn );
//                }
//            }
//            buttons.put( new Integer( id ), button );
            setButtonLayoutData( btn );
        });
    }

    
    @Override
    protected Button createButton( Composite parent, int id, String label, boolean defaultButton ) {
        // increment the number of columns in the button bar
        ((GridLayout)parent.getLayout()).numColumns++;
        Button button = new Button( parent, SWT.PUSH );
        button.setText( label );
        button.setData( new Integer( id ) );
        button.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent event ) {
                buttonPressed( ((Integer)event.widget.getData()).intValue() );
            }
        } );
        if (defaultButton) {
            Shell shell = parent.getShell();
            if (shell != null) {
                shell.setDefaultButton( button );
            }
        }
//        buttons.put( new Integer( id ), button );
        setButtonLayoutData( button );
        return button;
    }

}
