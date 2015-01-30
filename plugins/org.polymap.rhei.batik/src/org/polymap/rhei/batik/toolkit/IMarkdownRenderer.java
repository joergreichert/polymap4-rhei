/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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

import org.eclipse.swt.widgets.Widget;

import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.layout.desktop.DesktopToolkit;

/**
 * Allows to render links and images in Markdown and later maybe other special nodes.
 * <p/>
 * Renderers are create for every render request by the factory registered via
 * {@link DesktopToolkit#registerMarkdownRenderer(java.util.concurrent.Callable)}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IMarkdownRenderer {

    /**
     * @param node The Markdown node to render.
     * @param out
     * @param widget The widget the text is rendered for.
     * @return True if this renderer was able to render the given node.
     */
    public boolean render( IMarkdownNode node, MarkdownRenderOutput out, IAppContext context, Widget widget );
    
}