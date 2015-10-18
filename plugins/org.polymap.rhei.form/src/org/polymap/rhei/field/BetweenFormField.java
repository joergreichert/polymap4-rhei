/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * $Id: $
 */
package org.polymap.rhei.field;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.polymap.rhei.form.IFormToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class BetweenFormField
        extends AbstractFieldFormPair {


    // Date helpers ***************************************

    public static Date dayStart( Date date ) {
        Calendar cal = Calendar.getInstance( Locale.GERMANY );
        cal.setTime( date );
        cal.set( Calendar.HOUR_OF_DAY, 0 );
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.SECOND, 0 );
        cal.set( Calendar.MILLISECOND, 0 );
        return cal.getTime();
    }


    public static Date dayEnd( Date date ) {
        Calendar cal = Calendar.getInstance( Locale.GERMANY );
        cal.setTime( date );
        cal.set( Calendar.HOUR_OF_DAY, 0 );
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.SECOND, 0 );
        cal.set( Calendar.MILLISECOND, 0 );
        cal.add( Calendar.DAY_OF_MONTH, 1 );
        return cal.getTime();
    }

    // instance *******************************************

    public BetweenFormField( IFormField field1, IFormField field2 ) {
        super(field1, field2);
    }

    public Control createControl( Composite parent, IFormToolkit toolkit ) {
        Composite contents = toolkit.createComposite( parent );
        RowLayout layout = new RowLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.spacing = 3;
        // layout.justify = true;
        layout.center = true;
        contents.setLayout( layout );

        field1.createControl( contents, toolkit );
        toolkit.createLabel( contents, "bis" );
        field2.createControl( contents, toolkit );

        contents.pack( true );
        return contents;
    }

    /* (non-Javadoc)
     * @see org.polymap.rhei.field.AbstractFieldFormPair#postProcessValues()
     */
    @Override
    protected void postProcessValues() {
        if (newValue1 instanceof Comparable && newValue2 instanceof Comparable) {
            Comparable c1 = (Comparable)newValue1;
            Comparable c2 = (Comparable)newValue2;
            if (c1.compareTo( c1 ) > 0) {
                newValue2 = newValue1;
            }
        }
    }
}
