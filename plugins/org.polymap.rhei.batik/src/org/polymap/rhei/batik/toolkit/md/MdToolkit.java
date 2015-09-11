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
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.UIUtils;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.engine.PageStack;
import org.polymap.rhei.batik.toolkit.DefaultToolkit;

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

    public FileUpload createFileUploadFab( String label, int position, int... styles ) {
        FileUpload result = new FileUpload( panelPage.control, stylebits( styles ) );
        result.setText( label );
        result.moveAbove( null );
        UIUtils.setVariant( result, CSS_FAB );
        posControl( result, position );
        return result;
    }


    /**
     * Creates a Floating Action Button.
     * 
     * @see <a
     *      href="http://www.google.com/design/spec/components/buttons-floating-action-button.html">Material
     *      Design</a>.
     */
    public Button createFab() {
        return createFab("+", SWT.UP | SWT.RIGHT);
    }

    /**
     * Creates a Floating Action Button with the given label.
     * 
     * @see <a
     *      href="http://www.google.com/design/spec/components/buttons-floating-action-button.html">Material
     *      Design</a>.
     */
    public Button createFab(String label, int position) {
        Button result = createButton( panelPage.control, "+", SWT.PUSH );
        result.moveAbove( null );
        UIUtils.setVariant( result, CSS_FAB );
        posControl( result, position );
        return result;
    }

    /**
     * Creates a Floating Action Button with the given image.
     * 
     * @see <a
     *      href="http://www.google.com/design/spec/components/buttons-floating-action-button.html">Material
     *      Design</a>.
     */
    public Button createFab(Image image, int position) {
        Button result = createButton( panelPage.control, image, SWT.PUSH );
        result.moveAbove( null );
        UIUtils.setVariant( result, CSS_FAB );
        posControl( result, position );
        return result;
    }


    private void posControl( Control result, int position ) {
        if(position == (SWT.UP | SWT.RIGHT)) {
            result.setLayoutData( FormDataFactory.defaults()
                    .top( 0, dp( 72 ) ).right( 100, -dp( 40 ) )
                    .width( dp( 72 ) ).height( dp( 72 ) ).create() );
        } else if(position == (SWT.DOWN | SWT.RIGHT)) {
            result.setLayoutData( FormDataFactory.defaults()
                    .bottom( 100, -dp( 72 ) ).right( 100, -dp( 40 ) )
                    .width( dp( 72 ) ).height( dp( 72 ) ).create() );
        } else if(position == (SWT.UP | SWT.LEFT)) {
            result.setLayoutData( FormDataFactory.defaults()
                    .top( 0, dp( 72 ) ).left( 100, dp( 40 ) )
                    .width( dp( 72 ) ).height( dp( 72 ) ).create() );
        } else if(position == (SWT.DOWN | SWT.LEFT)) {
            result.setLayoutData( FormDataFactory.defaults()
                    .bottom( 100, dp( -72 ) ).left( 100, dp( 40 ) )
                    .width( dp( 72 ) ).height( dp( 72 ) ).create() );
        }
    }
    
    /**
     * Creates a floating snack bar.
     * 
     * @see <a
     *      href="http://www.google.com/design/spec/components/snackbars-toasts.html">Material
     *      Design</a>.
     */
    public Snackbar createFloatingSnackbar(int styleBits) {
        return new Snackbar(this, panelPage.control, styleBits);
    }    


    public Composite createCard( Composite parent ) {
        throw new RuntimeException( "not yet..." );
    }


    public MdListViewer createListViewer( Composite parent, int... styles ) {
        return new MdListViewer( parent, stylebits( styles ) );
    }
}
