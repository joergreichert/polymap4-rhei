/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.rhei.form;

import java.util.List;
import java.util.SortedMap;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Widget;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ImageDialog
        extends Dialog {

    private static final int                                           IMAGE_BOXES_IN_ROW     = 14;

    private static final int                                           IMAGE_DISPLAY_BOX_SIZE = 76;

    private static final int                                           PALETTE_BOX_SIZE       = 12;

    private static final int                                           BUTTON_WIDTH           = 60;

    private Image                                                      image;

    private Label                                                      imageDisplay;

    private Button                                                     noIcon;

    private final SortedMap<Pair<String,String>,List<Supplier<ImageDescriptor>>> imageLibrary;


    public ImageDialog( Shell parent, SortedMap<Pair<String,String>,List<Supplier<ImageDescriptor>>> imageLibrary ) {
        this( parent, SWT.APPLICATION_MODAL, imageLibrary );
    }


    /**
     * Constructs a new instance of this class given its parent and a style value
     * describing its behavior and appearance.
     * <p>
     * The style value is either one of the style constants defined in class
     * <code>SWT</code> which is applicable to instances of this class, or must be
     * built by <em>bitwise OR</em>'ing together (that is, using the <code>int</code>
     * "|" operator) two or more of those <code>SWT</code> style constants. The class
     * description lists the style constants that are applicable to the class. Style
     * bits are also inherited from superclasses.
     * </p>
     *
     * @param parent a composite control which will be the parent of the new instance
     *        (cannot be null)
     * @param style the style of control to construct
     * @exception IllegalArgumentException <ul>
     *            <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     *            </ul>
     * @exception SWTException <ul>
     *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
     *            that created the parent</li>
     *            <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
     *            subclass</li>
     *            </ul>
     * @see SWT
     * @see Widget#checkSubclass
     * @see Widget#getStyle
     */
    public ImageDialog( Shell parent, int style, SortedMap<Pair<String,String>,List<Supplier<ImageDescriptor>>> imageLibrary ) {
        super( parent, checkStyle( parent, style ) );
        checkSubclass();
        setText( "Select icon" );
        this.imageLibrary = imageLibrary;
    }


    private static int checkStyle( Shell parent, int style ) {
        int result = style;
        int mask = SWT.PRIMARY_MODAL | SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL;
        if ((result & SWT.SHEET) != 0) {
            result &= ~SWT.SHEET;
            if ((result & mask) == 0) {
                result |= parent == null ? SWT.APPLICATION_MODAL : SWT.PRIMARY_MODAL;
            }
        }
        if ((result & mask) == 0) {
            result |= SWT.APPLICATION_MODAL;
        }
        if ((result & (SWT.LEFT_TO_RIGHT)) == 0) {
            if (parent != null) {
                if ((parent.getStyle() & SWT.LEFT_TO_RIGHT) != 0) {
                    result |= SWT.LEFT_TO_RIGHT;
                }
            }
        }
        return result;
    }


    /**
     * Makes the receiver visible and brings it to the front of the display.
     *
     * <!-- Begin RAP specific -->
     * <p>
     * This method is not supported when running the application in JEE_COMPATIBILITY
     * mode. Use DialogUtil#open instead.
     * </p>
     * <!-- End RAP specific -->
     *
     * @return the selected color, or null if the dialog was cancelled, no color was
     *         selected, or an error occurred
     * @exception SWTException <ul>
     *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
     *            that created the receiver</li>
     *            </ul>
     * @exception UnsupportedOperationException when running the application in
     *            JEE_COMPATIBILITY mode
     *
     * @see org.eclipse.rap.rwt.application.Application.OperationMode
     */
    public Image open() {
        checkOperationMode();
        prepareOpen();
        runEventLoop( shell );
        return image;
    }


    /**
     * Returns the currently selected image in the receiver.
     *
     * @return the image, may be null
     */
    public Image getImage() {
        return image;
    }


    /**
     * Sets the receiver's selected image to be the argument.
     *
     * @param image the new image value, may be null to let the platform select a
     *        default when open() is called
     */
    public void setImage( Image image ) {
        this.image = image;
    }


    @Override
    protected void prepareOpen() {
        createShell();
        createControls();
        updateImageDisplay();
        configureShell();
    }


    private void createShell() {
        shell = new Shell( getParent(), SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL );
        shell.addShellListener( new ShellAdapter() {

            @Override
            public void shellClosed( ShellEvent event ) {
                if (returnCode == SWT.CANCEL) {
                    ImageDialog.this.image = null;
                }
            }
        } );
        shell.setLayout( new GridLayout( 1, false ) );
    }


    private void createControls() {
        createImageArea();
        createImageSelection();
        createNoIconButton();
        createButtons();
    }


    private void createNoIconButton() {
        noIcon = new Button( shell, SWT.CHECK );
        noIcon.setText( "Use no icon" );
        noIcon.setSelection( image == null );
        noIcon.addSelectionListener( new SelectionAdapter() {

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse
             * .swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected( SelectionEvent e ) {
                if (noIcon.getSelection()) {
                    image = null;
                    updateImageDisplay();
                    noIcon.setEnabled( false );
                }
            }
        } );
    }


    private void createImageSelection() {
        Composite imageSelectionComp = new Composite( shell, SWT.NONE );
        imageSelectionComp.setLayout( new GridLayout( 1, false ) );
        TabFolder tabFolder = new TabFolder( imageSelectionComp, SWT.NONE );
        for (Pair<String,String> nameAndLicenceText : imageLibrary.keySet()) {
            TabItem tabItem = new TabItem( tabFolder, SWT.NONE );
            tabItem.setText( nameAndLicenceText.getLeft() );
            Composite comp = new Composite( tabFolder, SWT.NONE );
            comp.setLayout( new GridLayout( 1, false ) );
            createImageSelection( comp, imageLibrary.get( nameAndLicenceText ) );
            Label licenceLabel = new Label( comp, SWT.NONE );
            licenceLabel.setText( nameAndLicenceText.getRight() );
            licenceLabel.setData( RWT.MARKUP_ENABLED, true );
            tabItem.setControl( comp );
        }
    }


    private Composite createImageSelection( Composite parent, List<Supplier<ImageDescriptor>> list ) {
        Composite imageSelectionComp = new Composite( parent, SWT.NONE );
        GridData palData = new GridData( SWT.CENTER, SWT.CENTER, true, false );
        imageSelectionComp.setLayoutData( palData );
        imageSelectionComp.setLayout( new GridLayout( IMAGE_BOXES_IN_ROW, true ) );
        Label title = new Label( imageSelectionComp, SWT.NONE );
        String titleText = "Select symbol";
        title.setText( titleText );
        GridData titleData = new GridData( SWT.CENTER, SWT.CENTER, true, false );
        titleData.horizontalSpan = IMAGE_BOXES_IN_ROW;
        title.setLayoutData( titleData );
        for (Supplier<ImageDescriptor> imageDesc : list) {
            Image image = imageDesc.get().createImage();
            if (image != null) {
                createPaletteImageBox( imageSelectionComp, image );
            }
        }
        return imageSelectionComp;
    }


    public Image getScaledImage( Image image, int width, int height ) {
        ImageData imageData = image.getImageData();
        Rectangle bounds = image.getBounds();
        int newHeight = -1;
        int newWidth = -1;
        if (bounds.width > bounds.height) {
            newHeight = Double.valueOf( width * bounds.height / bounds.width ).intValue();
            newWidth = width;
        }
        else {
            newWidth = Double.valueOf( height * bounds.width / bounds.height ).intValue();
            newHeight = height;
        }
        ImageData scaledImageData = imageData.scaledTo( newWidth, newHeight );
        return new Image( Display.getDefault(), scaledImageData );
    }


    private Label createPaletteImageBox( Composite parent, Image image ) {
        Label result = new Label( parent, SWT.BORDER | SWT.FLAT );
        GridData data = new GridData();
        data.widthHint = PALETTE_BOX_SIZE;
        data.heightHint = PALETTE_BOX_SIZE;
        data.horizontalAlignment = SWT.CENTER;
        data.verticalAlignment = SWT.CENTER;
        result.setLayoutData( data );
        result.setImage( getScaledImage( image, data.widthHint, data.heightHint ) );
        result.addMouseListener( new PaletteListener( image ) );
        return result;
    }


    private void createImageArea() {
        Composite areaComp = new Composite( shell, SWT.NONE );
        GridData compData = new GridData( SWT.CENTER, SWT.CENTER, true, false );
        areaComp.setLayoutData( compData );
        areaComp.setLayout( new GridLayout( 1, false ) );
        imageDisplay = new Label( areaComp, SWT.BORDER | SWT.FLAT );
        GridData data = new GridData();
        data.horizontalAlignment = SWT.CENTER;
        data.verticalAlignment = SWT.CENTER;
        data.widthHint = IMAGE_DISPLAY_BOX_SIZE;
        data.heightHint = IMAGE_DISPLAY_BOX_SIZE;
        imageDisplay.setLayoutData( data );
    }


    private void createButtons() {
        Composite composite = new Composite( shell, SWT.NONE );
        composite.setLayout( new GridLayout( 0, true ) );
        GridData gridData = new GridData( SWT.RIGHT, SWT.CENTER, true, false );
        composite.setLayoutData( gridData );
        Button okButton = createButton( composite, SWT.getMessage( "SWT_OK" ), SWT.OK );
        shell.setDefaultButton( okButton );
        createButton( composite, SWT.getMessage( "SWT_Cancel" ), SWT.CANCEL );
        okButton.forceFocus();
    }


    private void configureShell() {
        shell.setText( getText() );
        Rectangle parentSize = getParent().getBounds();
        Point prefSize = shell.computeSize( SWT.DEFAULT, SWT.DEFAULT );
        shell.setSize( prefSize );
        int locationX = (parentSize.width - prefSize.x) / 2 + parentSize.x;
        int locationY = (parentSize.height - prefSize.y) / 2 + parentSize.y;
        shell.setLocation( new Point( locationX, locationY ) );
        shell.pack();
    }


    private void updateImageDisplay() {
        if (image == imageDisplay.getImage()) {
            return;
        }
        if (imageDisplay.getImage() != null) {
            imageDisplay.getImage().dispose();
        }
        if (image == null) {
            imageDisplay.setImage( null );
        }
        else {
            imageDisplay.setImage( getScaledImage( image, IMAGE_DISPLAY_BOX_SIZE, IMAGE_DISPLAY_BOX_SIZE ) );
        }
    }


    private Button createButton( Composite parent, String text, final int buttonId ) {
        ((GridLayout)parent.getLayout()).numColumns++;
        Button result = new Button( parent, SWT.PUSH );
        GridData data = new GridData( GridData.HORIZONTAL_ALIGN_FILL );
        int widthHint = convertHorizontalDLUsToPixels( shell, BUTTON_WIDTH );
        Point minSize = result.computeSize( SWT.DEFAULT, SWT.DEFAULT, true );
        data.widthHint = Math.max( widthHint, minSize.x );
        result.setLayoutData( data );
        result.setText( text );
        result.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent event ) {
                ImageDialog.this.returnCode = buttonId;
                shell.close();
            }
        } );
        return result;
    }

    private static final int HORIZONTAL_DIALOG_UNIT_PER_CHAR = 4;


    private static int convertHorizontalDLUsToPixels( Control control, int dlus ) {
        Font dialogFont = control.getFont();
        float charWidth = TextSizeUtil.getAvgCharWidth( dialogFont );
        float width = charWidth * dlus + HORIZONTAL_DIALOG_UNIT_PER_CHAR / 2;
        return (int)(width / HORIZONTAL_DIALOG_UNIT_PER_CHAR);
    }


    private void setImageFromPalette( Image image ) {
        this.image = image;
        updateImageDisplay();
    }


    private class PaletteListener
            extends MouseAdapter {

        private Image image;


        public PaletteListener( Image image ) {
            this.image = image;
        }


        @Override
        public void mouseDown( MouseEvent event ) {
            setImageFromPalette( image );
            noIcon.setSelection( false );
            noIcon.setEnabled( true );
        }
    }
}
