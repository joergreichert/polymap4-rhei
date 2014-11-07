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
package org.polymap.rhei.batik.layout.desktop;

import java.util.ArrayList;

import org.pegdown.FastEncoder;
import org.pegdown.LinkRenderer;
import org.pegdown.LinkRenderer.Rendering;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.AutoLinkNode;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.ExpLinkNode;
import org.pegdown.ast.RefLinkNode;
import org.pegdown.ast.WikiLinkNode;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.batik.layout.desktop.DesktopAppManager.DesktopAppContext;
import org.polymap.rhei.batik.toolkit.IBusyIndicator;
import org.polymap.rhei.batik.toolkit.ILayoutContainer;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DesktopToolkit
        implements IPanelToolkit {

    private static Log log = LogFactory.getLog( DesktopToolkit.class );

    public static final String  CSS_PREFIX = "batik-panel";
    public static final String  CSS_FORM = CSS_PREFIX + "-form";
    public static final String  CSS_FORM_DISABLED = CSS_PREFIX + "-form-disabled";
    public static final String  CSS_FORMFIELD = CSS_PREFIX + "-formfield";
    public static final String  CSS_FORMFIELD_DISABLED = CSS_PREFIX + "-formfield-disabled";
    public static final String  CSS_SECTION = CSS_PREFIX + "-section";
    public static final String  CSS_SECTION_TITLE = CSS_PREFIX + "-section-title";
    public static final String  CSS_SECTION_SEPARATOR = CSS_PREFIX + "-section-separator";
    public static final String  CSS_SECTION_CLIENT = CSS_PREFIX + "-section-client";
    
    public static final Lazy<Color> COLOR_SECTION_TITLE_FG = new LockedLazyInit( new Supplier<Color>() {
        public Color get() { return Graphics.getColor( new RGB( 0x41, 0x83, 0xa6 ) ); }  //0x4a, 0x8a, 0xaf
    });
    public static final Lazy<Color> COLOR_SECTION_TITLE_BG = new LockedLazyInit( new Supplier<Color>() {
        public Color get() { return Graphics.getColor( new RGB( 0xbc, 0xe1, 0xf4 ) ); }
    });

    private static ArrayList<MarkdownRenderer> renderers = new ArrayList();
    
    static {
        registerMarkdownRenderer( new PageLinkRenderer() );
    }
    
    public static void registerMarkdownRenderer( MarkdownRenderer renderer ) {
        renderers.add( renderer );
    }

    
    // instance *******************************************
    
    private FormColors          colors;
    
    private DesktopAppContext   context;

    
    protected DesktopToolkit( DesktopAppContext context ) {
        this.context = context;
    }
    
    @Override
    public Label createLabel( Composite parent, String text, int... styles ) {
        Label result = adapt( new Label( parent, stylebits( styles ) ), false, false );
        if (text != null) {
            result.setText( text );
        }
        return result;
    }

    @Override
    public Label createFlowText( Composite parent, String text, int... styles ) {
        Label result = adapt( new Label( parent, stylebits( styles ) | SWT.WRAP ), false, false );
        if (text != null) {
            // process markdown
            LinkRenderer linkRenderer = new DelegatingLinkRenderer();
            String processed = new PegDownProcessor().markdownToHtml( text, linkRenderer );
            result.setText( processed );
        }
        result.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
        return result;
    }

    @Override
    public Label createFlowText( Composite parent, String text, LinkAction[] linkActions, int... styles ) {
        Label result = adapt( new Label( parent, stylebits( styles ) | SWT.WRAP ), false, false );
        if (text != null) {
            // process markdown
            LinkRenderer linkRenderer = new DelegatingLinkRenderer();
            String processed = new PegDownProcessor().markdownToHtml( text, linkRenderer );
            result.setText( processed );
        }
        result.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
        return result;
    }

    /**
     * 
     */
    protected static class PegDownRenderOutput
            implements RenderOutput {

        private String      url, text, title;
        
        @Override
        public void setUrl( String linkUrl ) {
            this.url = linkUrl;
        }

        @Override
        public void setTitle( String title ) {
            this.title = title;
        }

        @Override
        public void setText( String text ) {
            this.text = text;
        }
        
        public Rendering createRendering() {
            Rendering rendering = new Rendering( url, text );
            if (!StringUtils.isEmpty( title )) {
                rendering.withAttribute( "title", FastEncoder.encode( title ) );
            }
            return rendering;
        }
    }
    
    
    /**
     * Delegates link and image rendering to the
     * {@link DesktopToolkit#registerMarkdownRenderer(IPanelToolkit.MarkdownRenderer)
     * registered} {@link MarkdownRenderer}s.
     */
    protected class DelegatingLinkRenderer
            extends LinkRenderer {
        
        protected Rendering render( MarkdownNode node ) {
            for (MarkdownRenderer renderer : renderers) {
                PegDownRenderOutput out = new PegDownRenderOutput();
                if (renderer.render( node, out, context )) {
                    return out.createRendering();
                }
            }
            return null;
        }
        
        @Override
        public Rendering render( final ExpImageNode imageNode, final String text ) {
            Rendering result = render( new MarkdownNode() {
                @Override
                public MarkdownNodeType type() {
                    return MarkdownNodeType.ExpImage;
                }
                @Override
                public String url() {
                    return imageNode.url;
                }
                @Override
                public String title() {
                    return imageNode.title;
                }
                @Override
                public String text() {
                    return text;
                }
            });
            return result != null ? result : super.render( imageNode, text );
        }

        @Override
        public Rendering render( final ExpLinkNode linkNode, final String linktext ) {
            Rendering result = render( new MarkdownNode() {
                @Override
                public MarkdownNodeType type() {
                    return MarkdownNodeType.ExpLink;
                }
                @Override
                public String url() {
                    return linkNode.url;
                }
                @Override
                public String title() {
                    return linkNode.title;
                }
                @Override
                public String text() {
                    return linktext;
                }
            });
            return result != null ? result : super.render( linkNode, linktext );
        }

        @Override
        public Rendering render( AutoLinkNode node ) {
            return super.render( node );
        }

        @Override
        public Rendering render( RefLinkNode node, String url, String title, String text ) {
            return super.render( node, url, title, text );
        }

        @Override
        public Rendering render( WikiLinkNode node ) {
            return super.render( node );
        }
    };

    @Override
    public Link createLink( Composite parent, String text, int... styles ) {
        Link result = adapt( new Link( parent, stylebits( styles ) ), false, false );
        if (text != null) {
            result.setText( text.contains( "<a>" ) ? text : Joiner.on( "" ).join( "<a>", text, "</a>" ) );
        }
//        Label result = createLabel( parent, text, styles | SWT.L );
//        result.setCursor( new Cursor( Polymap.getSessionDisplay(), SWT.CURSOR_HAND ) );
//        result.setForeground( Graphics.getColor( 0x00, 0x00, 0xff ) );
        return result;
    }

    @Override
    public Button createButton( Composite parent, String text, int... styles ) {
        Button control = adapt( new Button( parent, stylebits( styles ) ), true, true );
        if (text != null) {
            control.setText( StringUtils.upperCase( text, Polymap.getSessionLocale() ) );
        }
        return control;
    }
    
    @Override
    public Text createText( Composite parent, String defaultText, int... styles ) {
        Text control = adapt( new Text( parent, stylebits( styles ) ), true, true );
        if (defaultText != null) {
            control.setText( defaultText );
        }
        return control;
    }

    @Override
    public Composite createComposite( Composite parent, int... styles ) {
        boolean scrollable = ArrayUtils.contains( styles, SWT.V_SCROLL )
                || ArrayUtils.contains( styles, SWT.H_SCROLL );
        
        Composite result = null;
        if (scrollable) { 
            result = new ScrolledComposite( parent, stylebits( styles ) );
            ((ScrolledComposite)result).setExpandHorizontal( true );
            ((ScrolledComposite)result).setExpandVertical( true );
            
            Composite content = createComposite( result );
            ((ScrolledComposite)result).setContent( content );
            
            result.setLayout( new FillLayout() );
        }
        else {
            result = new Composite( parent, stylebits( styles ) );
            result.setLayout( new FillLayout( SWT.HORIZONTAL ) );
        }
        return adapt( result );
    }

    
    @Override
    public Section createSection( Composite parent, String title, int... styles ) {
        Section result = adapt( new Section( parent, stylebits( styles ) | SWT.NO_FOCUS ) );
        result.setBackgroundMode( SWT.INHERIT_NONE );
        result.setText( title );
        result.setExpanded( true );

        result.setMenu( parent.getMenu() );
//        if (result.toggle != null) {
//            section.toggle.setHoverDecorationColor(colors
//                    .getColor(IFormColors.TB_TOGGLE_HOVER));
//            section.toggle.setDecorationColor(colors
//                    .getColor(IFormColors.TB_TOGGLE));
//        }

//        result.setFont( boldFontHolder.getBoldFont(parent.getFont()));

//        if ((sectionStyle & Section.TITLE_BAR) != 0
//                || (sectionStyle & Section.SHORT_TITLE_BAR) != 0) {
//            colors.initializeSectionToolBarColors();
//            result.setTitleBarBackground( colors.getColor( IFormColors.TB_BG ) );
//            result.setTitleBarBorderColor( colors.getColor( IFormColors.TB_BORDER ) );
//        }
        // call setTitleBarForeground regardless as it also sets the label color
//        result.setTitleBarForeground( colors.getColor( IFormColors.TB_TOGGLE ) );

//        FontData[] defaultFont = parent.getFont().getFontData();
//        FontData bold = new FontData(defaultFont[0].getName(), defaultFont[0].getHeight(), SWT.BOLD);
//        result.setFont( Graphics.getFont( bold ) );
        result.setTitleBarForeground( COLOR_SECTION_TITLE_FG.get() );
        result.setTitleBarBackground( COLOR_SECTION_TITLE_BG.get() );
        result.setTitleBarBorderColor( Graphics.getColor( new RGB( 0x80, 0x80, 0xa0 ) ) );

        Composite client = createComposite( result );
        result.setClient( client );

        FillLayout layout = new FillLayout( SWT.VERTICAL );
        layout.spacing = 1;
        layout.marginWidth = 2;
        layout.marginHeight = 2;
        client.setLayout( layout );
        return result;
    }

    
    @Override
    public IPanelSection createPanelSection( Composite parent, String title, int... styles ) {
        DesktopPanelSection result = new DesktopPanelSection( this, parent, styles );
        adapt( result.getControl() );
        if (title != null) {
            result.setTitle( title );
        }
        return result;
    }

    
    @Override
    public IPanelSection createPanelSection( ILayoutContainer parent, String title, int... styles ) {
        return createPanelSection( parent.getBody(), title, styles );
    }

    
    @Override
    public List createList( Composite parent, int... styles ) {
        List result = adapt( new List( parent, stylebits( styles ) ), false, false );
        return result;
    }

    
    @Override
    public IBusyIndicator busyIndicator( Composite parent ) {
        return new BusyIndicator( parent );
    }

    
    protected <T extends Composite> T adapt( T composite ) {
        composite.setData( WidgetUtil.CUSTOM_VARIANT, CSS_PREFIX );

//        composite.setBackground( colors.getBackground() );
//        composite.addMouseListener( new MouseAdapter() {
//            public void mouseDown( MouseEvent e ) {
//                ((Control)e.widget).setFocus();
//            }
//        } );
//        if (composite.getParent() != null) {
//            composite.setMenu( composite.getParent().getMenu() );
//        }
        return composite;
    }


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
    public <T extends Control> T adapt( T control, boolean trackFocus, boolean trackKeyboard) {
        control.setData( WidgetUtil.CUSTOM_VARIANT, CSS_PREFIX );

//        control.setBackground( colors.getBackground() );
//        control.setForeground( colors.getForeground() );

//        if (control instanceof ExpandableComposite) {
//            ExpandableComposite ec = (ExpandableComposite)control;
//            if (ec.toggle != null) {
//                if (trackFocus)
//                    ec.toggle.addFocusListener( visibilityHandler );
//                if (trackKeyboard)
//                    ec.toggle.addKeyListener( keyboardHandler );
//            }
//            if (ec.textLabel != null) {
//                if (trackFocus)
//                    ec.textLabel.addFocusListener( visibilityHandler );
//                if (trackKeyboard)
//                    ec.textLabel.addKeyListener( keyboardHandler );
//            }
//            return;
//        }

//        if (trackFocus) {
//            control.addFocusListener( visibilityHandler );
//        }
//        if (trackKeyboard) {
//            control.addKeyListener( keyboardHandler );
//        }
        return control;
    }


    protected int stylebits( int... styles ) {
        int result = SWT.NONE;
        for (int style : styles) {
            result |= style;
        }
        return result;
    }

}
