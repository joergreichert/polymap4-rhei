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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ColorFormField
        extends AbstractDelegatingFormField<RGB> {

    @Override
    protected String getInitialLabel() {
        return "Choose...";
    }
    
    @Override
    protected String getButtonText( Button button ) {
        return "";
    }

    @Override
    protected String getDisabledButtonText( Button button ) {
        return getInitialLabel();
    }

    @Override
    protected Color getButtonBackground( Button button ) {
        Color color = null;
        if(getCurrentValue() != null) {
            color = new Color( Display.getDefault(), getCurrentValue() );
        }
        return color;
    }

    @Override
    protected Color getDisabledButtonBackground( Button button ) {
        return null;
    }
    

    @Override
    protected void processLoadedValue( Object loadedValue ) {
        setCurrentValue( loadedValue instanceof RGB ? (RGB)loadedValue : null );
    }


    @Override
    public IFormField setValue( Object value ) {
        if (value instanceof RGB) {
            setCurrentValue( (RGB)value );
            updateButton();
        }
        return this;
    }
}
