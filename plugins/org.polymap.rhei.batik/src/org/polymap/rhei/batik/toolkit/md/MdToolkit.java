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
package org.polymap.rhei.batik.toolkit.md;

import static org.polymap.rhei.batik.toolkit.md.MdAppDesign.dp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.UIUtils;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.engine.PageStack;
import org.polymap.rhei.batik.toolkit.DefaultToolkit;
import org.polymap.rhei.batik.toolkit.md.Toolbar.ActionConfiguration;

/**
 * Material design toolkit.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MdToolkit
        extends DefaultToolkit {

    private static Log                log     = LogFactory.getLog( MdToolkit.class );

    public static final String        CSS_FAB = CSS_PREFIX + "-fab";

    private PageStack<PanelPath>.Page panelPage;


    public MdToolkit( PanelPath panelPath, PageStack<PanelPath>.Page panelPage ) {
        super( panelPath );
        this.panelPage = panelPage;
    }


    /**
     * Creates a Floating Action Button.
     * 
     * @see <a
     *      href="http://www.google.com/design/spec/components/buttons-floating-action-button.html">Material
     *      Design</a>.
     */
    @SuppressWarnings("javadoc")
    public Button createFab() {
        Button result = createButton( panelPage.control, "+", SWT.PUSH );
        result.moveAbove( null );
        UIUtils.setVariant( result, CSS_FAB );
        result.setLayoutData( FormDataFactory.defaults()
                .top( 0, dp( 72 ) ).right( 100, -dp( 40 ) )
                .width( dp( 72 ) ).height( dp( 72 ) ).create() );
        return result;
    }


    /**
     * Creates a floating snack bar.
     * 
     * @see <a
     *      href="http://www.google.com/design/spec/components/snackbars-toasts.html">Material
     *      Design</a>.
     */
    @SuppressWarnings("javadoc")
    public Snackbar createFloatingSnackbar(int styleBits) {
        return new Snackbar(this, panelPage.control, styleBits);
    }    


    public Composite createCard( Composite parent ) {
        throw new RuntimeException( "not yet..." );
    }


    public MdListViewer createListViewer( Composite parent, int... styles ) {
        return new MdListViewer( parent, stylebits( styles ) );
    }

    public Toolbar createToolbar( Composite parent, String label, boolean fixedPosition, int style, ActionConfiguration... actions ) {
        return new Toolbar( this, parent == null ? panelPage.control : parent, fixedPosition, style, actions ).title.put(label);
    }
}
