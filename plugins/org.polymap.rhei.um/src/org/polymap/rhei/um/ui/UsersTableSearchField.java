/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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

import java.util.List;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.rwt.graphics.Graphics;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.cache.CacheLoader;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.um.Address;
import org.polymap.rhei.um.Property;
import org.polymap.rhei.um.User;

/**
 *  A search text field for {@link UsersTableViewer}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UsersTableSearchField
        extends ViewerFilter
        implements CacheLoader<String,UsersTableSearchField.PropertyAccessor,IntrospectionException> {

    private static Log log = LogFactory.getLog( UsersTableSearchField.class );
    
    private UsersTableViewer        viewer;
    
    private Composite               container;
    
    private Text                    searchTxt;
    
    private Label                   clearBtn;
    
    private List<String>            searchPropNames;

    protected String                filterText;
    
    private Cache<String,PropertyAccessor>  accessors = CacheConfig.DEFAULT.defaultElementSize( 1024 ).create();

    
    public UsersTableSearchField( UsersTableViewer _viewer, Composite _parent, Iterable<String> _searchPropNames ) {
        this.viewer = _viewer;
        this.searchPropNames = Lists.newArrayList( _searchPropNames );

        container = new Composite( _parent, SWT.NONE );
        container.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );

        clearBtn = new Label( container, SWT.PUSH | SWT.SEARCH );
        clearBtn.setToolTipText( "Zurücksetzen" );
        clearBtn.setImage( DataPlugin.getDefault().imageForName( "icons/etool16/delete_edit.gif" ) );
        clearBtn.setLayoutData( FormDataFactory.filled().top( 0, 5 ).right( 100, -5 ).left( -1 ).create() );
        clearBtn.addMouseListener( new MouseAdapter() {
            public void mouseUp( MouseEvent e ) {
                filterText = null;
                viewer.refresh();
                clearBtn.setVisible( false );
                searchTxt.setText( "Suchen..." );
                searchTxt.setForeground( Graphics.getColor( 0xa0, 0xa0, 0xa0 ) );
            }
        });
//        clearBtn.addSelectionListener( new SelectionAdapter() {
//            public void widgetSelected( SelectionEvent e ) {
//                searchTxt.setText( "" );
//            }
//        });
        clearBtn.setVisible( false );

        searchTxt = new Text( container, SWT.SEARCH | SWT.CANCEL );
        searchTxt.setLayoutData( FormDataFactory.filled().create() );
        searchTxt.moveBelow( clearBtn );

        searchTxt.setText( "Suchen..." );
        searchTxt.setToolTipText( "Suchbegriff: min. 3 Zeichen" );
        searchTxt.setForeground( Graphics.getColor( 0xa0, 0xa0, 0xa0 ) );
        searchTxt.addFocusListener( new FocusListener() {
            @Override
            public void focusLost( FocusEvent ev ) {
                if (searchTxt.getText().length() == 0) {
//                    searchTxt.setText( "Suchen..." );
//                    searchTxt.setForeground( Graphics.getColor( 0xa0, 0xa0, 0xa0 ) );
                    clearBtn.setVisible( false );
                }
            }
            @Override
            public void focusGained( FocusEvent ev ) {
                if (searchTxt.getText().startsWith( "Suchen" )) {
                    searchTxt.setText( "" );
                    searchTxt.setForeground( Graphics.getColor( 0x00, 0x00, 0x00 ) );
                }
            }
        });

        viewer.addFilter( UsersTableSearchField.this );
        
        searchTxt.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                String text = searchTxt.getText();
                if (!text.startsWith( "Suchen" )) {
                    filterText = text.toLowerCase();
                    clearBtn.setVisible( filterText.length() > 0 );
                    viewer.refresh();
                }
            }
        });
    }

    
    public void dispose() {
        viewer.removeFilter( UsersTableSearchField.this );
    }

    
    public Composite getControl() {
        return container;
    }
    

    // ViewerFilter ***************************************
    
    /**
     * Access a direct property of {@link User}.
     */
    public static class PropertyAccessor {
        protected PropertyDescriptor    descriptor;

        public PropertyAccessor() {
        }
        public PropertyAccessor( String propName ) throws IntrospectionException {
            descriptor = new PropertyDescriptor( propName, User.class, propName, null );
        }
        public <T> T getValue( User user ) throws Exception {
            Property prop = (Property)descriptor.getReadMethod().invoke( user );
            return (T)prop.get();
        }
    }
    
    /**
     * Access a property of the address of {@link User}. 
     */
    static class AddressPropertyAccessor
            extends PropertyAccessor {
        
        public AddressPropertyAccessor( String propName ) throws IntrospectionException {
            descriptor = new PropertyDescriptor( propName, Address.class, propName, null );
        }
        @Override
        public <T> T getValue( User user ) throws Exception {
            Property prop = (Property)descriptor.getReadMethod().invoke( user.address().get() );
            return (T)prop.get();
        }
    }
    
    @Override
    public PropertyAccessor load( String propName ) throws IntrospectionException {
        return propName.equals( "street" ) || propName.equals( "city" )
                ? new AddressPropertyAccessor( propName )
                : new PropertyAccessor( propName );
    }


    @Override
    public int size() throws RuntimeException {
        return 1024;
    }


    @Override
    public boolean select( Viewer _viewer, Object parentElm, Object elm ) {
        if (filterText != null /*&& filterText.length() >= 3*/) {
            User user = (User)elm;
            for (String filterPart : StringUtils.split( filterText )) {
                boolean partFound = false;                
                for (String propName : searchPropNames) {
                    try {
                        PropertyAccessor accessor = accessors.get( propName, this );
                        Object value = accessor.getValue( user );
                        if (value != null && value.toString().toLowerCase().contains( filterPart )) {
                            partFound = true; 
                            break;
                        }
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                }
                if (!partFound) {
                    return false;
                }
            }
            return true;
        }
        else {
            return true;
        }
    }
    
}
