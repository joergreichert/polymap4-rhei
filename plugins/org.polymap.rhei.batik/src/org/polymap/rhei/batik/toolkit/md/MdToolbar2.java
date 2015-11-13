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
package org.polymap.rhei.batik.toolkit.md;

import static org.polymap.core.runtime.event.TypeEventFilter.ifType;
import static org.polymap.core.ui.UIUtils.setVariant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.layout.RowLayoutFactory;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * 
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MdToolbar2
        implements MdToolItemContainer {

    private static Log log = LogFactory.getLog( MdToolbar2.class );
    
    /** Pseudo class for toolbar Composite. */
    private static final String     CSS_TOOLBAR = "toolbar2";
    /** Pseudo class for toolbar item (Button). */
    private static final String     CSS_TOOLBAR_ITEM = "toolbar2-item";
    
    /**
     * 
     */
    public enum Alignment {
        Left, Right;        
    }
    
    
    // instance *******************************************
    
    private Composite           bar;

    private MdToolkit           tk;
    
    private GroupItem           rootGroup = new GroupItem( this, "root" );            
    

    MdToolbar2( Composite parent, MdToolkit tk, int style ) {
        this.tk = tk;
        
        bar = tk.createComposite( parent, style );
        bar.setLayout( RowLayoutFactory.fillDefaults().spacing( 3 ).create() );
        
        EventManager.instance().subscribe( this, ifType( ToolItemEvent.class, 
                ev2 -> ev2.getSource().toolbar() == this ) );
    }
    
    
    public void dispose() {
        EventManager.instance().unsubscribe( this );
    }
    
    
    @EventHandler( display=true, delay=100 )
    protected void onItemChange( List<ToolItemEvent> evs ) {
        update();
    }
    
    
    protected void update() {
    }
    
    
    protected void updateGroup( Composite parent, GroupItem group ) {
        Composite control = (Composite)Arrays.stream( parent.getChildren() )
                .filter( c -> c.getData( "_item_" ) == group )
                .findAny().get();
        
        if (control == null) {
            control = setVariant( tk.createComposite( parent ), CSS_TOOLBAR );
            control.setData( "_item_", group );
        }
    }
    
    
    protected void updateItem( Composite parent, ToolItem item ) {
        Control control = (Control)Arrays.stream( parent.getChildren() )
                .filter( c -> c.getData( "_item_" ) == item )
                .findAny().get();
        
        // PushToolItem
        if (item instanceof PushToolItem) {
            Button btn = (Button)control;
            if (btn == null) {
                btn = setVariant( tk.createButton( parent, null, SWT.PUSH ), CSS_TOOLBAR_ITEM );
                btn.setData( "_item_", item );
                btn.addSelectionListener( new SelectionAdapter() {
                    @Override
                    public void widgetSelected( SelectionEvent ev ) {
                        ((PushToolItem)item).action.get().run();
                    }
                });
            }
            btn.setText( ((PushToolItem)item).text.get() );
            btn.setToolTipText( ((PushToolItem)item).tooltip.get() );
            btn.setImage( ((PushToolItem)item).icon.get() );
        }
        // unknown
        else {
            throw new RuntimeException( "Unhandled ToolItem type: " + item );
        }
    }
    
    
    public Control getControl() {
        return bar;
    }
    
    
    // ToolItem *******************************************

    /**
     * 
     */
    public abstract class ToolItem
            extends Configurable {
        
        private MdToolItemContainer     container;
        
        
        public ToolItem( MdToolItemContainer container ) {
            this.container = container;
            GroupItem group = container instanceof GroupItem 
                    ? (GroupItem)container 
                    : ((MdToolbar2)container).rootGroup;
            group.addItem( this );
        }
        
        public MdToolbar2 toolbar() {
            return container instanceof GroupItem 
                    ? ((GroupItem)container).toolbar() 
                    : (MdToolbar2)container;
            
        }
    }
    
    
    // GroupItem ******************************************
    
    /**
     * 
     */
    public class GroupItem
            extends ToolItem
            implements MdToolItemContainer {

        @Mandatory
        @Immutable
        public Config2<GroupItem,String>    id;
        
        @Mandatory
        public Config2<GroupItem,Alignment> align;
        
        private List<ToolItem>              items = new ArrayList();
        
        
        public GroupItem( MdToolItemContainer container, String id ) {
            super( container );
            this.id.set( id );
            this.align.set( Alignment.Left );
        }


        protected void addItem( ToolItem item ) {
            items.add( item );
        }
    }

    
    // PushToolItem ***************************************
    
    /**
     * 
     */
    public class PushToolItem
            extends ToolItem {
        
        public PushToolItem( MdToolbar2 parent ) {
            super( parent );
        }

        public Config2<ToolItem,String>     text;
        
        public Config2<ToolItem,String>     tooltip;
        
        public Config2<ToolItem,Image>      icon;
        
        @Mandatory
        public Config2<ToolItem,Runnable>   action;
    }
    
    
    /**
     * 
     */
    class ToolItemEvent
            extends EventObject {

        public ToolItemEvent( ToolItem source ) {
            super( source );
        }

        public <T extends ToolItem> T item() {
            return (T)super.getSource();
        }
        
        @Override
        public ToolItem getSource() {
            return (ToolItem)super.getSource();
        }
    }
    
}
