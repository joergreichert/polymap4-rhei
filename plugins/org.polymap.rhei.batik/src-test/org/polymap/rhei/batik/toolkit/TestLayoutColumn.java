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

import java.util.Arrays;

import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutColumn;
import org.polymap.rhei.batik.toolkit.ConstraintLayout.LayoutElement;

/**
 * 
 */
public class TestLayoutColumn
        extends LayoutColumn {

    public TestLayoutColumn( TestLayoutElement... elms ) {
        addAll( Arrays.asList( elms ) );
    }

    @Override
    public void justifyElements() {
//        assert width > 0;
        height = 0;
        for (LayoutElement elm : this) {
//            MinHeightConstraint minHeight = elm.constraint( MinHeightConstraint.class, 
//                    new MinHeightConstraint( SWT.DEFAULT, -1 ) );
//            elm.height = elm.control.computeSize( width, minHeight.getValue() ).y;
            elm.y = height;
            
            height += elm.height;
        }
    }
    
}