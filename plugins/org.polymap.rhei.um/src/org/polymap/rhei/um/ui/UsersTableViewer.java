/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.um.ui;

import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.deferred.DeferredContentProvider;
import org.eclipse.jface.viewers.deferred.SetModel;

import org.polymap.rhei.um.Address;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UsersTableViewer
        extends TableViewer {

    private static Log log = LogFactory.getLog( UsersTableViewer.class );
    
    private UserRepository      repo;

    private Iterable<User>      content;
    
    private SetModel            model;
    
    
    public UsersTableViewer( Composite parent, Iterable<User> content, int style ) {
        super( parent, SWT.VIRTUAL /*| SWT.V_SCROLL | SWT.FULL_SELECTION |*/ | style );
        this.repo = UserRepository.instance();
        this.content = content;

        ColumnViewerToolTipSupport.enableFor( this );
        getTable().setLinesVisible( true );
        getTable().setHeaderVisible( true );
        getTable().setLayout( new TableLayout() );

        TableViewerColumn vcolumn = new TableViewerColumn( this, SWT.LEFT );
        vcolumn.getColumn().setResizable( true );
        vcolumn.getColumn().setText( "Name" );
        vcolumn.setLabelProvider( new ColumnLabelProvider() {
            public String getText( Object elm ) {
                return Joiner.on( ' ' ).skipNulls().join( ((User)elm).firstname().get(), ((User)elm).name().get() );
            }
        });
        ((TableLayout)getTable().getLayout()).addColumnData( new ColumnWeightData( 2, 100, true ) );            

        vcolumn = new TableViewerColumn( this, SWT.LEFT );
        vcolumn.getColumn().setResizable( true );
        vcolumn.getColumn().setText( "Adresse" );
        vcolumn.setLabelProvider( new ColumnLabelProvider() {
            public String getText( Object elm ) {
                Address address = ((User)elm).address().get();
                return Joiner.on( ' ' ).skipNulls().join( 
                        address.postalCode().get(), address.city().get() );
            }
        });
        ((TableLayout)getTable().getLayout()).addColumnData( new ColumnWeightData( 2, 100, true ) );            

        setContentProvider( new DeferredContentProvider( new Comparator<User>() {
            public int compare( User o1, User o2 ) {
                return 0;
            }
        }));
        setInput( model = new SetModel() );
        model.addAll( ImmutableList.copyOf( content ) );
    }

}
