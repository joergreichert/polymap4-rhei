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
package org.polymap.rhei.fulltext.update;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.runtime.FutureJobAdapter;
import org.polymap.core.runtime.SessionContext;
import org.polymap.core.runtime.UIJob;

import org.polymap.rhei.fulltext.FullTextIndex;
import org.polymap.rhei.fulltext.indexing.FeatureTransformer;
import org.polymap.rhei.fulltext.update.UpdateableFullTextIndex.Updater;

/**
 * Helper for {@link FullTextIndex} updates. Handles {@link FeatureTransformer
 * feature transformation} and proper creating/closing the underlying {@link Updater}
 * . The update runs inside a newly created {@link UIJob}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FullTextIndexUpdater {

    private static Log log = LogFactory.getLog( FullTextIndexUpdater.class );

    private List<FeatureTransformer>    transformers = new ArrayList();
    
    private boolean                     commitOnException = false;
    
    
    public void addTransformer( FeatureTransformer transformer ) {
        this.transformers.add( transformer );
    }
    
    
    public FullTextIndexUpdater setCommitOnException( boolean commitOnException ) {
        this.commitOnException = commitOnException;
        return this;
    }

    public boolean isCommitOnException() {
        return commitOnException;
    }
    
    
    protected abstract UpdateableFullTextIndex index();
    
    protected abstract void doUpdateIndex( Updater updater, IProgressMonitor monitor ) throws Exception;

    
    protected <F,T> T transform( F feature ) {
        Object transformed = feature;
        for (FeatureTransformer transformer : transformers) {
            transformed = transformer.apply( transformed );
        }
        return (T)transformed;
    }

    
    public Future<IStatus> start( SessionContext session ) {
        try {
            return session.execute( new Callable<Future<IStatus>>() {
                @Override
                public Future<IStatus> call() throws Exception {
                    UIJob job = new UIJob( "FullTextIndexUpdater" ) {
                        @Override
                        protected void runWithException( IProgressMonitor monitor ) throws Exception {
                            Updater updater = index().prepareUpdate();
                            try {
                                doUpdateIndex( updater, monitor );
                                updater.apply();
                            }
                            catch (Throwable e) {
                                if (commitOnException) {
                                    updater.apply();
                                }
                                throw e;
                            }
                            finally {
                                updater.close();
                            }
                        }
                    };
                    job.setPriority( Job.LONG );
                    job.schedule();
                    return new FutureJobAdapter<IStatus>( job );
                }
            });
        }
        catch (Exception e) {
            // we should never get here
            throw new RuntimeException( e );
        }        
    }
    
}
