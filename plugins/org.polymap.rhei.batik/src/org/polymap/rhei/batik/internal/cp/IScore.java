/* 
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.batik.internal.cp;

/**
 * The score of a {@link ISolution}.
 * <p/>
 * Score instances are immutable.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IScore
        extends Comparable<IScore> {

    public static final IScore INVALID = new IScore() {
        @Override
        public int compareTo( IScore o ) {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public <T extends IScore> T add( T o ) {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public IScore prioritize( int priority ) {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }
    };

    /**
     * Adds the given
     * <p/>
     * XXX The method name "add" is maybe neither intuitive nor correct.
     *
     * @param o
     * @return A newly created instance representing the result.
     */
    public <T extends IScore> T add( T o );
    
    public IScore prioritize( int priority );

}
