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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;
import org.polymap.rhei.form.IFormToolkit;

/**
 * A form field using a {@link Spinner} widget.
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class SpinnerFormField
        implements IFormField {

    private static Log     log             = LogFactory.getLog( SpinnerFormField.class );

    // instance *******************************************

    private IFormFieldSite site;

    private Spinner        spinner;

    private Object         loadedValue;

    private Double         min             = null;

    private Double         max             = null;

    private Double         increment       = null;

    private Double         defaultValue    = null;

    private Integer        digitCount      = null;

    private boolean        deferredEnabled = true;


    /**
     * Creates a spinner for values in range of the given minimum and maximum value.
     * Default value will be the minimum value and increment will be 1.
     * 
     * @param aMin
     * @param aMax
     */
    public SpinnerFormField( int aMin, int aMax ) {
        this( aMin, aMax, 1, aMin, 0 );
    }


    /**
     * Creates a spinner for values in range of the given minimum and maximum value.
     * The given default value will be set. Spinner increment will be 1.
     * 
     * @param aMin
     * @param aMax
     */
    public SpinnerFormField( int aMin, int aMax, int aDefaultValue ) {
        this( aMin, aMax, 1, aDefaultValue, 0 );
    }


    /**
     * Creates a spinner for values in range of the given minimum and maximum value.
     * The default value will be the minimum value. The parameter anIncrement will be
     * used to calculate the digit count to be assumed for all values. So if aMin,
     * aMax, or aDefaultValue has more digits as anIncrement, they will be cut off.
     * 
     * @param aMin
     * @param aMax
     * @param anIncrement
     */
    public SpinnerFormField( double aMin, double aMax, double anIncrement ) {
        this( aMin, aMax, 1, aMin, calculateDigitCount( anIncrement ) );
    }


    private static int calculateDigitCount( double anIncrement ) {
        String doubleStr = String.valueOf( anIncrement );
        String[] parts = doubleStr.split( "\\." );
        return parts.length > 1 ? parts[1].length() : 0;
    }


    /**
     * Creates a spinner for values in range of the given minimum and maximum value.
     * The given default value will and increment will be set. The parameter
     * aDigitCount is used to scale aMin, aMax, anIncrement and aDefaultValue, as an
     * spinner is only able to handle integer values.
     * 
     * @param aMin minimum number for spinner to accept
     * @param aMax maximum number for spinner to accept
     * @param anIncrement number to add resp. substract from the current selection
     *        when using the spinner down resp. up button
     * @param aDefaultValue the initial value to select in the spinner
     * @param aDigitCount digits to show inside the spinner field
     */
    public SpinnerFormField( double aMin, double aMax, double anIncrement, double aDefaultValue, int aDigitCount ) {
        this.min = aMin;
        this.max = aMax;
        this.increment = anIncrement;
        this.defaultValue = aDefaultValue;
        this.digitCount = aDigitCount;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.polymap.rhei.field.IFormField#init(org.polymap.rhei.field.IFormFieldSite)
     */
    @Override
    public void init( IFormFieldSite aSite ) {
        this.site = aSite;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.rhei.field.IFormField#dispose()
     */
    @Override
    public void dispose() {
        spinner.dispose();
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.polymap.rhei.field.IFormField#createControl(org.eclipse.swt.widgets.Composite
     * , org.polymap.rhei.form.IFormToolkit)
     */
    @Override
    public Control createControl( Composite parent, IFormToolkit toolkit ) {
        spinner = toolkit
                .createSpinner( parent, getMin(), getMax(), getIncrement(), getDefaultValue(), getDigitCount() );

        // modify listener
        spinner.addModifyListener( new ModifyListener() {

            public void modifyText( ModifyEvent ev ) {
                log.debug( "modifyEvent(): selection= " + unscale(spinner.getSelection()) );
                site.fireEvent( SpinnerFormField.this, IFormFieldListener.VALUE_CHANGE, loadedValue == null
                        && spinner.getText().equals( "" ) ? null : unscale(spinner.getSelection()) );
            }
        } );
        // focus listener
        spinner.addFocusListener( new FocusListener() {

            public void focusLost( FocusEvent event ) {
                site.fireEvent( SpinnerFormField.this, IFormFieldListener.FOCUS_LOST, unscale(spinner.getSelection()) );
            }


            public void focusGained( FocusEvent event ) {
                site.fireEvent( SpinnerFormField.this, IFormFieldListener.FOCUS_GAINED, unscale(spinner.getSelection()) );
            }
        } );
        spinner.setEnabled( deferredEnabled );
        return spinner;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.rhei.field.IFormField#setEnabled(boolean)
     */
    @Override
    public IFormField setEnabled( boolean enabled ) {
        if (spinner != null) {
            spinner.setEnabled( enabled );
        }
        else {
            deferredEnabled = enabled;
        }
        return this;
    }


    /**
     * Explicitly set the value of the text field. This causes events to be fired
     * just like the value was typed in.
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.rhei.field.IFormField#setValue(java.lang.Object)
     */
    public IFormField setValue( Object object ) {
        if (!(object instanceof Double)) {
            throw new IllegalArgumentException( object + " isn't a number." );
        }
        Double value = (Double)object;
        if (value < getMin() || value > getMax()) {
            throw new IllegalArgumentException( value + " isn't in range of [" + getMin() + ", " + getMax() + "]." );
        }
        int intValue = getScaled( value );
        spinner.setSelection( intValue );
        return this;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.rhei.field.IFormField#load()
     */
    @Override
    public void load() throws Exception {
        assert spinner != null : "Control is null, call createControl() first.";

        loadedValue = site.getFieldValue();
        Double value = null;
        if (loadedValue instanceof Integer) {
            value = Double.valueOf( (Integer)loadedValue );
        }
        else if (loadedValue instanceof Double) {
            value = (Double)loadedValue;
        }
        spinner.setSelection( loadedValue != null ? getScaled( value ) : getScaled( getDefaultValue() ) );
    }


    private Integer getScaled( Double value ) {
        int times = Double.valueOf( Math.pow( 10, getDigitCount() ) ).intValue();
        return Double.valueOf( value * times ).intValue();
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.rhei.field.IFormField#store()
     */
    @Override
    public void store() throws Exception {
        site.setFieldValue( unscale(spinner.getSelection()) );
    }


    private Object unscale( Integer value ) {
        if(digitCount != null && digitCount > 0) {
            int times = Double.valueOf( Math.pow( 10, getDigitCount() ) ).intValue();
            return Double.valueOf( new Double(value) / new Double(times) );
        } else {
            return value;
        }
    }


    public Double getMin() {
        return min;
    }


    public void setMin( Double min ) {
        this.min = min;
    }


    public Double getMax() {
        return max;
    }


    public void setMax( Double max ) {
        this.max = max;
    }


    public Double getIncrement() {
        return increment;
    }


    public void setIncrement( Double increment ) {
        this.increment = increment;
    }


    public Double getDefaultValue() {
        return defaultValue;
    }


    public void setDefaultValue( Double defaultValue ) {
        this.defaultValue = defaultValue;
    }


    public Integer getDigitCount() {
        return digitCount;
    }


    public void setDigitCount( Integer digitCount ) {
        this.digitCount = digitCount;
    }
}
