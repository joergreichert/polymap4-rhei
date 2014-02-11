/* 
 * polymap.org
 * Copyright (C) 2009-2014, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.fulltext.servlet;

import static org.polymap.rhei.fulltext.FullTextIndex.FIELD_GEOM;
import static org.polymap.rhei.fulltext.FullTextIndex.FIELD_SRS;
import static org.polymap.rhei.fulltext.FullTextIndex.FIELD_TITLE;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.StringWriter;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import com.sun.syndication.feed.module.georss.GeoRSSModule;
import com.sun.syndication.feed.module.georss.SimpleModuleImpl;
import com.sun.syndication.feed.module.georss.geometries.Position;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.util.Geometries;

/**
 * This stream encodes {@link SimpleFeature} and {@link Document} objects into
 * GeoRSS stream. Later this may also extend to be a {@link PipelineProcessor}
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class GeoRssEncoder
        extends DataOutputStream
        //extends ObjectOutputStream
        implements ObjectOutput {

    private static final Log log = LogFactory.getLog( GeoRssEncoder.class );

    private static final GeometryFactory gf = JTSFactoryFinder.getGeometryFactory( null );

    private boolean                     streamStarted;
    
    private CoordinateReferenceSystem   dataCRS, worldCRS;
    
    private MathTransform               transform;

    private List<SyndEntry>             feedEntries;
    
    private String                      baseURL;
    
    private String                      feadTitle;
    
    private String                      description;          
    

    public GeoRssEncoder( OutputStream out, CoordinateReferenceSystem worldCRS,
            String baseURL, String title, String description ) throws IOException {
        super( out );
        assert baseURL != null;
        this.worldCRS = worldCRS;
        this.baseURL = baseURL;
        this.feadTitle = title;
        this.description = description;
    }

    
    public void writeObject( Object obj ) throws IOException {
        if (!streamStarted) {
            startStream();
        }
        try {
            if (obj instanceof SimpleFeature) {
                encodeFeature( (SimpleFeature)obj );
            }
            else if (obj instanceof JSONObject) {
                encodeFeature( (JSONObject)obj );
            }
            else {
                throw new RuntimeException( "Unsupported object type: " + obj );
            }
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOException( e.getMessage(), e );
        }
    }

    
    protected void startStream() {
        assert !streamStarted;
        streamStarted = true;
        feedEntries = new LinkedList();
    }
    
    
    private boolean aboutToFlush = false;
    
    public synchronized void flush() throws IOException {
        if (aboutToFlush) {
            super.flush();
            return;
        }
        aboutToFlush = true;
        log.debug( "flush(): ..." );

        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType( "rss_2.0" );

        feed.setTitle( feadTitle != null ? feadTitle : "POLYMAP3|Atlas" );
        feed.setLink( baseURL );
        feed.setDescription( description != null ? description : "" );

        feed.setEntries( feedEntries );
        //WireFeed wiredFeed = feed.createWireFeed();
        
        SyndFeedOutput output = new SyndFeedOutput();
        try {
            //output.output( feed, new OutputStreamWriter( out, "ISO-8859-1" ) );

            StringWriter buf = new StringWriter();
            output.output( feed, buf );
            buf.flush();
            write( buf.toString().getBytes( "UTF-8" ) );
        } 
        catch (FeedException e) {
            log.warn( "unhandled: ", e );
        }

        aboutToFlush = false;
        super.flush();
//        feedEntries.clear();
    }


    protected void encodeFeature( SimpleFeature feature ) throws FactoryException {
        //
        CoordinateReferenceSystem featureCRS = feature.getFeatureType().getCoordinateReferenceSystem();
        if (!featureCRS.equals( dataCRS )) {
            boolean lenient = true; // allow for some error due to different datums
            transform = CRS.findMathTransform( dataCRS, worldCRS, lenient );
        }

        // entry
        SyndEntryImpl entry = new SyndEntryImpl();
        entry.setTitle( "Entry" );
        entry.setLink( "http://polymap.org/" );
        entry.setPublishedDate( new Date() );

        // description
        StringBuffer buf = new StringBuffer( 256 );
        Collection<Property> props = feature.getProperties();
        Point point = null;
        for (Property prop : props) {
            //log.info( prop.getName() + ": " + prop.getValue().getClass() );
            if (prop.getValue().getClass().equals( Point.class )) {
                point = (Point)prop.getValue();
            }
            else {
                String value = prop.getValue().toString();
                if (value != null && value.length() > 0) {
                    buf.append( "<b>" ).append( prop.getName() ).append( "</b>" );
                    buf.append( ": " ).append( escapeHtml( value ) );
                    buf.append( "<br/>" );
                }
            }
        }
        SyndContentImpl descr = new SyndContentImpl();
        descr.setType( "text/html" );
        descr.setValue( buf.toString() );
        entry.setDescription( descr );

        GeoRSSModule module = new SimpleModuleImpl();
        //GeometryAttribute geom = feature.getDefaultGeometryProperty();
        //geoRSSModule.setPosition( new Position( 54.2, 12.4 ) );

        // transform
        //point = (Point)JTS.transform( point, transform);

        module.setPosition( new Position( point.getY(), point.getX() ) );
        entry.getModules().add( module );

        feedEntries.add( entry );
        log.debug( "Feed entry added: " + entry );
    }

    
    protected void encodeFeature( JSONObject obj ) throws Exception {
        String title = obj.getString( FIELD_TITLE );
        Geometry geom = (Geometry)obj.get( FIELD_GEOM );
        String srs = obj.getString( FIELD_SRS );
        geom = Geometries.transform( geom, Geometries.crs( srs ), worldCRS );
        log.debug( "encoding: " + title );

        // entry
        SyndEntryImpl entry = new SyndEntryImpl();
        entry.setTitle( escapeHtml( title ) );
        entry.setLink( escapeHtml( baseURL + "?search=" + title ) );
        entry.setPublishedDate( new Date() );

//        // address
//        Address address = obj.getAddress();
//        StringBuffer addressBuf = new StringBuffer( 256 );
//        if (address != null && address.getStreet() != null) {
//            // postal code
//            String postalCode = StringUtils.contains( address.getPostalCode(), "-" )
//                    ? StringUtils.substringAfterLast( address.getPostalCode(), "-" )
//                    : address.getPostalCode();
//            
//            addressBuf.append( "<p><em>" ).append( escapeHtml( address.getStreet() ) )
//                    .append( " " ).append( escapeHtml( address.getNumber() ) )
//                    .append( "<br/>" )
//                    // remove country code from postal code
//                    .append( escapeHtml( postalCode ) )
//                    .append( " " ).append( escapeHtml( address.getCity() ) )
//                    .append( "</em></p>" );
//        }

        // geometry
        Point point = geom.getCentroid();
        Position pos = new Position( point.getY(), point.getX() );
        
        // description
        StringBuilder buf = new StringBuilder( 256 );
//        buf.append( addressBuf );
        for (String key : (Set<String>)obj.keySet()) {
            log.debug( "    field: " + key );
            String value = obj.getString( key );
            if (value != null && value.length() > 0) {
                buf.append( "<nobr><b>" ).append( escapeHtml( StringUtils.capitalize( key ) ) ).append( "</b>" );
                // URL?
                if (value.contains( "www." ) || value.contains( ".de" ) || value.contains( ".com" ) || value.contains( "http://" )) {
                    buf.append( ": " )
                        .append( "<a href=\"" ).append( value ).append( "\" target=\"atlas_content\">" ) 
                        .append( escapeHtml( value ) )
                        .append( "</a>" ); 
                }
                else {
                    buf.append( ": " ).append( escapeHtml( value ) );
                }
                buf.append( "</nobr><br/>" );
            }
        }
        SyndContentImpl descr = new SyndContentImpl();
        descr.setType( "text/html" );
        descr.setValue( buf.toString() );
        entry.setDescription(descr);

        GeoRSSModule module = new SimpleModuleImpl();
        //GeometryAttribute geom = feature.getDefaultGeometryProperty();
        //geoRSSModule.setPosition( new Position( 54.2, 12.4 ) );

        // transform
        //point = (Point)JTS.transform( point, transform);

        module.setPosition( pos );
        entry.getModules().add( module );

        feedEntries.add( entry );
        //log.info( "Feed entry added: " + entry );
    }
    
    
    protected String escapeHtml( String s ) {
        //return StringEscapeUtils.escapeHtml( s );
        return s;
    }
    
}
