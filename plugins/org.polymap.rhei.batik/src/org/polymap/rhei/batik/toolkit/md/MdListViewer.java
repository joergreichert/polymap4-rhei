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

import static org.polymap.rhei.batik.toolkit.md.dp.dp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.template.ImageCell;
import org.eclipse.rap.rwt.template.Template;
import org.eclipse.rap.rwt.template.TextCell;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.ConfigurationFactory;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.toolkit.md.MdAppDesign.FontStyle;

/**
 * Expandable list. Use the {@link Config} properties to configure the viewer.
 * 
 * @see <a href="http://www.google.com/design/spec/components/lists.html">Material Design</a>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MdListViewer
        extends TreeViewer {

    private static Log log = LogFactory.getLog( MdListViewer.class );
    
    public static final String      CELL_ICON = "icon";
    public static final String      CELL_FIRSTLINE = "firstLine";
    public static final String      CELL_SECONDLINE = "secondLine";
    public static final String      CELL_THIRDLINE = "thirdLine";
    public static final String      CELL_EXPAND = "expand";
    
    
    /**
     * This label provider is responsible to provide the primary icon if any.
     */
    public Config<CellLabelProvider>   iconProvider;

    /**
     * The label provider of the first line.
     */
    public Config<CellLabelProvider>   firstLineLabelProvider;
    
    public Config<CellLabelProvider>   secondLineLabelProvider;
    
    public Config<CellLabelProvider>   thirdLineLabelProvider;
    
    private boolean                 customized = false;
    
    /**
     * 
     * <p/>
     * Possible style: {@link SWT#VIRTUAL}, {@link SWT#FULL_SELECTION}
     * 
     * @param parent
     * @param style
     */
    public MdListViewer( Composite parent, int style ) {
        super( parent, style );
        ConfigurationFactory.inject( this );
    }

    
    /**
     * Method is called after all label providers and configurations are done by
     * client code. It allows to customize the tree with templates, label providers
     * and stuff.
     */
    @Override
    public Control getControl() {
        customizeTree();
        return super.getControl();
    }
    
    
    protected void customizeTree() {
        if (!customized) {
            customized = true;
            Template template = new Template();

            // 16dp used for the tree node handle
            dp left = iconProvider.isPresent() ? dp( 56 ) : dp( 0 );
            dp tileHeight = dp( 0 );
            int colCount = 0;
            
            // first line
            if (firstLineLabelProvider.isPresent()) {
                TreeViewerColumn col = new TreeViewerColumn( this, SWT.NONE );
                col.setLabelProvider( firstLineLabelProvider.get() );

                TextCell cell = new TextCell( template );
                cell.setName( CELL_FIRSTLINE );
                cell.setLeft( left.pix() ).setRight( 50 )
                        .setTop( dp( 11 ).pix() ).setHeight( 18 )
                        .setHorizontalAlignment( SWT.LEFT );
                cell.setBindingIndex( colCount++ );
                cell.setSelectable( true );
                cell.setFont( MdAppDesign.font( FontStyle.Subhead ) );
                
                tileHeight = dp( 48 );
            }
            // second line
            if (secondLineLabelProvider.isPresent()) {
                TreeViewerColumn col = new TreeViewerColumn( this, SWT.NONE );
                col.setLabelProvider( secondLineLabelProvider.get() );

                TextCell cell = new TextCell( template );
                cell.setName( CELL_SECONDLINE );
                cell.setLeft( left.pix() ).setRight( 50 ).setTop( dp( 39 ).pix() ).setHeight( 15 );
                cell.setBindingIndex( colCount++ );

                tileHeight = dp( 72 );
            }
            // third line
            if (thirdLineLabelProvider.isPresent()) {
                TreeViewerColumn col = new TreeViewerColumn( this, SWT.NONE );
                col.setLabelProvider( thirdLineLabelProvider.get() );

                TextCell cell = new TextCell( template );
                cell.setName( CELL_THIRDLINE );
                cell.setLeft( 30 ).setRight( 30 ).setTop( 30 ).setHeight( 15 );
                cell.setBindingIndex( colCount++ );
            }
            // primary icon
            if (iconProvider.isPresent()) {
                TreeViewerColumn col = new TreeViewerColumn( this, SWT.NONE );
                col.setLabelProvider( iconProvider.get() );

                ImageCell cell = new ImageCell( template );
                cell.setName( CELL_ICON );
                cell.setLeft( 0 ).setWidth( dp( 56 ).pix() )
                        .setTop( 0 ).setHeight( tileHeight.pix() )
                        .setVerticalAlignment( SWT.CENTER ).setHorizontalAlignment( SWT.CENTER );
                cell.setBindingIndex( colCount++ );
                cell.setSelectable( true );
            }
            
            // expandable
            if (true) {
                ImageCell cell = new ImageCell( template );
                cell.setName( "expand" );
                cell.setRight( 1 ).setWidth( dp( 56 ).pix() ).setTop( 0 ).setHeight( tileHeight.pix() )
                        .setVerticalAlignment( SWT.CENTER ).setHorizontalAlignment( SWT.CENTER );
                cell.setImage( BatikPlugin.instance().imageForName( "resources/icons/md/chevron-down.png" ) );
                cell.setScaleMode( ImageCell.ScaleMode.NONE );
                cell.setSelectable( true );
            }
            
            getTree().addSelectionListener( new SelectionAdapter() {
                @Override
                public void widgetSelected( SelectionEvent ev ) {
                    log.info( "selection: " + ev );
                    if (CELL_EXPAND.equals( ev.text )) {
                        log.info( "data: " + ev.item.getData() );
                        expandToLevel( ev.item.getData(), 1 );
                    }
                }
            });
            super.addSelectionChangedListener( new ISelectionChangedListener() {
                @Override
                public void selectionChanged( SelectionChangedEvent ev ) {
                    log.info( "selection: " + ev );
                }
            });

            getTree().setData( RWT.ROW_TEMPLATE, template );        
            getTree().setData( RWT.CUSTOM_ITEM_HEIGHT, tileHeight.pix() );
        }
    }
    

    @Override
    public void setLabelProvider( IBaseLabelProvider labelProvider ) {
        throw new UnsupportedOperationException( "The Material Design list supports multiple lines of text, for example call #setFirstLineLabelProvider()." );
    }
     
}
