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
package org.polymap.rhei.batik.tx;

import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelPath;

/**
 * Provides a {@link Context} variable that holds a transaction and allows to
 * {@link Propagation propagate} to child panels.
 *
 *@param <T> The actual transaction type mahaged by this provider.
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class TxProvider<T> {

    private static Log log = LogFactory.getLog( TxProvider.class );
    
    /** 
     * Types a transaction propagation.
     */
    public static enum Propagation {
        /**
         * Same {@link UnitOfWork} will be used if there is an already opened
         * {@link UnitOfWork} in the current context. If there is no existing
         * UnitOfWork this holder will create a new one for the panel.
         */
        REQUIRED,
        /**
         * A new {@link UnitOfWork} will always be created for this panel. In other
         * words the inner transaction may commit or rollback independently of the
         * outer transaction, i.e. the outer transaction will not be affected by the
         * inner transaction result: they will run in distinct physical transactions.
         */
        REQUIRES_NEW,
        /**
         * Creates a new {@link UnitOfWork} for this panel only. This is similar the
         * {@link #REQUIRES_NEW} except that the UnitOfWork is hidden from the child
         * panels so that {@link #REQUIRED} will create a new UnitOfWork and
         * {@link #MANDATORY} throws an exception. In other words this requests a
         * child panel to manage its own UnitOfWork.
         */
        REQUIRES_NEW_LOCAL,
        /**
         * A new {@link UnitOfWork#newUnitOfWork() nested} UnitOfWork will always be
         * created for this panel. The inner transaction may rollback independently
         * of the outer transaction. Committing the nested UnitOfWork writes down the
         * modifications to the parent without changing the underlying store.
         */
        NESTED,
        /**
         * States that an existing opened {@link UnitOfWork} must already exist. If
         * not an exception will be thrown.
         */
        MANDATORY
    }
    
    /**
     * 
     */
    public static enum Completion {
        /**
         * {@link UnitOfWork#commit() Commit} and {@link UnitOfWork#close() close}
         * the {@link UnitOfWork} of this panel.
         */
        COMMIT,
        /**
         * {@link UnitOfWork#rollback() Revert} all modification and
         * {@link UnitOfWork#close() close} the {@link UnitOfWork} of this panel.
         */
        REVERT
    }

    
    /**
     * Supplier of the actual underlying transaction.
     */
    public class Tx
            implements Supplier<T> {

        private IPanel                  panel;
        
        private Propagation             propagation;
        
        private T                       t;
        
        
        protected Tx( IPanel panel ) {
            this.panel = panel;
        }

        @Override
        public T get() {
            return t;
        }
        
        /**
         * 
         * @param propagation
         * @return this
         */
        public Tx start( @SuppressWarnings("hiding") Propagation propagation ) {
            assert t == null : "Tx started already and not yet completed.";
            
            this.propagation = propagation;
            
            //
            if (propagation == Propagation.REQUIRES_NEW
                    || propagation == Propagation.REQUIRES_NEW_LOCAL) {
                t = newTx( (T)null );
            }
            //
            else if (propagation == Propagation.MANDATORY) {
                Tx parentTx = parentTx()
                        .orElseThrow( () -> new IllegalStateException( "MANDATORY: parent panel has no UnitOfWork started." ) );
                if (parentTx.propagation == Propagation.REQUIRES_NEW_LOCAL) {
                    new IllegalStateException( "MANDATORY: parent UnitOfWork is REQUIRES_NEW_LOCAL" );
                }
                t = parentTx.get();
            }
            //
            else if (propagation == Propagation.NESTED) {
                Tx parentTx = parentTx()
                        .orElseThrow( () -> new IllegalStateException( "MANDATORY: parent panel has no UnitOfWork started." ) );
                if (parentTx.propagation == Propagation.REQUIRES_NEW_LOCAL) {
                    new IllegalStateException( "NESTED: parent UnitOfWork is REQUIRES_NEW_LOCAL" );
                }
                t = newTx( parentTx.get() );
            }
            // fail fast
            else {
                throw new IllegalStateException( "Unhandled propagation type: " + propagation );
            }
            return this;
        }
        
        /**
         * 
         * @param completion
         * @return this
         */
        public Tx endTx( Completion completion ) {
            assert t != null : "No tx has been started.";
            //
            if (propagation == Propagation.MANDATORY) {
                throw new IllegalStateException( "MANDATORY: closing parent UnitOfWork is not permitted." );
            }
            //
            if (propagation == Propagation.REQUIRED) {
            }
            
            //
            if (completion == Completion.COMMIT) {
                commitTx( t );
            }
            //
            else if (completion == Completion.REVERT) {
                rollbackTx( t );
            }
            else {
                throw new IllegalStateException( "Unhandled completion type: " + completion );
            }
            closeTx( t );
            t = null;
            propagation = null;
            return this;
        }        
        
        protected Optional<Tx> parentTx() {
            PanelPath panelPath = panel.getSite().getPath();
            if (panelPath.size() <= 1) {
                return Optional.empty();
            }
            else {
                PanelPath parentPath = panelPath.removeLast( 1 );
                return panels.entrySet().stream()
                        .filter( entry -> entry.getKey().getSite().getPath().equals( parentPath ) )
                        .map( entry -> entry.getValue() )
                        .findFirst();
            }
        }
    }

    
    // instance *******************************************

    private Lazy<T>                     rootTx = new LockedLazyInit( () -> newTx( (T)null ) );
    
    private WeakHashMap<IPanel,Tx>      panels = new WeakHashMap();
    

    public TxProvider() {
//        BatikApplication.instance().getContext().addListener( this, ev -> panels.containsKey( ev.getPanel() ) );
    }

    /**
     * 
     *
     * @param parent The parent transaction, or null.
     * @return
     */
    protected abstract T newTx( T parent );
    
    protected abstract void commitTx( T tx );
    
    protected abstract void rollbackTx( T tx );
    
    protected abstract void closeTx( T tx );
    
    
    public Tx newTx( IPanel panel ) {
        Tx result = new Tx( panel );
        if (panels.put( panel, result ) != null) {
            throw new UnsupportedOperationException( "Multiple transactions per panel are not supported yet." );
        }
        return result;
    }

    
    protected void panelChanged( PanelChangeEvent ev ) {
        throw new RuntimeException( "not yet implemented" );
    }
    
}
