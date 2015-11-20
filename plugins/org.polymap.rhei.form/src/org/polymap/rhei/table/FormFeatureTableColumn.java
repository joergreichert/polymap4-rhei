/*
 * polymap.org
 * Copyright (C) 2011-2015, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.table;

import java.util.Collections;
import static org.polymap.core.runtime.event.SourceEventFilter.Identical;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import java.beans.PropertyChangeEvent;

import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerColumn;

import org.eclipse.rap.rwt.graphics.Graphics;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.runtime.event.SourceEventFilter;

import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NullValidator;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;

/**
 * An {@link IFeatureTableColumn} that employes {@link IFormField} and
 * {@link IFormFieldValidator} to display/transform and edit values of a
 * {@link FeatureTableViewer}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FormFeatureTableColumn
        implements IFeatureTableColumn {

    static Log log = LogFactory.getLog( FormFeatureTableColumn.class );

    public static final Color       INVALID_BACKGROUND = Graphics.getColor( 0xff, 0xd0, 0xe0 );
    public static final Color       DIRTY_BACKGROUND = Graphics.getColor( 0xd0, 0xf0, 0xc0 );

    private FeatureTableViewer      viewer;

    private PropertyDescriptor      prop;
    
    private IFormFieldValidator     labelValidator;
    
    private IFormField              editingFormField;
    
    private IFormFieldValidator     editingValidator;
    
    private ColumnLabelProvider     labelProvider;
    
    private EditingSupport          editingSupport;
    
    private Comparator<IFeatureTableElement> sorter;

    private String                  header;

    private int                     weight = -1;

    private int                     minimumWidth = -1;
    
    private int                     align = -1;
    
    private boolean                 sortable = true;
    
    /** The feature ids of the currently dirty {@link IFeatureTableElement}s. */
    private Set<String>             dirtyFids = new HashSet();

    /** The feature ids of the currently invalid {@link IFeatureTableElement}s. */
    private Set<String>             invalidFids = new HashSet();

    private TableViewerColumn       viewerColumn;


    public FormFeatureTableColumn( PropertyDescriptor prop ) {
        super();
        assert prop != null : "Argument is null.";
        this.prop = prop;
    }

    public FormFeatureTableColumn addFieldChangeListener( Object annotated ) {
        EventManager.instance().subscribe( annotated, new SourceEventFilter( this, Identical ) );
        return this;
    }
    
    public FeatureTableViewer getViewer() {
        return viewer;
    }
    
    public PropertyDescriptor getProperty() {
        return prop;
    }

    @Override
    public String getName() {
        return prop.getName().getLocalPart();
    }

    @Override
    public FormFeatureTableColumn setLabelProvider( ColumnLabelProvider labelProvider ) {
        this.labelProvider = labelProvider;
        return this;
    }

    @Override
    public ColumnLabelProvider getLabelProvider() {
        return labelProvider;
    }

    public FormFeatureTableColumn setLabelProvider( IFormFieldValidator labelValidator ) {
        this.labelValidator = labelValidator;
        return this;
    }


    public FormFeatureTableColumn setHeader( String header ) {
        this.header = header;
        return this;
    }

    public FormFeatureTableColumn setWeight( int weight, int minimumWidth ) {
        this.weight = weight;
        this.minimumWidth = minimumWidth;
        return this;
    }
    
    @Override
    public int getWeight() {
        return weight;
    }

    public FormFeatureTableColumn setAlign( int align ) {
        this.align = align;
        return this;
    }

    public boolean isSortable() {
        return sortable;
    }
    
    public FormFeatureTableColumn setSortable( boolean sortable ) {
        this.sortable = sortable;
        return this;
    }

    public FormFeatureTableColumn setSortable( Comparator<IFeatureTableElement> sorter ) {
        this.sorter = sorter;
        return this;
    }
    

    public FormFeatureTableColumn setEditing( IFormField formField, IFormFieldValidator validator ) {
        assert viewer == null : "Call before table is created.";
        this.editingFormField = formField;
        this.editingValidator = validator != null ? validator : new NullValidator();
        return this;
    }


    @Override
    public void setViewer( FeatureTableViewer viewer ) {
        this.viewer = viewer;
    }
    
    
    @Override
    public TableViewerColumn getViewerColumn() {
        return viewerColumn;
    }

    
    @Override
    public TableViewerColumn newViewerColumn() {
        assert viewerColumn == null;
        
        if (align == -1) {
            align = Number.class.isAssignableFrom( prop.getType().getBinding() )
                    || Date.class.isAssignableFrom( prop.getType().getBinding() )
                    ? SWT.RIGHT : SWT.LEFT;
        }

        viewerColumn = new TableViewerColumn( viewer, align );
        viewerColumn.getColumn().setMoveable( true );
        viewerColumn.getColumn().setResizable( true );
        viewerColumn.getColumn().setText( header != null ? header : StringUtils.capitalize( getName() ) );
        
        boolean editing = editingFormField != null;
        
        // defaults for basic types
        Class binding = prop.getType().getBinding();
        Locale locale = Optional.ofNullable( Polymap.getSessionLocale() ).orElse( Locale.getDefault() );
        // Number
        if (Number.class.isAssignableFrom( binding )) {
            labelValidator = labelValidator != null ? labelValidator : new NumberValidator( binding, locale );
            editingValidator = editingValidator != null ? editingValidator : labelValidator;
            editingFormField = editingFormField != null ? editingFormField : new StringFormField();
        }
        // Date
        else if (Date.class.isAssignableFrom( binding )) {
            throw new RuntimeException( "Not yet supported: Date" );
        }
        // Boolean
        else if (Boolean.class.isAssignableFrom( binding )) {
            throw new RuntimeException( "Not yet supported: Boolean" );
        }
        // default: String
        else {
            labelValidator = labelValidator != null ? labelValidator : new NullValidator();
            editingValidator = editingValidator != null ? editingValidator : labelValidator;
            editingFormField = editingFormField != null ? editingFormField : new StringFormField();
        }
        
        // labelProvider
        labelProvider = labelProvider != null ? labelProvider : new FormColumnLabelProvider( this, labelValidator );
        viewerColumn.setLabelProvider( new LoadingCheckLabelProvider( labelProvider ) );
        
        // editingSupport
        if (editing) {
            editingSupport = new FormEditingSupport( viewer, this, editingFormField, editingValidator );
            viewerColumn.setEditingSupport( editingSupport );
        }
        
        // sort listener for supported prop bindings
        Class propBinding = prop.getType().getBinding();
        if (sortable &&
                (String.class.isAssignableFrom( propBinding )
                || Number.class.isAssignableFrom( propBinding )
                || Date.class.isAssignableFrom( propBinding ))) {

            viewerColumn.getColumn().addListener( SWT.Selection, new Listener() {
                public void handleEvent( Event ev ) {
                    TableColumn sortColumn = viewer.getTable().getSortColumn();
                    final TableColumn selectedColumn = (TableColumn)ev.widget;
                    int dir = viewer.getTable().getSortDirection();
                    //log.info( "Sort: sortColumn=" + sortColumn.getText() + ", selectedColumn=" + selectedColumn.getText() + ", dir=" + dir );

                    if (sortColumn == selectedColumn) {
                        dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
                    } 
                    else {
                        dir = SWT.DOWN;
                    }
                    Comparator<IFeatureTableElement> comparator = newComparator( dir );
                    viewer.sortContent( comparator, dir, selectedColumn );
                }
            });
        }
        
        TableLayout tableLayout = (TableLayout)viewer.getTable().getLayout();

        if (weight > -1) {
            tableLayout.addColumnData( new ColumnWeightData( weight, minimumWidth, true ) );            
        }
        else if (String.class.isAssignableFrom( propBinding )) {
            tableLayout.addColumnData( new ColumnWeightData( 20, 120, true ) );
        }
        else {
            tableLayout.addColumnData( new ColumnWeightData( 10, 80, true ) );            
        }
        return viewerColumn;
    }

    
    public Comparator<IFeatureTableElement> newComparator( int sortDir ) {
        Comparator<IFeatureTableElement> result = null;
        if (sorter != null) {
            result = sorter;
        }
        else {
            result = new Comparator<IFeatureTableElement>() {

                private String                  sortPropName = getName();
                private ColumnLabelProvider     lp = getLabelProvider();

                @Override
                public int compare( IFeatureTableElement elm1, IFeatureTableElement elm2 ) {
                    // the value from the elm or String from LabelProvider as fallback
                    Object value1 = Optional.ofNullable( elm1.getValue( sortPropName ) ).orElse( lp.getText( elm1 ) );
                    Object value2 = Optional.ofNullable( elm2.getValue( sortPropName ) ).orElse( lp.getText( elm2 ) );
                    
                    if (value1 == null && value2 == null) {
                        return 0;
                    }
                    else if (value1 == null) {
                        return -1;
                    }
                    else if (value2 == null) {
                        return 1;
                    }
                    else if (!value1.getClass().equals( value2.getClass() )) {
                        throw new RuntimeException( "Column type do not match: " + value1.getClass().getSimpleName() + " - " + value2.getClass().getSimpleName() );
                    }
                    else if (value1 instanceof String) {
                        return ((String)value1).compareToIgnoreCase( (String)value2 );
                    }
                    else if (value1 instanceof Number) {
                        return (int)(((Number)value1).doubleValue() - ((Number)value2).doubleValue());
                    }
                    else if (value1 instanceof Date) {
                        return ((Date)value1).compareTo( (Date)value2 );
                    }
                    else {
                        return value1.toString().compareTo( value2.toString() );
                    }
                }
            };
        }
        return sortDir == SWT.UP ? Collections.reverseOrder( result ) : result;
    }

    
    @Override
    public FormFeatureTableColumn sort( int dir ) {
        assert viewerColumn != null : "Add this column to the viewer before calling sort()!";
        Comparator<IFeatureTableElement> comparator = newComparator( dir );
        viewer.sortContent( comparator, dir, viewerColumn.getColumn() );
        return this;
    }

    
    protected void markElement( IFeatureTableElement elm, boolean dirty, boolean invalid ) {
        String fid = elm.fid();
        boolean success = dirty ? dirtyFids.add( fid ) : dirtyFids.remove( fid );
        log.debug( "markElement: elm=" + fid + ", dirty=" + dirty + ", success="  + success );
        success = invalid ? invalidFids.add( fid ) : invalidFids.remove( fid );
        log.debug( "markElement: elm=" + fid + ", invalid=" + invalid + ", success="  + success );
        
        EventManager.instance().publish( new PropertyChangeEvent( this, getName(), null, null ) );
    }
    
    
    public Set<String> invalidFids() {
        return invalidFids;
    }
    
    
    public Set<String> dirtyFids() {
        return dirtyFids;
    }
    
    
    /**
     * Loading and dirty/valid decoration. 
     */
    class LoadingCheckLabelProvider
            extends ColumnLabelProvider {
    
        private ColumnLabelProvider     delegate;

        public LoadingCheckLabelProvider( ColumnLabelProvider delegate ) {
            assert delegate != null;
            this.delegate = delegate;
        }

        public String getText( Object element ) {
            return element == FeatureTableViewer.LOADING_ELEMENT
                    ? "Laden..."
                    : delegate.getText( element );
        }

        public String getToolTipText( Object element ) {
            return element == FeatureTableViewer.LOADING_ELEMENT
                    ? null : delegate.getToolTipText( element );
        }

        public Image getImage( Object elm ) {
            if (elm == FeatureTableViewer.LOADING_ELEMENT) {
                return null;
            }
            else if (invalidFids.contains( ((IFeatureTableElement)elm).fid() ) ) {
//                return DefaultFormFieldDecorator.invalidImage;
                return null;
            }
            else if (dirtyFids.contains( ((IFeatureTableElement)elm).fid() ) ) {
//                return DefaultFormFieldDecorator.dirtyImage;
                return null;
            }
            else {
                return delegate.getImage( elm );
            }
        }

        public Color getForeground( Object element ) {
            return element == FeatureTableViewer.LOADING_ELEMENT
                    ? FeatureTableViewer.LOADING_FOREGROUND
                    : delegate.getForeground( element );
        }

        public Color getBackground( Object elm ) {
            if (elm == FeatureTableViewer.LOADING_ELEMENT) {
                return FeatureTableViewer.LOADING_BACKGROUND;
            }
            else if (invalidFids.contains( ((IFeatureTableElement)elm).fid() ) ) {
                return INVALID_BACKGROUND;
            }
            else if (dirtyFids.contains( ((IFeatureTableElement)elm).fid() ) ) {
                return DIRTY_BACKGROUND;
            }
            else {
                return delegate.getBackground( elm );
            }
        }

        public void addListener( ILabelProviderListener listener ) {
            delegate.addListener( listener );
        }

//        public void update( ViewerCell cell ) {
//            delegate.update( cell );
//        }

        public void dispose() {
            delegate.dispose();
        }

        public boolean isLabelProperty( Object element, String property ) {
            return delegate.isLabelProperty( element, property );
        }

        public Font getFont( Object element ) {
            return delegate.getFont( element );
        }

        public void removeListener( ILabelProviderListener listener ) {
            delegate.removeListener( listener );
        }

        public Image getToolTipImage( Object object ) {
            return delegate.getToolTipImage( object );
        }

        public Color getToolTipBackgroundColor( Object object ) {
            return delegate.getToolTipBackgroundColor( object );
        }

        public Color getToolTipForegroundColor( Object object ) {
            return delegate.getToolTipForegroundColor( object );
        }

        public Font getToolTipFont( Object object ) {
            return delegate.getToolTipFont( object );
        }

        public Point getToolTipShift( Object object ) {
            return delegate.getToolTipShift( object );
        }

        public boolean useNativeToolTip( Object object ) {
            return delegate.useNativeToolTip( object );
        }

        public int getToolTipTimeDisplayed( Object object ) {
            return delegate.getToolTipTimeDisplayed( object );
        }

        public int getToolTipDisplayDelayTime( Object object ) {
            return delegate.getToolTipDisplayDelayTime( object );
        }

        public int getToolTipStyle( Object object ) {
            return delegate.getToolTipStyle( object );
        }

        public void dispose( @SuppressWarnings("hiding") ColumnViewer viewer, ViewerColumn column ) {
            delegate.dispose( viewer, column );
        }
        
    }

}
