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

import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.UIJob;

import org.polymap.rhei.fulltext.FullTextIndex;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FulltextProposal {

    private static Log log = LogFactory.getLog( FulltextProposal.class );
    
    private FullTextIndex                   index;
    
    private Text                            control;
    
    private String                          field;

    private SimpleContentProposalProvider   proposalProvider;

    private XContentProposalAdapter         proposal;

    protected String                        currentSearchTxtValue;
    
    private boolean                         eventOnAccept = true;


    public FulltextProposal( FullTextIndex index, final Text control ) {
        this.index = index;
        this.control = control;

        // proposal
        proposalProvider = new SimpleContentProposalProvider( new String[0] );
        TextContentAdapter controlAdapter = new TextContentAdapter() {
            public void insertControlContents( @SuppressWarnings("hiding") Control control, String text, int cursorPosition ) {
                ((Text)control).setText( text );
                ((Text)control).setSelection( text.length() );
            }
        };
        proposal = new XContentProposalAdapter( control, controlAdapter, proposalProvider, null, null );
        //proposal.setAutoActivationDelay( 2500 );
        
        proposal.addContentProposalListener( new IContentProposalListener() {
            public void proposalAccepted( IContentProposal _proposal ) {
                if (eventOnAccept) {
                    Event event = new Event();
                    event.keyCode = SWT.Selection;
                    event.display = control.getDisplay();
                    event.type = SWT.KeyUp;
                    control.notifyListeners( SWT.KeyUp, event );
                }
            }
        });
        
        control.addKeyListener( new KeyAdapter() {
            public void keyReleased( KeyEvent ev ) {
                if (ev.keyCode == SWT.ARROW_DOWN) {
                    proposal.setProposalPopupFocus();
                }
                else {
                    // close popup: visual feedback for user that key has been recognized;
                    // also, otherwise proposal would check in the current entries
                    proposalProvider.setProposals( new String[0] );
                    //proposal.closeProposalPopup();

                    currentSearchTxtValue = control.getText();
                    if (currentSearchTxtValue.length() < 1) {
                        proposalProvider.setProposals( new String[0] );
                    }
                    else {
                        new ProposalJob().schedule( 2000 );
                    }
                }
            }
        });
        
//        control.addModifyListener( new ModifyListener() {
//            public void modifyText( ModifyEvent ev ) {
//                currentSearchTxtValue = control.getText();
//                new ResultCountJob().schedule( 2000 );
//            }
//        });
//        control.addListener( SWT.DefaultSelection, new Listener() {
//            public void handleEvent( Event ev ) {
//                try {
//                    String txt = control.getText();
//                    if (txt.length() == 0) {
//                        return;
//                    }
//                    Iterable<JSONObject> results = addressIndex.search( txt, 100 );
//
//                    zoomResults( results );
//                }
//                catch (Exception e) {
//                    log.warn( "", e ); //$NON-NLS-1$
//                    BatikApplication.handleError( "", e ); //$NON-NLS-1$
//                }
//            }
//        });
    }    

    
    
    /**
     * The field to make the {@link FullTextIndex#propose(String, int, String) proposal} for.
     * 
     * @param field The name of the field or null.
     * @return this
     */
    public FulltextProposal setField( String field ) {
        this.field = field;
        return this;
    }

    /**
     * @see #setEventOnAccept(boolean)
     */
    public boolean isEventOnAccept() {
        return eventOnAccept;
    }
    
    /**
     * True specifies that a {@link SWT#Selection} event is send to the Text control.
     * Fpr {@link AbstractSearchField} this triggers the search to be performed.
     * <p/>
     * Default: true
     */
    public FulltextProposal setEventOnAccept( boolean eventOnAccept ) {
        this.eventOnAccept = eventOnAccept;
        return this;
    }


    /**
     * Updates the ...? 
     */
    class ProposalJob
            extends UIJob {

        private String      value = control.getText();
        
        public ProposalJob() {
            super( "Adressfeld suchen" );
        }

        @Override
        protected void runWithException( IProgressMonitor monitor ) throws Exception {
            // skip if control is disposed or no longer focused
            if (control == null || control.isDisposed()) {
                log.info( "Control is disposed." );
                return;
            }
            // skip if search text has changed
            if (value != currentSearchTxtValue) {
                log.info( "Search text has changed: " + value + " -> " + currentSearchTxtValue );
                return;
            }

            // find proposals
            final String[] results = Iterables.toArray( index.propose( value, 10, field ), String.class );

            // display
            control.getDisplay().asyncExec( new Runnable() {
                public void run() {
                    if (!control.isFocusControl()) {
                        log.info( "Control is no longer focused." );
                        return;
                    }
                    proposalProvider.setProposals( results );
                    if (results.length > 0 && !results[0].equals( value )) {
                        proposal.openProposalPopup();
                        //proposal.setProposalPopupFocus();
                    }
                    else {
                        proposal.closeProposalPopup();
                    }
                }
            });
        }        
    }

    
    /**
     * Expose some protected methods.
     */
    class XContentProposalAdapter
            extends ContentProposalAdapter {

        public XContentProposalAdapter( Control control, IControlContentAdapter controlContentAdapter,
                IContentProposalProvider proposalProvider, KeyStroke keyStroke, char[] autoActivationCharacters ) {
            super( control, controlContentAdapter, proposalProvider, keyStroke, autoActivationCharacters );
        }

        @Override
        protected void closeProposalPopup() {
            super.closeProposalPopup();
        }

        @Override
        protected void openProposalPopup() {
            super.openProposalPopup();
        }
        
    }

}
