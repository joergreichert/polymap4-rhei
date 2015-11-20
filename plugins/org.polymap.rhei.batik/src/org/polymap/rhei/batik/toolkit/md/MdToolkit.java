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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.engine.PageStack;
import org.polymap.rhei.batik.toolkit.DefaultToolkit;
import org.polymap.rhei.batik.toolkit.SimpleDialog;

/**
 * Material design toolkit.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MdToolkit
        extends DefaultToolkit {

    private static Log log = LogFactory.getLog( MdToolkit.class );

    public static final String        CSS_FAB = CSS_PREFIX + "-fab";

    private PageStack<PanelPath>.Page panelPage;


    public MdToolkit( PanelPath panelPath, PageStack<PanelPath>.Page panelPage ) {
        super( panelPath );
        assert panelPage != null;
        this.panelPage = panelPage;
    }


    /**
     * The following button types are allowed:
     * <ul>
     * <li>{@link SWT#PUSH}</li>
     * <li>{@link SWT#TOGGLE}</li>
     * <li>{@link SWT#CHECK}</li>
     * <li>{@link SWT#RADIO}</li>
     * </ul>
     * {@link SWT#PUSH} and {@link SWT#TOGGLE} buttons are Raised buttons be default. {@link SWT#BORDER}
     * has no effect. A Flat button appearance can be created by giving {@link SWT#FLAT} flag.
     * 
     * @see <a href="http://www.google.com/design/spec/components/buttons.html">Material Design</a>
     */
    @Override
    public Button createButton( Composite parent, String text, int... styles ) {
        return super.createButton( parent, text, styles );
    }


    /**
     * Creates a default Floating Action Button with "check" icon and
     * position TOP|RIGHT.
     * 
     * @see #createFab(Image, int)
     * @see <a
     *      href="http://www.google.com/design/spec/components/buttons-floating-action-button.html">Material
     *      Design</a>.
     */
    @SuppressWarnings("javadoc")
    public Button createFab() {
        return createFab( BatikPlugin.images().svgImage( "check.svg", SvgImageRegistryHelper.WHITE24 ), SWT.TOP|SWT.RIGHT );
    }
    
    
    /**
     * Creates a Floating Action Button.
     * 
     * @param icon
     * @param position A combination of {@link SWT#TOP}, {@link SWT#BOTTOM},
     *        {@link SWT#LEFT} and {@link SWT#RIGHT}.
     * @see <a
     *      href="http://www.google.com/design/spec/components/buttons-floating-action-button.html">Material
     *      Design</a>.
     */
    @SuppressWarnings("javadoc")
    public Button createFab( Image icon, int position ) {
        assert (position & ~SWT.TOP & ~SWT.BOTTOM & ~SWT.LEFT & ~SWT.RIGHT) == 0 : "position param is not valid: " + position;
        
        Button result = createButton( panelPage.control, "", SWT.PUSH );
        result.setImage( icon );
        result.moveAbove( null );
        UIUtils.setVariant( result, CSS_FAB );
        
        int marginTop = dp( 72 );
        int margin = dp( 40 );
        int size = dp( 72 );

        FormDataFactory layout = FormDataFactory.on( result )
                .width( size ).height( size );
        
        if ((position & SWT.TOP) != 0) {
            layout.top( 0, marginTop );
        }
        else if ((position & SWT.BOTTOM) != 0) {
            layout.bottom( 100, margin );
        }
        else {
            layout.top( 50, -size/2 );            
        }
        
        if ((position & SWT.LEFT) != 0) {
            layout.left( 0, margin );
        }
        else if ((position & SWT.RIGHT) != 0) {
            layout.right( 100, -margin );
        }
        else {
            layout.left( 50, -size/2 );            
        }
        return result;
    }


    /**
     * Creates a snack bar.
     * 
     * @see <a
     *      href="http://www.google.com/design/spec/components/snackbars-toasts.html">Material
     *      Design</a>.
     */
    @SuppressWarnings("javadoc")
    public MdSnackbar createSnackbar( int... styleBits ) {
        return new MdSnackbar( this, panelPage.control, stylebits( styleBits ) );
    }
    
    
    /**
     * Creates a toast.
     * 
     * @see <a
     *      href="http://www.google.com/design/spec/components/snackbars-toasts.html">Material
     *      Design</a>.
     */
    @SuppressWarnings("javadoc")
    public MdToast createToast( int verticalPosition, int... styleBits ) {
        return new MdToast( this, panelPage.control, verticalPosition, stylebits( styleBits ) );
    }    


    public Composite createCard( Composite parent ) {
        throw new RuntimeException( "not yet..." );
    }


    public MdListViewer createListViewer( Composite parent, int... styles ) {
        return new MdListViewer( parent, stylebits( styles ) );
    }

    
    @Override
    public SimpleDialog createSimpleDialog( String title ) {
        SimpleDialog result = super.createSimpleDialog( title );
        result.centerOn.put( panelPage.control );
        return result;
    }


    /**
     * Creates the main toolbar of the panel.
     * 
     * @param style {@link SWT#ON_TOP} The toolbar will float when scrolling. The
     *        toolbar disappears when scrolling otherwise.
     * @see <a
     *      href="http://www.google.com/design/spec/components/toolbars.html">Material
     *      Design</a>
     */
    public MdToolbar createToolbar( String label, int... styles ) {
        return new MdToolbar( this, panelPage.control, label, stylebits( styles ) );
    }


    public MdToolbar2 createToolbar( Composite parent, int... styles ) {
        return new MdToolbar2( parent, this, stylebits( styles ) );
    }

}
