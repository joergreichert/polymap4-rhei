/*
 * polymap.org
 * Copyright 2013, Falko BrÃ¤utigam. All rights reserved.
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

import java.util.EventListener;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;

/**
 * The factory for basic UI elements used by {@link IPanel} instances.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IPanelToolkit {

    /**
     *
     * @see Label#Label(Composite, int)
     * @param parent
     * @param text
     * @param styles
     * @return Newly created control instance.
     */
    public Label createLabel( Composite parent, String text, int... styles );

    
    /**
     * Creates a flow text element. Flow text allows HTML markup and <a
     * href="http://daringfireball.net/projects/markdown/syntax">markdown syntax</a>
     * with some <a href="https://github.com/sirthias/pegdown">extensions</a> inside
     * the text.
     * <p/>
     * Links can be used as: <pre>[Link text](@command/panelId)</pre> Currently the
     * command <code>open</code> ist supported which opens the panel for the given panelId.
     * 
     * @param parent
     * @param text
     * @param styles
     * @return The newly created control.
     */
    public Label createFlowText( Composite parent, String text, int... styles );

    /**
     * Creates a flow text element. Flow text allows HTML markup and <a
     * href="http://daringfireball.net/projects/markdown/syntax">markdown syntax</a>
     * with some <a href="https://github.com/sirthias/pegdown">extensions</a> inside
     * the text.
     * 
     * @param parent
     * @param text
     * @param styles
     * @return The newly created control.
     */
    public Label createFlowText( Composite parent, String text, LinkAction[] linkActions, int... styles );

    public static interface LinkAction
            extends EventListener {
        
        public void linkPressed() throws Exception;
        
    }

    /**
     * Allows to render links and images and later maybe other special nodes. 
     */
    public static interface MarkdownRenderer {

        /**
         * @param node The Markdown node to render.
         * @param out
         * @return True if this renderer was able to render the given node.
         */
        public boolean render( MarkdownNode node, RenderOutput out, IAppContext context );
    }

    /**
     * 
     */
    public static enum MarkdownNodeType {
        ExpLink,
        RefLink,
        ExpImage,
        RefImage
    }
    
    /**
     * 
     */
    public static interface MarkdownNode {
        
        public MarkdownNodeType type();

        public String url();

        public String title();
        
        public String text();
    }
    
    /**
     * 
     */
    public static interface RenderOutput {

        public void setUrl( String linkUrl );

        public void setTitle( String title );

        public void setText( String text );
    }
    
    
    public Link createLink( Composite parent, String text, int... styles );

    public Button createButton( Composite parent, String text, int... styles );

    
    /**
     * Creates a new {@link Composite} under the given parent.
     * <p/>
     * The newly created Composite has a {@link FillLayout}. Caller may change this
     * as needed but should keep margins and spacing.
     * 
     * @param parent
     * @param styles
     * @return Newly created Composite.
     */
    public Composite createComposite( Composite parent, int... styles );

    public Section createSection( Composite parent, String title, int... styles );

    public Text createText( Composite parent, String defaultText, int... styles );

    /**
     * Creates a top level section.
     * 
     * @param parent
     * @param title The heading of this section, or null if no heading.
     * @param styles One of the {@link IPanelSection} constants.
     * @return Newly created viewer instance.
     */
    public IPanelSection createPanelSection( Composite parent, String title, int... styles );

    /**
     * Equivalent of calling <code>createPanelSection( parent.getBody(), title, styles )</code>.
     * 
     * @see #createPanelSection(Composite, String, int...)
     */
    public IPanelSection createPanelSection( ILayoutContainer parent, String title, int... styles );

    public List createList( Composite parent, int... styles );

}
