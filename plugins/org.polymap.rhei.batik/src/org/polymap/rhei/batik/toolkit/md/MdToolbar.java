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

import static org.polymap.rhei.batik.BatikPlugin.images;
import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.polymap.core.ui.UIUtils;

/**
 * 
 * @see <a href="http://www.google.com/design/spec/components/toolbars.html">Toolbars</a>
 * @see <a href="http://www.google.com/design/spec/layout/structure.html#structure-ui-regions">structuring UI region</a>
 * @author Joerg Reichert <joerg@mapzone.io>
 */
public class MdToolbar
        extends Configurable {

    private Image                       navigationMenuImage  = images().svgImage( "ic_menu_48px.svg", NORMAL24 );

    private Image                       moreActionsMenuImage = images().svgImage( "ic_more_vert_48px.svg", NORMAL24 );

    private MdToolkit                   tk;

    private final Composite             control;

    private Composite                   actionsPanel;

    private List<ActionConfiguration>   actions = new ArrayList<ActionConfiguration>();


    /**
     * 
     * @param tk the Material design toolkit to be used to create specific MD
     *        components
     * @param parent the parent composite of that toolbar to create
     * @param title the title to be shown in this toolbar next to the navigation menu
     * @param style the style configuration for the toolbar
     * @param style {@link SWT#ON_TOP} The toolbar will float when scrolling. The
     *        toolbar disappears when scrolling otherwise.
     * @param actions the actions to be shown as buttons (for the first two actions)
     *        resp. menu entries for further actions
     */
    public MdToolbar( MdToolkit tk, Composite parent, String title, int style ) {
        this.tk = tk;
        
        /*
         * We could use org.eclipse.swt.widgets.ToolBar instead of creating this composite,
         * but I encounter some issues with applying styles and layouting (e.g. spacing
         * between ToolItems).
         * 
         * http://help.eclipse.org/juno/index.jsp?topic=/org.eclipse.rap.help/help/html/reference/theming/ToolBar.html
         */
        control = tk.createComposite( parent, style );
        UIUtils.setVariant( control, "atlas-toolbar" );
        
        // FIXME layout does not work at all (fixed or floating)
        // find a way to get "special areas" from the MdAppDesign, so that design can layout and we can fill
        control.setLayoutData( FormDataFactory.filled().noBottom().top( 0, 50 ).create() );
        control.moveAbove( null );
//        if ((style & SWT.ON_TOP) != 0) {
//            control.moveAbove( null );
//        }
        control.setLayout( new GridLayout( 3, false ) );

        createNavigationMenu();
        createTitleLabel( title );
    }


    /**
     * 
     * @return The base {@link Composite} of this toolbar.
     */
    public Control getControl() {
        return control;
    }


    public MdToolbar addAction( ActionConfiguration action ) {
        actions.add( action );
        updateActionsPanel();
        return this;
    }


    protected void updateActionsPanel() {
        // remove current
        if (actionsPanel != null) {
            actionsPanel.dispose();
        }
        
        // create new
        actionsPanel = tk.createComposite( control, SWT.NONE );
        actionsPanel.setLayout( new FillLayout( SWT.HORIZONTAL ) );
        
        // actions
        List<ActionConfiguration> sortedActions = sortActionsByPriorityAndName();
        if (actions.size() <= 3) {
            createVisibleActionButtons( sortedActions );
        }
        else {
            List<ActionConfiguration> visibleActions = sortedActions.subList( 0, 3 );
            createVisibleActionButtons( visibleActions );
            createMoreActionsButton( sortedActions );
        }
    }


    protected List<ActionConfiguration> sortActionsByPriorityAndName() {
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


    protected Label createTitleLabel( String title ) {
        Label titleLabel = new Label( control, SWT.NONE );
        titleLabel.setText( title );
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        titleLabel.setLayoutData( gridData );
        return titleLabel;
    }


    protected Button createNavigationMenu() {
        Button navigationMenuButton = tk.createButton( control, "", SWT.NONE );
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


    private Button createMoreActionsButton( List<ActionConfiguration> sortedActions ) {
        Button moreActionsMenuButton = tk.createButton( actionsPanel, "", SWT.NONE );
        moreActionsMenuButton.setImage( moreActionsMenuImage );
        Menu moreActionsMenu = new Menu( moreActionsMenuButton );
        addDropDownMenuListener( moreActionsMenuButton, control, moreActionsMenu );

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
            @Override
            public void widgetSelected( SelectionEvent event ) {
                Rectangle bounds = button.getBounds();
                Point point = parent.toDisplay( bounds.x - 0, bounds.y + bounds.height );
                menu.setLocation( point );
                menu.setVisible( true );
            }
        } );
    }


    private void createVisibleActionButtons( List<ActionConfiguration> visibleActions ) {
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
            @Override
            public void widgetSelected( SelectionEvent e ) {
                if (action.getCallback() != null) {
                    action.getCallback().handle( null );
                }
            }
        };
    }

    
//    public List<ActionConfiguration> getActionConfigurations() {
//        return actions;
//    }
    
}
