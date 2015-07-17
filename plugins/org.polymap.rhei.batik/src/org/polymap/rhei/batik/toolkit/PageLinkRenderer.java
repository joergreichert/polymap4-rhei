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
package org.polymap.rhei.batik.toolkit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.internal.LinkActionServiceHandler;

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
        implements IMarkdownRenderer, DisposeListener, ILinkAction {

    private static Log log = LogFactory.getLog( PageLinkRenderer.class );
    
    private Display             display;

    private IMarkdownNode       node;
    
    private DefaultToolkit      toolkit;

    
    @Override
    @SuppressWarnings("hiding")
    public boolean render( DefaultToolkit toolkit, final IMarkdownNode node, MarkdownRenderOutput out, Widget widget ) {
        log.debug( "url=" + node.url() );
        if (node.type() == IMarkdownNode.Type.ExpLink 
                && node.url().startsWith( "@" )) {

            // prevent this from being GCed as long as the widget exists
            widget.addDisposeListener( this );

            String id = LinkActionServiceHandler.register( this );
            
            this.toolkit = toolkit;
            this.node = node;
            this.display = UIUtils.sessionDisplay();
            assert display != null;
            
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


    @Override
    public Display display() {
        return display;
    }

    
    @Override
    public void linkPressed() throws Exception {
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
            log.debug( command + " : " + panelId );
            IAppContext context = BatikApplication.instance().getContext();
            context.openPanel( toolkit.getPanelPath(), PanelIdentifier.parse( panelId ) );
        }
        else {
            throw new IllegalStateException( "Unknown link command: " + command );
        }
    }

    @Override
    public void widgetDisposed( DisposeEvent ev ) {
        //LinkActionServiceHandler.deregister( this );
    }
    
}
