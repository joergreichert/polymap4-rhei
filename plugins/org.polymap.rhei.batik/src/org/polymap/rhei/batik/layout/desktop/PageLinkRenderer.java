/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.batik.layout.desktop;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.internal.LinkActionServiceHandler;
import org.polymap.rhei.batik.toolkit.IPanelToolkit.LinkAction;
import org.polymap.rhei.batik.toolkit.IPanelToolkit.MarkdownNode;
import org.polymap.rhei.batik.toolkit.IPanelToolkit.MarkdownNodeType;
import org.polymap.rhei.batik.toolkit.IPanelToolkit.MarkdownRenderer;
import org.polymap.rhei.batik.toolkit.IPanelToolkit.RenderOutput;

/**
 * Render @action/page style links.
 * <p/>
 * <b>Examples:</b>
 * <pre>
 *    [Nutzer/Kunden](@open/_Panel_ID_)
 * </pre>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class PageLinkRenderer
        implements MarkdownRenderer {

    private static Log log = LogFactory.getLog( PageLinkRenderer.class );

    
    @Override
    public boolean render( final MarkdownNode node, RenderOutput out, final IAppContext context ) {
        log.info( "url=" + node.url() );
        if (node.type() == MarkdownNodeType.ExpLink 
                && node.url().startsWith( "@" )) {
            String id = LinkActionServiceHandler.register( new LinkAction() {
                Display display = Polymap.getSessionDisplay();
                
                @Override
                public void linkPressed() throws Exception {
                    display.asyncExec( new Runnable() {
                        public void run() {
                            String[] urlParts = StringUtils.split( node.url(), "/" );
                            String command = "open";
                            String panelId = urlParts[0];

                            if (urlParts.length > 1) {
                                command = urlParts[0].substring( 1 );
                                panelId = urlParts[1];
                            }
                            if (urlParts.length > 2) {
                                
                            }
                            if ("open".equalsIgnoreCase( command )) {
                                log.info( command + " : " + panelId );
                                context.openPanel( PanelIdentifier.parse( panelId ) );
                            }
                            else {
                                throw new IllegalStateException( "Unknown link command: " + command );
                            }
                        }
                    });
                }
            });
            
            String linkUrl = "javascript:sendServiceHandlerRequest('" + LinkActionServiceHandler.SERVICE_HANDLER_ID + "','" + id + "');";
            out.setUrl( linkUrl );
            out.setText( node.text() );
            out.setTitle( node.title() );
            return true;
            
//            Rendering rendering = new Rendering( linkUrl, linktext );
//            return StringUtils.isEmpty( node.title() ) ? rendering : rendering.withAttribute( "title", FastEncoder.encode( node.title ) );
        }
        else {
            return false;
        }
    }
    
}
