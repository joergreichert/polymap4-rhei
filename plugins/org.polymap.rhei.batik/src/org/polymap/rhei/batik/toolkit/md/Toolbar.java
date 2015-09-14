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

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.polymap.core.runtime.Callback;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.rhei.batik.BatikPlugin;

/**
 * 
 * In order to <a
 * href=http://www.google.com/design/spec/layout/structure.html#structure
 * -ui-regions">structuring UI region</a> <a
 * href="http://www.google.com/design/spec/components/toolbars.html">Toolbars</a>
 * should offer page and/or app specific actions
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class Toolbar
        extends Configurable {

    private Image                  navigationMenuImage   = BatikPlugin.images().svgImage( "ic_menu_48px.svg", NORMAL24 );

    private Image                  moreActionsMenuImage  = BatikPlugin.images().svgImage( "ic_more_vert_48px.svg",
                                                                 NORMAL24 );

    @org.polymap.core.runtime.config.Mandatory
    public Config2<Toolbar,String> title;

    private final Composite        comp;

    private final Button           navigationMenuButton;

    private final Menu             navigationMenu;

    // or combo box showing the current page
    private final Label            titleLabel;

    private final Composite        actionsPanel;

    private Button                 moreActionsMenuButton = null;

    private Menu                   moreActionsMenu       = null;


    public static class ActionConfiguration
            extends Configurable {

        @org.polymap.core.runtime.config.Mandatory
        public Config2<ActionConfiguration,String>          name;

        @org.polymap.core.runtime.config.Mandatory
        public Config2<ActionConfiguration,Boolean>         showName;

        public Config2<ActionConfiguration,Image> image;

        @org.polymap.core.runtime.config.Mandatory
        public Config2<ActionConfiguration,Callback<?>>     callback;

        public Config2<ActionConfiguration,Integer>         priority;


        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals( Object obj ) {
            if (!(obj instanceof ActionConfiguration))
                return false;
            return this.toString().equals( obj.toString() );
        }


        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return name.orElse( "<unknown>" );
        }
    }

    private List<ActionConfiguration> actions = new ArrayList<ActionConfiguration>();


    /**
     * 
     */
    public Toolbar( MdToolkit tk, Composite parent, boolean fixedPosition, int style, ActionConfiguration... actions ) {
        this.actions = Arrays.asList( actions);
        comp = tk.createComposite( parent, style );
        comp.setLayoutData( FormDataFactory.defaults().top( 5 ).left( 0 ).right( 100 ).create() );
        if (fixedPosition) {
            comp.moveAbove( null );
        }
        GridLayout gridLayout = new GridLayout( 3, false );
        comp.setLayout( gridLayout );

        navigationMenuButton = tk.createButton( comp, "", SWT.NONE );
        navigationMenuButton.setImage( navigationMenuImage );
        navigationMenu = new Menu( navigationMenuButton );

        titleLabel = new Label( comp, SWT.NONE );
        title.ifPresent( title -> titleLabel.setText( title ) );
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        titleLabel.setLayoutData( gridData );

        actionsPanel = tk.createComposite( comp, SWT.NONE );
        FillLayout fillLayout2 = new FillLayout( SWT.HORIZONTAL );
        actionsPanel.setLayout( fillLayout2 );

        List<ActionConfiguration> sortedActions = Arrays.asList( actions ).stream().sorted( ( a1, a2 ) -> {
            int prioComp = a1.priority.get().compareTo( a2.priority.get() );
            if (prioComp != 0)
                return prioComp;
            else
                return a1.name.get().compareTo( a2.name.get() );
        } ).collect( Collectors.toList() );

        if (actions.length <= 3) {
            createVisibleActionButtons( tk, sortedActions );
        }
        else {
            List<ActionConfiguration> visibleActions = sortedActions.subList( 0, 3 );
            createVisibleActionButtons( tk, visibleActions );

            moreActionsMenuButton = tk.createButton( actionsPanel, "", SWT.NONE );
            moreActionsMenuButton.setImage( moreActionsMenuImage );
            moreActionsMenu = new Menu( moreActionsMenuButton );

            List<ActionConfiguration> hiddenActions = sortedActions.subList( 3, sortedActions.size() );
            hiddenActions.forEach( action -> {
                MenuItem menuItem = new MenuItem( moreActionsMenu, SWT.NONE );
                action.name.ifPresent( text -> menuItem.setText( text ) );
                action.image.ifPresent( image -> menuItem.setImage( image ) );
                menuItem.addSelectionListener( new org.eclipse.swt.events.SelectionAdapter() {

                    public void widgetSelected( SelectionEvent e ) {
                        menuItem.setEnabled( action.callback.isPresent() );
                        action.callback.ifPresent( callback -> callback.handle( null ) );
                    }
                } );
            } );
        }
    }


    private void createVisibleActionButtons( MdToolkit tk, List<ActionConfiguration> visibleActions ) {
        visibleActions.forEach( action -> {
            Button btn = tk.createButton( actionsPanel, "", SWT.NONE );
            action.name.ifPresent( text -> btn.setText( text ) );
            action.image.ifPresent( image -> btn.setImage( image ) );
            btn.addSelectionListener( new org.eclipse.swt.events.SelectionAdapter() {

                public void widgetSelected( SelectionEvent e ) {
                    btn.setEnabled( action.callback.isPresent() );
                    action.callback.ifPresent( callback -> callback.handle( null ) );
                }
            } );
        } );
    }


    /**
     * @return
     */
    public Control getControl() {
        return comp;
    }


    public List<ActionConfiguration> getActionConfigurations() {
        return actions;
    }
}
