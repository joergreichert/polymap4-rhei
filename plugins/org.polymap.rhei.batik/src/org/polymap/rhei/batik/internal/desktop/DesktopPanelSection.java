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
package org.polymap.rhei.batik.internal.desktop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.rhei.batik.toolkit.ConstraintData;
import org.polymap.rhei.batik.toolkit.ConstraintLayout;
import org.polymap.rhei.batik.toolkit.ILayoutElement;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.LayoutConstraint;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class DesktopPanelSection
        implements IPanelSection {

    private static Log log = LogFactory.getLog( DesktopPanelSection.class );
    
    private PanelSection            control;
    
    private int                     level;

    
    public DesktopPanelSection( DesktopToolkit tk, Composite parent, int[] styles ) {
        control = new PanelSection( parent, SWT.NO_FOCUS );
        control.setData( WidgetUtil.CUSTOM_VARIANT, DesktopToolkit.CSS_SECTION  );
        control.setData( "panelSection", this );
        control.setExpanded( true );
        control.setMenu( parent.getMenu() );
        control.setLayout( new FillLayout() );

//        FontData[] defaultFont = parent.getFont().getFontData();
//        FontData bold = new FontData(defaultFont[0].getName(), defaultFont[0].getHeight(), SWT.BOLD);
//        control.setFont( Graphics.getFont( bold ) );
//        control.setTitleBarForeground( DesktopToolkit.COLOR_SECTION_TITLE_FG );
//        control.setTitleBarBackground( DesktopToolkit.COLOR_SECTION_TITLE_BG );
//        control.setTitleBarBorderColor( Graphics.getColor( new RGB( 0x80, 0x80, 0xa0 ) ) );

        control.getTitleControl().setData( WidgetUtil.CUSTOM_VARIANT, DesktopToolkit.CSS_SECTION_TITLE  );
        //control.getSeparatorControl().setData( WidgetUtil.CUSTOM_VARIANT, DesktopToolkit.CSS_PREFIX + "-section-separator"  );
        //control.getTextClient().setData( WidgetUtil.CUSTOM_VARIANT, DesktopToolkit.CSS_PREFIX + "-section-client"  );
        //control.getDescriptionControl().setData( WidgetUtil.CUSTOM_VARIANT, DesktopToolkit.CSS_PREFIX + "-section"  );
                
        Composite client = tk.adapt( new Composite( control, SWT.NO_FOCUS /*| SWT.BORDER*/ | tk.stylebits( styles ) ) );

//        ColumnLayout clientLayout = ColumnLayoutFactory.defaults()
//                .columns( 1, 3 ).spacing( 10 ).margins( 10 ).create(); 
        ConstraintLayout clientLayout = new ConstraintLayout( 0, 0, 10 );
        client.setLayout( clientLayout );
        control.setClient( client );
        
        level = getParentPanel() != null ? getParentPanel().getLevel()+1 : 0;

//        assert level >=0 && level <= 2 : "Section levels out of range: " + level;
//        switch (level) {
//            case 0:
//                // 1000 -> 30px margin
//                clientLayout.spacing = (int)( BatikApplication.sessionDisplay().getBounds().width * 0.03 );
//                clientLayout.marginWidth = clientLayout.spacing;
//                log.debug( "display width: " + BatikApplication.sessionDisplay().getBounds().width + " -> margin: " + clientLayout.marginWidth );
//                break;
//            case 1:
//                clientLayout.spacing = 10;
//                clientLayout.marginWidth = 0;
//                break;
//        }
//        clientLayout.marginHeight = 0;
    }

    
    public void dispose() {
        control.dispose();
    }


    @Override
    public ILayoutElement addConstraint( LayoutConstraint... constraints ) {
        if (control.getLayoutData() == null) {
            control.setLayoutData( new ConstraintData() );
        }
        ((ConstraintData)control.getLayoutData()).add( constraints );
        return this;
    }


    /**
     * Do the layout of this newly added children. 
     */
    protected void controlAdded( Control c ) {
        log.info( "control added: " + c );
    }

    protected void controlRemoved( Control c ) {
        log.info( "control removed: " + c );
    }

    
    @Override
    public Composite getControl() {
        return control;
    }

    
    @Override
    public Composite getBody() {
        return (Composite)control.getClient();
    }


    @Override
    public IPanelSection getParentPanel() {
        // parent -> section -> client
        for (Composite cursor=control.getParent(); cursor!=null; cursor=cursor.getParent()) {
            Object result = cursor.getData( "panelSection" );
            if (result != null) {
                return (IPanelSection)result;
            }
        }
        return null;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public String getTitle() {
        return control.getText();
    }

    @Override
    public IPanelSection setTitle( String title ) {
        control.setText( title );
        if (control.getSeparatorControl() == null) {
            Label sep = new Label( control, SWT.SEPARATOR | SWT.HORIZONTAL );
            sep.setData( WidgetUtil.CUSTOM_VARIANT, DesktopToolkit.CSS_SECTION_SEPARATOR  );
            control.setSeparatorControl( sep );
        }
        return this;
    }

    @Override
    public boolean isExpanded() {
        return control.isExpanded();
    }

    @Override
    public IPanelSection setExpanded( boolean expanded ) {
        control.setExpanded( expanded );
        return this;
    }

    
    /**
     * 
     */
    static class PanelSection
            extends Section {
        
        public PanelSection( Composite parent, int style ) {
            super( parent, style /*| Section.EXPANDED*/ );
        }

        public Control getTitleControl() {
            return textLabel;
        }
    }
    
}
