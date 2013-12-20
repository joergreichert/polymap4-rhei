/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.batik.toolkit;

import com.google.common.base.Joiner;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutElement;

/**
 * 
 */
public class TestLayoutElement
        extends LayoutElement {

    public String                   title;
    
    private ConstraintData          constraints;
    
    public TestLayoutElement( String title, int y, int height, LayoutConstraint... constraints ) {
        this.title = title;
        this.y = y;
        this.height = height;
        this.constraints = new ConstraintData( constraints );
    }

    
    @Override
    protected TestLayoutElement clone() {
        TestLayoutElement result = (TestLayoutElement)super.clone();
        return result;
    }


    @Override
    public <T extends LayoutConstraint> T constraint( Class<T> type, T defaultValue ) {
        return constraints.constraint( type, defaultValue );
    }

//    @Override
//    public int computeWidth( int wHint ) {
//        // XXX Auto-generated method stub
//        throw new RuntimeException( "not yet implemented." );
//    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public boolean equals( Object obj ) {
        return obj == this;
    }

    @Override
    public String toString() {
        return Joiner.on( "" ).join( "[y=", y, ",control=", title, "]" );  
    }
    
}