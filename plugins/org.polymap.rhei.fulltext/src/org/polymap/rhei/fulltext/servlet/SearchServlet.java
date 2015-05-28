/*                                                                                           
 * polymap.org                                                                               
 * Copyright (C) 2010-2014, Polymap GmbH. All rights reserved.                                   
 *                                                                                           
 * This is free software; you can redistribute it and/or modify it                           
 * under the terms of the GNU Lesser General Public License as                               
 * published by the Free Software Foundation; either version 3 of                            
 * the License, or (at your option) any later version.                                       
 *                                                                                           
 * This software is distributed in the hope that it will be useful,                          
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                            
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU                          
 * Lesser General Public License for more details.                                           
 */
package org.polymap.rhei.fulltext.servlet;

import java.util.zip.GZIPOutputStream;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.fulltext.FulltextIndex;

/**
 * This servlet handles search and autocomplete requests. It provides data
 * as GeoRSS or KML.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SearchServlet
        extends HttpServlet {

    private static final Log log = LogFactory.getLog( SearchServlet.class );

    public static final int             DEFAULT_MAX_SEARCH_RESULTS = 300;

    public static final CoordinateReferenceSystem DEFAULT_WORLD_CRS;

    public static final CoordinateReferenceSystem WGS84;

    private FulltextIndex               dispatcher;
    
    
    static {
        try {
            DEFAULT_WORLD_CRS = CRS.decode( "EPSG:900913" );
            WGS84 = CRS.decode( "EPSG:4326" );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    

    public SearchServlet( FulltextIndex index ) throws Exception {
        log.info( "Initializing SearchServlet ..." );
        dispatcher = index;
    }


    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        log.info( "Request: " + request.getQueryString() );

        String srsParam = request.getParameter( "srs" );
        CoordinateReferenceSystem worldCRS = DEFAULT_WORLD_CRS;
        if (srsParam != null) {
            try {
                worldCRS = CRS.decode( srsParam );
            } 
            catch (Exception e) {
                worldCRS =  DEFAULT_WORLD_CRS;
            }
        }
        log.debug( "worldCRS: " + worldCRS );

	   	// completion request *****************************
	   	if (request.getParameter( "term" ) != null) {
            String searchStr = request.getParameter( "term" );
            searchStr = StringEscapeUtils.unescapeHtml4( searchStr );
	        
	        try {
	            JSONArray result = new JSONArray();

	            for (String record : dispatcher.propose( searchStr, 7, null )) {
	                //result.put( StringEscapeUtils.escapeHtml( record ) );
	                result.put( record );
	            }
	            
                log.info( "Response: " + result.toString() );
                response.setContentType( "application/json; charset=UTF-8" );
                response.setCharacterEncoding( "UTF-8" );
                response.getWriter().println( result.toString() );
            }
            catch (Exception e) {
                log.info( "Response: " + "Fehler: " + e.getMessage(), e );
                response.setContentType( "text/html" );
                response.getWriter().println( "Fehler: " + e.getMessage() );
            }
	   	}
	   	
	   	// content request (GeoRSS/KML/GeoJSON) ***********
	   	else if (request.getParameter( "search" ) != null) {
            String searchStr = request.getParameter( "search" );
            searchStr = URLDecoder.decode( searchStr, "UTF-8" );
            log.info( "    searchStr= " + searchStr );
            String outputType = request.getParameter( "outputType" );

            int maxResults = request.getParameter( "maxResults" ) != null
                    ? Integer.parseInt( request.getParameter( "maxResults" ) )
                    : DEFAULT_MAX_SEARCH_RESULTS;
            
            try {
                // gzipped response?
                CountingOutputStream cout = new CountingOutputStream( response.getOutputStream() );
                OutputStream bout = cout;
                String acceptEncoding = request.getHeader( "Accept-Encoding" );
                if (acceptEncoding != null && acceptEncoding.toLowerCase().contains( "gzip" )) {
                    try {
                        bout = new GZIPOutputStream( bout );
                        response.setHeader( "Content-Encoding", "gzip" );
                    }
                    catch (NoSuchMethodError e) {
                        // for whatever reason the syncFlush ctor is not always available
                        log.warn( e.toString() );
                    }
                }

                ObjectOutput out = null;
                // KML
                if ("KML".equalsIgnoreCase( outputType )) {
                    out = new KMLEncoder( bout );
                    response.setContentType( "application/vnd.google-earth.kml+xml; charset=UTF-8" );
                    response.setCharacterEncoding( "UTF-8" );
                }
                // JSON
                else if ("JSON".equalsIgnoreCase( outputType )) {
                    response.setContentType( "application/json; charset=UTF-8" );
                    response.setCharacterEncoding( "UTF-8" );
                    out = new GeoJsonEncoder( bout, worldCRS );
                }
                // RSS
                else {
                    // XXX figure the real client URL (without reverse proxies)
                    //String baseURL = StringUtils.substringBeforeLast( request.getRequestURL().toString(), "/" ) + "/index.html";
                    String baseURL = (String)System.getProperties().get( "org.polymap.atlas.feed.url" );
                    log.info( "    baseURL: " + baseURL );                    
                    String title = (String)System.getProperties().get( "org.polymap.atlas.feed.title" );
                    String description = (String)System.getProperties().get( "org.polymap.atlas.feed.description" );

                    out = new GeoRssEncoder( bout, worldCRS, baseURL, title, description );
                    response.setContentType( "application/rss+xml; charset=UTF-8" );
                    response.setCharacterEncoding( "UTF-8" );
                }

                // make sure that empty searchStr *always* results in empty reponse 
                if (searchStr != null && searchStr.length() > 0) {
                    for (JSONObject feature : dispatcher.search( searchStr, maxResults )) {
                        try {
                            out.writeObject( feature );
                        }
                        catch (Exception e) {
                            log.warn( "Error during encode: " + e, e );
                        }
                    }
                }

                // make sure that streams and deflaters are flushed
                out.close();
                log.info( "    written: " + cout.getCount() + " bytes" );
            }
            catch (Exception e) {
                log.error( e.getLocalizedMessage(), e );
            }
	   	}
	   	response.flushBuffer();
 	}

}


