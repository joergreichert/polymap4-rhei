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
package org.polymap.rhei.fulltext.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rap.rwt.graphics.Graphics;

import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.config.Check;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.DefaultInt;
import org.polymap.core.runtime.config.NumberRangeValidator;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.fulltext.FulltextPlugin;

/**
 * A text search field with clear button and deferred search execution running inside
 * a {@link Job}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class AbstractSearchField
        extends Configurable {

    private static Log log = LogFactory.getLog( AbstractSearchField.class );
    
    protected Composite                         container;
    
    protected Text                              searchTxt;
    
    protected Label                             clearBtn;
    
    protected String                            filterText;
    
    /**
     * True specifies that the search is performed when keyboard Enter is pressed.
     * Otherwise the search is started automatically (after
     * {@link #searchDelayMillis}) when input has changed.
     */
    @DefaultBoolean( true )
    public Config2<AbstractSearchField,Boolean>  searchOnEnter;

    /**
     * The delay before auto search starts. Only used if {@link #searchOnEnter} is
     * set to false.
     */
    @DefaultInt( 750 )
    @Check( value=NumberRangeValidator.class, args={"0","10000"} )
    public Config2<FulltextProposal,Integer>     searchDelayMillis;

    
    /**
     * Constructs a new instance.
     *  
     * @param _parent
     */
    public AbstractSearchField( Composite _parent  ) {
        container = new Composite( _parent, SWT.NONE );
        container.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );

        clearBtn = new Label( container, SWT.PUSH | SWT.SEARCH );
        clearBtn.setToolTipText( "Zurücksetzen" );
        clearBtn.setImage( FulltextPlugin.images().image( "icons/delete_edit.gif" ) );
        clearBtn.setLayoutData( FormDataFactory.filled().top( 0, 5 ).right( 100, -5 ).left( -1 ).create() );
        clearBtn.addMouseListener( new MouseAdapter() {
            public void mouseUp( MouseEvent e ) {
                searchTxt.setText( "" );
                filterText = searchTxt.getText();
                search( searchTxt.getDisplay(), filterText );
            }
        });
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

        searchTxt.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                filterText = searchTxt.getText();  //.toLowerCase();

                clearBtn.setVisible( filterText.length() > 0 );
                
                if (!searchOnEnter.get()) {                
                    // Job: defer refresh for 2s
                    new UIJob( "Fulltext search" ) {

                        String myFilterText = filterText;

                        @Override
                        protected void runWithException( IProgressMonitor monitor ) throws Exception {
                            if (myFilterText != filterText) {
                                log.info( "Text changed: " + myFilterText + " -> " + filterText );
                                return;
                            }
                            search( getDisplay(), filterText );
                        }
                    }.schedule( searchDelayMillis.get() );
                }
            }
        });
        searchTxt.addKeyListener( new KeyAdapter() {
            // FullTextProposal depends on this event type "KeyUp"
            public void keyReleased( KeyEvent ev ) {
                if (ev.keyCode == SWT.Selection && searchOnEnter.get()) {
                    filterText = searchTxt.getText();
                    search( searchTxt.getDisplay(), filterText );
                }
            }
        });
    }

    
    protected void search( Display display, String query ) {
        try {
            doSearch( filterText );
        }
        catch (Exception e) {
            FulltextPlugin.instance().handleError( "Suche konnte nicht erfolgreich beendet werden.", e );
        }
        display.asyncExec( new Runnable() {
            @Override
            public void run() {
                clearBtn.setVisible( filterText.length() > 0 );
                doRefresh();
            }
        });        
    }
    
    
    /**
     * Performs the search for the given text query from the input field. This method
     * is called inside a {@link UIJob}. Refreshing the UI is done by
     * {@link #doRefresh()} inside the display thread.
     * 
     * @see #doRefresh()
     * @throws Exception
     */
    protected abstract void doSearch( String query ) throws Exception;
    

    /**
     * Updates the UI after {@link #doSearch(String)} has been performed.
     *
     * @see #doSearch(String)
     */
    protected abstract void doRefresh();
    
    
    public Composite getControl() {
        return container;
    }


    public Text getText() {
        return searchTxt;
    }

}
