/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.rhei.field;

import org.polymap.rhei.form.IFormToolkit;

public abstract class DelegateSite
        implements IFormFieldSite {

    private IFormFieldSite delegate;
    private IEventAware eventAware;

    public DelegateSite( IFormFieldSite delegate, IEventAware eventAware  ) {
        this.delegate = delegate;
        this.eventAware = eventAware;
    }


    public void addChangeListener( IFormFieldListener l ) {
        delegate.addChangeListener( l );
    }


    public void fireEvent(Object source, int eventCode, Object newValue ) {
        eventAware.fireEvent( source, eventCode, newValue );
    }


    public String getErrorMessage() {
        return delegate.getErrorMessage();
    }


    public String getFieldName() {
        return delegate.getFieldName();
    }


    public IFormToolkit getToolkit() {
        return delegate.getToolkit();
    }


    public boolean isDirty() {
        return delegate.isDirty();
    }


    public boolean isValid() {
        return delegate.isValid();
    }


    public void removeChangeListener( IFormFieldListener l ) {
        delegate.removeChangeListener( l );
    }


    public void setErrorMessage( String msg ) {
        delegate.setErrorMessage( msg );
    }
}
