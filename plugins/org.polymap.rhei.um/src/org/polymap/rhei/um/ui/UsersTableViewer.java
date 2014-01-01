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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.deferred.SetModel;

import org.polymap.core.ui.SelectionAdapter;

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
    
    
    public UsersTableViewer( Composite parent, final Iterable<User> content, int style ) { super( parent, SWT.VIRTUAL /*| SWT.V_SCROLL | SWT.FULL_SELECTION |*/ | style );
        this.repo = UserRepository.instance();
        this.content = content;

        ColumnViewerToolTipSupport.enableFor( this );
        getTable().setLinesVisible( true );
        getTable().setHeaderVisible( true );
        getTable().setLayout( new TableLayout() );

        TableViewerColumn vcolumn = new TableViewerColumn( this, SWT.CENTER );
        vcolumn.getColumn().setResizable( true );
        vcolumn.getColumn().setText( "Name" );
        vcolumn.setLabelProvider( new ColumnLabelProvider() {
            public String getText( Object elm ) {
                log.info( "getText(): ..." );
                User user = (User)elm;
                String firstname = user.firstname().get();
                return firstname != null && firstname.length() > 0 
                        ? user.name().get() + ", " + firstname
                        : user.name().get();
            }
        });
        ((TableLayout)getTable().getLayout()).addColumnData( new ColumnWeightData( 2, 100, true ) );            
        getTable().setSortColumn( vcolumn.getColumn() );

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

        setContentProvider( new ArrayContentProvider() );
        setInput( Iterables.toArray( content, User.class ) );

//        setContentProvider( new DeferredContentProvider( new Comparator<User>() {
//            public int compare( User left, User right ) {
//                return left.name().get().compareToIgnoreCase( right.name().get() );
//            }
//        }));
//        setInput( model = new SetModel() );
//        
//        // content loader
//        new UIJob( "Nutzer laden" ) {
//            protected void runWithException( IProgressMonitor monitor ) throws Exception {
//                for (User user : content) {
//                    Thread.sleep( 1000 );
//                    model.addAll( new Object[] { user } );
//                }
//            }
//        }.schedule();
//        model.addAll( ImmutableList.copyOf( content ) );
    }

    
    public void reload() {
//        super.remove( elements );
        setInput( Iterables.toArray( content, User.class ) );
    }


    public User getSelectedUser() {
        return new SelectionAdapter( getSelection() ).first( User.class );
    }
    
}
