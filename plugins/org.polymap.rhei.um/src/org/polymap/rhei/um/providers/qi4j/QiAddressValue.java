/* 
 * polymap.org
 * Copyright (C) 2013, Polymap GmbH. All rights reserved.
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
 */
package org.polymap.rhei.um.providers.qi4j;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import org.polymap.rhei.um.Address;

/**
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface QiAddressValue
        extends ValueComposite {
    
    @Optional
    public Property<String> _street();
    
    @Optional
    public Property<String> _number();
    
    @Optional
    public Property<String> _postalCode();
    
    @Optional
    public Property<String> _city();

    @Optional
    public Property<String> _district();

    @Optional
    public Property<String> _province();    
    
    @Optional
    public Property<String> _country();    

    
    /**
     * 
     */
    public static class QiAddress
            implements Address {
    
        private QiAddressValue              delegate;
        
        private Property<QiAddressValue>    valueProp;

        
        public QiAddress( QiAddressValue delegate, Property<QiAddressValue> valueProp ) {
            this.delegate = delegate;
            this.valueProp = valueProp;
        }

        @Override
        public org.polymap.rhei.um.Property<String> street() {
            return QiValueProperty.create( delegate._street(), valueProp );
        }

        @Override
        public org.polymap.rhei.um.Property<String> number() {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public org.polymap.rhei.um.Property<String> postalCode() {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public org.polymap.rhei.um.Property<String> city() {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public org.polymap.rhei.um.Property<String> district() {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public org.polymap.rhei.um.Property<String> province() {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public org.polymap.rhei.um.Property<String> country() {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }
        
    }
    
}
