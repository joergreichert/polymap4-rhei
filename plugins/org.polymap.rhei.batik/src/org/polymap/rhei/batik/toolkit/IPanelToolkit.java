/*
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.rhei.batik.IPanel;

/**
 * The factory for basic UI elements used by {@link IPanel} instances.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IPanelToolkit
        extends AutoCloseable {

//    @Override
//    public void close();

    public <T extends Composite> T adapt( T composite );

    /**
     * Adapts a control to be used in a form that is associated with this toolkit.
     * This involves adjusting colors and optionally adding handlers to ensure focus
     * tracking and keyboard management.
     *
     * @param control a control to adapt
     * @param trackFocus if <code>true</code>, form will be scrolled horizontally
     *        and/or vertically if needed to ensure that the control is visible when
     *        it gains focus. Set it to <code>false</code> if the control is not
     *        capable of gaining focus.
     * @param trackKeyboard if <code>true</code>, the control that is capable of
     *        gaining focus will be tracked for certain keys that are important to
     *        the underlying form (for example, PageUp, PageDown, ScrollUp,
     *        ScrollDown etc.). Set it to <code>false</code> if the control is not
     *        capable of gaining focus or these particular key event are already used
     *        by the control.
     */
    public <T extends Control> T adapt( T control, boolean trackFocus, boolean trackKeyboard );

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
     * Links inside the text:
     * <ul>
     * <li><code>[Link text](@command/panelId)</code> - executes the given command. Currently the
     * command <code>open</code> ist supported which opens the panel for the given panelId</li>
     * <br/>
     * <li><code>![alt](URL)</code> - an image from the gicen URL</li>
     * <br/>
     * <li><code>[Link text](URL)</code> - a link to the given ULR</li>
     * </ul>
     * 
     * @param parent
     * @param text
     * @param styles
     * @return The newly created control.
     */
    public Label createFlowText( Composite parent, String text, int... styles );

    /**
     * See {@link #createFlowText(Composite, String, int...)}.
     *  
     * @see #createFlowText(Composite, String, int...) 
     * @param parent
     * @param text
     * @param styles
     * @return The newly created control.
     */
    public Label createFlowText( Composite parent, String text, ILinkAction[] linkActions, int... styles );

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

    public default Composite createComposite( Composite parent, Layout layout, int... styles ) {
        assert layout != null;
        Composite result = createComposite( parent, styles );
        result.setLayout( layout );
        return result;
    }

    public Section createSection( Composite parent, String title, int... styles );

    public Text createText( Composite parent, String defaultText, int... styles );

    /**
     * Creates a top level section.
     * 
     * @param parent
     * @param title The heading of this section, or null if no heading.
     * @param styles One of the {@link IPanelSection} constants, or {@link SWT#BORDER} .
     * @return Newly created viewer instance.
     */
    public IPanelSection createPanelSection( Composite parent, String title, int... styles );

    /**
     * Equivalent of calling <code>createPanelSection( parent.getBody(), title, styles )</code>.
     * 
     * @see #createPanelSection(Composite, String, int...)
     */
    public default IPanelSection createPanelSection( ILayoutContainer parent, String title, int... styles ) {
        return createPanelSection( parent.getBody(), title, styles );
    }

    public List createList( Composite parent, int... styles );

    public IBusyIndicator busyIndicator( Composite parent );

    
    /**
     * Creates a new dialog.
     * <b>Example:</b>
     * <pre>
     * SimpleDialog dialog = site().toolkit().createSimpleDialog( "Titel!" );
     * dialog.addAction( new Action( "OK") {
     *     public void run() {
     *         dialog.close( );
     *     }
     * } );
     * dialog.open();
     * </pre>
     * 
     * @param title
     * @return Newly created dialog instance.
     * @see <a href="http://www.google.com/design/spec/components/dialogs.html">
     *      Material Design</a>
     */
    public SimpleDialog createSimpleDialog( String title );
    
}
