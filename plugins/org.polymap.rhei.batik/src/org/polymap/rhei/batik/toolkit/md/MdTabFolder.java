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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.polymap.core.ui.UIUtils;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class MdTabFolder extends Composite {

    private static final String TABITEM_SELECTED_STYLE = "tabItem_selected";
    
    private static final String TABITEM_DEFAULT_STYLE  = "tabItem_default";
    
    private Map<String,Button> tabButtons = new HashMap<String, Button>();
    private Map<String,Composite> tabItemContents = new HashMap<String, Composite>();    
    private List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();

    private Composite tabContent = null;

    public MdTabFolder( Composite parent, java.util.List<String> tabItems,
            Map<String,Function<Composite,Composite>> tabContents, int style ) {
        super( parent, style );
        setTabFolderLayout();
        tabButtons = createTabBar( tabItems );
        createTabContents( tabItems, tabContents, tabButtons );
    }

    private void createTabContents( java.util.List<String> tabItems,
            Map<String,Function<Composite,Composite>> tabContents, Map<String,Button> tabButtons ) {
        tabContent = new Composite( this, SWT.NONE );
        tabContent.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        final StackLayout layout = setTabContentLayout( tabContent );
        tabItemContents = createAndRevealTabContents( tabItems, tabContents, tabButtons, tabContent, layout );
        tabContent.layout();
        addTabSwitchListener( tabItems );
    }

    private void addTabSwitchListener( java.util.List<String> tabItems) {
        for (String label : tabItems) {
            tabButtons.get( label ).addSelectionListener( new SelectionAdapter() {

                public void widgetSelected( SelectionEvent e ) {
                    openTab( label );
                    selectionListeners.forEach( selectionListener -> selectionListener.widgetSelected(e) );
                }
            } );
        }
    }

    private Map<String,Composite> createAndRevealTabContents( java.util.List<String> tabItems,
            Map<String,Function<Composite,Composite>> tabContents, Map<String,Button> tabButtons,
            final Composite tabContent, final StackLayout layout ) {
        Map<String,Composite> map = new HashMap<String,Composite>();
        tabContents.entrySet().stream()
                .forEach( entry -> map.put( entry.getKey(), entry.getValue().apply( tabContent ) ) );
        if (tabItems.size() > 0) {
            layout.topControl = map.get( tabItems.get( 0 ) );
            UIUtils.setVariant( tabButtons.get( tabItems.get( 0 ) ), TABITEM_SELECTED_STYLE );
        }
        return map;
    }

    private StackLayout setTabContentLayout( final Composite tabContent ) {
        final StackLayout layout = new StackLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        tabContent.setLayout( layout );
        return layout;
    }

    private void setTabFolderLayout() {
        GridLayout tabFolderLayout = new GridLayout( 1, false );
        tabFolderLayout.verticalSpacing = 0;
        setLayout( tabFolderLayout );
    }

    private Map<String,Button> createTabBar( java.util.List<String> tabItems ) {
        Composite tabBar = new Composite( this, SWT.NONE );

        Object data = createTabBarGridData();
        tabBar.setLayoutData( data );
        
        setTabBarLayout( tabItems, tabBar );
        
        return createTabButtons( tabItems, tabBar, data );
    }

    private Map<String,Button> createTabButtons( java.util.List<String> tabItems, Composite tabBar, Object data ) {
        Map<String,Button> tabButtons = new HashMap<String,Button>();
        for (String label : tabItems) {
            Button button = new Button( tabBar, SWT.PUSH );
            button.setText( label );
            button.setLayoutData( data );
            UIUtils.setVariant( button, TABITEM_DEFAULT_STYLE );
            tabButtons.put( label, button );
        }
        return tabButtons;
    }

    private void setTabBarLayout( java.util.List<String> tabItems, Composite tabBar ) {
        GridLayout tabBarGridLayout = new org.eclipse.swt.layout.GridLayout( tabItems.size(), true );
        tabBarGridLayout.verticalSpacing = 0;
        tabBarGridLayout.horizontalSpacing = 0;
        tabBarGridLayout.marginHeight = 0;
        tabBarGridLayout.marginWidth = 0;
        tabBar.setLayout( tabBarGridLayout );
    }

    private Object createTabBarGridData() {
        GridData data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = SWT.FILL;
        return data;
    }

    public void replaceTabContent( String label, Function<Composite,Composite> tabContentFun ) {
        tabItemContents.put( label, tabContentFun.apply(tabContent) );
    }

    public void openTab( String label ) {
        Composite tabItem = tabItemContents.get( label );
        if(tabItem != null) {
            Composite tabFolder = tabItem.getParent();
            ((StackLayout) tabFolder.getLayout()).topControl = tabItem;
            tabFolder.layout();
            UIUtils.setVariant( tabButtons.get( label ), TABITEM_SELECTED_STYLE );
            tabButtons.values().stream().filter( button -> button != tabButtons.get( label ) )
            .forEach( button -> UIUtils.setVariant( button, TABITEM_DEFAULT_STYLE ) );
        }
    }

    public void addSelectionListener( SelectionListener selectionListener ) {
        selectionListeners.add( selectionListener );
    }

    public void removeSelectionListener( SelectionListener selectionListener ) {
        selectionListeners.remove( selectionListener );
    }
}
