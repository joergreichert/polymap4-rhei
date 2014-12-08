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
package org.polymap.rhei.batik.app;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.rhei.batik.IPanel;

/**
 * Handles multiple {@link IStatus} coming from different senders of an
 * {@link IPanel}. The highest severity status is set as active status of the panel.
 * Optionaly the enable state of a submit {@link Enableable} (IAction or Button) is
 * managed.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SubmitStatusManager {

    private static Log log = LogFactory.getLog( SubmitStatusManager.class );
    
    private IPanel                      panel;
    
    private Map<Object,IStatus>         status = new HashMap();
    
    private Enableable                  submit;
    
    private Enableable                  revert;
    
    
    public SubmitStatusManager( IPanel panel ) {
        this.panel = panel;
    }

    public SubmitStatusManager setSubmit( Enableable submit ) {
        this.submit = submit;
        return this;
    }

    public Enableable getSubmit() {
        return submit;
    }
    
    public SubmitStatusManager setRevert( Enableable revert ) {
        this.revert = revert;
        return this;
    }

    public Enableable getRevert() {
        return revert;
    }
    
    public IStatus updateStatusOf( Object sender, IStatus newStatus ) {
        assert sender != null;
        IStatus result = newStatus != null
            ? status.put( sender, newStatus )
            : status.remove( sender );
        updateUI();
        return result;
    }
    
    protected void updateUI() {
        IStatus highestSeverity = Status.OK_STATUS;
        for (IStatus s : status.values()) {
            highestSeverity = highestSeverity == Status.OK_STATUS || highestSeverity.getSeverity() < s.getSeverity() 
                    ? s : highestSeverity;
            log.info( "    checking: " + s + " -> highest: " + highestSeverity );
        }
        
        log.info( "highestSeverity: " + highestSeverity );
        panel.getSite().setStatus( highestSeverity );
        
        if (submit != null) {
            submit.setEnabled( highestSeverity.isOK() && highestSeverity != Status.OK_STATUS );
        }
    }
    
}
