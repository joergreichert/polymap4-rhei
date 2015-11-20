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
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultPropertyConcern;
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
    
    private GroupItem           rootGroup = new GroupItem( null, "root" );            
    

    MdToolbar2( Composite parent, MdToolkit tk, int style ) {
        this.tk = tk;
        
        bar = setVariant( tk.createComposite( parent, style ), CSS_TOOLBAR );
        bar.setLayout( new FillLayout() );  //RowLayoutFactory.fillDefaults().spacing( 3 ).create() );
        
        EventManager.instance().subscribe( this, ifType( ToolItemEvent.class, 
                ev2 -> ev2.getSource().toolbar() == MdToolbar2.this ) );
    }
    
    
    public void dispose() {
        EventManager.instance().unsubscribe( this );
    }
    
    
    @EventHandler( display=true, delay=100 )
    protected void onItemChange( List<ToolItemEvent> evs ) {
        renderGroup( bar, rootGroup );
    }
    

    protected void renderGroup( Composite parent, GroupItem group ) {
        // find Control of the group
        Composite control = findControl( parent, group );
        
        // create if not yet present
        if (control == null) {
            control = tk.createComposite( parent );
            control.setLayout( RowLayoutFactory.fillDefaults().spacing( 3 ).create() );
            control.setData( "_item_", group );
        }
        
        //
        for (ToolItem item : group.items) {
            renderItem( control, item );    
        }
    }
    
    
    protected void renderItem( Composite parent, ToolItem item ) {
        // find Control of the group
        Button btn = findControl( parent, item );
        
        // PushToolItem
        if (item instanceof PushToolItem) {
            if (btn == null) {
                btn = setVariant( tk.createButton( parent, null, SWT.PUSH ), CSS_TOOLBAR_ITEM );
                btn.setData( "_item_", item );
                btn.setLayoutData( RowDataFactory.swtDefaults().hint( SWT.DEFAULT, 30 ).create() );
                btn.addSelectionListener( new SelectionAdapter() {
                    @Override
                    public void widgetSelected( SelectionEvent ev ) {
                        ((PushToolItem)item).action.get().accept( ev );
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
    
    
    protected <C extends Control> C findControl( Composite parent, ToolItem item ) {
        return (C)Arrays.stream( parent.getChildren() )
                .filter( c -> c.getData( "_item_" ) == item )
                .findAny().orElse( null );
    }


    public Control getControl() {
        return bar;
    }
    
    
    // ToolItem *******************************************

    /**
     * 
     */
    public abstract static class ToolItem
            extends Configurable {
        
        /** The container of this item, or null if this is the root group of a toolbar */
        private MdToolItemContainer     container;
        
        
        public ToolItem( MdToolItemContainer container ) {
            this.container = container;
            if (container == null) {
                // nothing to do, we are root GroupItem
            }
            else if (container instanceof GroupItem) {
                ((GroupItem)container).addItem( this );
            }
            else if (container instanceof MdToolbar2) {
                ((MdToolbar2)container).rootGroup.addItem( this );
            }
            else {
                throw new RuntimeException( "Unknown container type: " + container );
            }
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
    public static class GroupItem
            extends ToolItem
            implements MdToolItemContainer {

        @Mandatory
        @Immutable
        @Concern( ToolItemEvent.Fire.class )
        public Config2<GroupItem,String>    id;
        
        @Mandatory
        @Concern( ToolItemEvent.Fire.class )
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
    public static class PushToolItem
            extends ToolItem {
        
        public PushToolItem( MdToolbar2 parent ) {
            super( parent );
        }

        @Concern( ToolItemEvent.Fire.class )
        public Config2<PushToolItem,String>     text;
        
        @Concern( ToolItemEvent.Fire.class )
        public Config2<PushToolItem,String>     tooltip;
        
        @Concern( ToolItemEvent.Fire.class )
        public Config2<PushToolItem,Image>      icon;
        
        @Mandatory
        @Concern( ToolItemEvent.Fire.class )
        public Config2<PushToolItem,Consumer<SelectionEvent>> action;
    }
    
    
    /**
     * 
     */
    static class ToolItemEvent
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
        
        /**
         * 
         */
        public static class Fire
                extends DefaultPropertyConcern {

            /**
             * This is called *before* the {@link Config2} property is set. However, there is no
             * race condition between event handler thread, that might access property value, and
             * the current thread, that sets the property value, because most {@link EventHandler}s
             * are done in display thread.
             */
            @Override
            public Object doSet( Object obj, Config prop, Object newValue ) {
                ToolItem item = prop.info().getHostObject();
                EventManager.instance().syncPublish( new ToolItemEvent( item ) );
                return newValue;
            }
        }
    }
    
}
