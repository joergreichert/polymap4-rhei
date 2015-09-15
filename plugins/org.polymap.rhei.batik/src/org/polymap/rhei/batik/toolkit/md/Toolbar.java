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
import java.util.Observable;
import java.util.Observer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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

    private Image                     navigationMenuImage  = BatikPlugin.images().svgImage( "ic_menu_48px.svg",
                                                                   NORMAL24 );

    private Image                     moreActionsMenuImage = BatikPlugin.images().svgImage( "ic_more_vert_48px.svg",
                                                                   NORMAL24 );

    private final Composite           comp;

    private List<ActionConfiguration> actions              = new ArrayList<ActionConfiguration>();


    /**
     * 
     * @param tk the Material design toolkit to be used to create specific MD
     *        components
     * @param parent the parent composite of that toolbar to create
     * @param title the title to be shown in this toolbar next to the navigation menu
     * @param fixedPosition if true, the toolbar will be floating when scrolling
     *        down, if false, the toolbar will disappear when scrolling the page down
     * @param style the style configuration for the toolbar
     * @param actions the actions to be shown as buttons (for the first two actions)
     *        resp. menu entries for further actions
     */
    public Toolbar( MdToolkit tk, Composite parent, String title, boolean fixedPosition, int style,
            ActionConfiguration... actions ) {
        this.actions = Arrays.asList( actions );
        comp = createToolbar( tk, parent, fixedPosition, style );
        createNavigationMenu( tk, comp );
        createTitleLabel( title, comp );
        createActionsPanel( tk, comp, this.actions );
    }


    private Composite createActionsPanel( MdToolkit tk, Composite comp, List<ActionConfiguration> actions  ) {
        Composite actionsPanel = tk.createComposite( comp, SWT.NONE );
        actionsPanel.setLayout( new FillLayout( SWT.HORIZONTAL ) );

        createWidgetsForActions( tk, actionsPanel, actions );

        return actionsPanel;
    }


    private Label createTitleLabel( String title, Composite comp  ) {
        Label titleLabel = new Label( comp, SWT.NONE );
        titleLabel.setText( title );
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        titleLabel.setLayoutData( gridData );
        return titleLabel;
    }


    private Button createNavigationMenu( MdToolkit tk, Composite comp  ) {
        Button navigationMenuButton = tk.createButton( comp, "", SWT.NONE );
        navigationMenuButton.setImage( navigationMenuImage );
        // TODO this should show a side bar instead of a menu: 
        // https://www.google.com/design/spec/layout/structure.html#structure-side-nav
//        Menu navigationMenu = new Menu( navigationMenuButton );
//        addDropDownMenuListener( navigationMenuButton, comp, navigationMenu );
//
//        MenuItem navigationMenuDummyItem = new MenuItem( navigationMenu, SWT.NONE );
//        navigationMenuDummyItem.setText( "dummy" );
        return navigationMenuButton;
    }

    /*
     * We could use org.eclipse.swt.widgets.ToolBar instead of creating this composite,
     * but I encounter some issues with applying styles and layouting (e.g. spacing
     * between ToolItems).
     * 
     * http://help.eclipse.org/juno/index.jsp?topic=/org.eclipse.rap.help/help/html/reference/theming/ToolBar.html
     */
    private Composite createToolbar( MdToolkit tk, Composite parent, boolean fixedPosition, int style ) {
        Composite comp = tk.createComposite( parent, style );
        comp.setData( RWT.CUSTOM_VARIANT, "atlas-toolbar" );
        comp.setLayoutData( FormDataFactory.defaults().left(0).right( 100 ).create() );
        if (fixedPosition) {
            comp.moveAbove( null );
        }
        GridLayout gridLayout = new GridLayout( 3, false );
        comp.setLayout( gridLayout );
        return comp;
    }


    private void createWidgetsForActions( MdToolkit tk, Composite actionsPanel, List<ActionConfiguration> actions ) {
        List<ActionConfiguration> sortedActions = sortActionsByPriorityAndName( actions );
        if (actions.size() <= 3) {
            createVisibleActionButtons( tk, actionsPanel, sortedActions );
        }
        else {
            List<ActionConfiguration> visibleActions = sortedActions.subList( 0, 3 );
            createVisibleActionButtons( tk, actionsPanel, visibleActions );
            createMoreActionsButton( tk, actionsPanel, sortedActions );
        }
    }


    private Button createMoreActionsButton( MdToolkit tk, Composite actionsPanel,
            List<ActionConfiguration> sortedActions ) {
        Button moreActionsMenuButton = tk.createButton( actionsPanel, "", SWT.NONE );
        moreActionsMenuButton.setImage( moreActionsMenuImage );
        Menu moreActionsMenu = new Menu( moreActionsMenuButton );
        addDropDownMenuListener( moreActionsMenuButton, comp, moreActionsMenu );

        List<ActionConfiguration> hiddenActions = sortedActions.subList( 3, sortedActions.size() );
        hiddenActions.forEach( action -> {
            MenuItem menuItem = new MenuItem( moreActionsMenu, SWT.NONE );
            if (action.showName.get() == Boolean.TRUE) {
                action.name.ifPresent( text -> menuItem.setText( text ) );
            }
            action.image.ifPresent( image -> menuItem.setImage( image ) );
            Function<Boolean,Void> function = ( Boolean enabled ) -> {
                menuItem.setEnabled( enabled );
                return null;
            };
            boundEnableStateToCallbackAvailability( action, function );
            menuItem.addSelectionListener( triggerCallbackWhenSelected( action ) );
            menuItem.setEnabled( action.getCallback() != null );
        } );
        return moreActionsMenuButton;
    }


    private List<ActionConfiguration> sortActionsByPriorityAndName( List<ActionConfiguration> actions ) {
        return actions.stream().sorted( ( a1, a2 ) -> {
            int prioComp = a1.priority.get().compareTo( a2.priority.get() );
            if (prioComp != 0) {
                return prioComp;
            }
            else {
                return a1.name.get().compareTo( a2.name.get() );
            }
        } ).collect( Collectors.toList() );
    }


    private void boundEnableStateToCallbackAvailability( ActionConfiguration action,
            Function<Boolean,Void> setEnableState ) {
        Observer observer = new Observer() {

            @Override
            public void update( Observable o, Object arg ) {
                setEnableState.apply( arg instanceof Callback<?> );
            }
        };
        action.addObserver( observer );
    }


    private void addDropDownMenuListener( Button button, Composite parent, Menu menu) {
        button.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent event ) {
                Rectangle bounds = button.getBounds();
                Point point = parent.toDisplay( bounds.x - 0, bounds.y + bounds.height );
                menu.setLocation( point );
                menu.setVisible( true );
            }
        } );
    }


    private void createVisibleActionButtons( MdToolkit tk, Composite actionsPanel,
            List<ActionConfiguration> visibleActions ) {
        visibleActions.forEach( action -> {
            Button btn = tk.createButton( actionsPanel, "", SWT.NONE );
            if (action.showName.get() == Boolean.TRUE) {
                action.name.ifPresent( text -> btn.setText( text ) );
            }
            action.image.ifPresent( image -> btn.setImage( image ) );
            action.tooltipText.ifPresent( tooltipText -> btn.setToolTipText( tooltipText ) );
            Function<Boolean,Void> function = ( Boolean enabled ) -> {
                btn.setEnabled( enabled );
                return null;
            };
            boundEnableStateToCallbackAvailability( action, function );
            btn.addSelectionListener( triggerCallbackWhenSelected( action ) );
            btn.setEnabled( action.getCallback() != null );
        } );
    }


    private SelectionAdapter triggerCallbackWhenSelected( ActionConfiguration action ) {
        return new org.eclipse.swt.events.SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                if (action.getCallback() != null) {
                    action.getCallback().handle( null );
                }
            }
        };
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
