/* 
 * polymap.org
 * Copyright 2010-2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.engine.form;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;

import org.eclipse.rap.rwt.graphics.Graphics;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.forms.widgets.Section;
import org.polymap.core.ui.UIUtils;
import org.polymap.rap.updownload.upload.Upload;
import org.polymap.rhei.form.IFormToolkit;

import com.google.common.collect.Sets;

/**
 * Default implementation of the {@link IFormToolkit} interface. Basically this
 * implementation delegates method calls to {@link FormToolkit}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FormEditorToolkit
        implements IFormToolkit {

    /**
     * 
     */
    private static final String TABITEM_SELECTED_STYLE = "tabItem_selected";

    /**
     * 
     */
    private static final String TABITEM_DEFAULT_STYLE  = "tabItem_default";

    public static final String  CUSTOM_VARIANT_VALUE   = "formeditor";

    // public static final Color textBackground = Graphics.getColor( 0xFF, 0xFE, 0xE1
    // );
    // public static final Color textBackgroundDisabled = Graphics.getColor( 0xF9,
    // 0xF7, 0xF7 );
    // public static final Color textBackgroundFocused = Graphics.getColor( 0xff,
    // 0xf0, 0xd2 );
    public static final Color   backgroundFocused      = Graphics.getColor( 0xF0, 0xF0, 0xFF );

    public static final Color   labelForeground        = Graphics.getColor( 0x70, 0x70, 0x70 );

    public static final Color   labelForegroundFocused = Graphics.getColor( 0x00, 0x00, 0x00 );

    private FormToolkit         delegate;


    public FormEditorToolkit( FormToolkit delegate ) {
        super();
        this.delegate = delegate;
        this.delegate.setBorderStyle( SWT.BORDER );
    }


    protected int stylebits( int... styles ) {
        int result = SWT.NONE;
        for (int style : styles) {
            result |= style;
        }
        return result;
    }


    public <T extends Control> T adapt( T control ) {
        UIUtils.setVariant( control, CUSTOM_VARIANT_VALUE );
        // reset colors to allow styling
        control.setForeground( null );
        control.setBackground( null );
        return control;
    }


    public Button createButton( Composite parent, String text, int... styles ) {
        return adapt( delegate.createButton( parent, text, stylebits( styles ) ) );
    }


    public Composite createComposite( Composite parent, int... styles ) {
        return adapt( delegate.createComposite( parent, stylebits( styles ) | SWT.NO_FOCUS ) );
    }


    public Composite createCompositeSeparator( Composite parent ) {
        return delegate.createCompositeSeparator( parent );
    }


    public ExpandableComposite createExpandableComposite( Composite parent, int expansionStyle ) {
        return delegate.createExpandableComposite( parent, expansionStyle | SWT.NO_FOCUS );
    }


    public Form createForm( Composite parent ) {
        return delegate.createForm( parent );
    }


    public FormText createFormText( Composite parent, boolean trackFocus ) {
        return delegate.createFormText( parent, trackFocus );
    }


    public Hyperlink createHyperlink( Composite parent, String text, int... styles ) {
        return delegate.createHyperlink( parent, text, stylebits( styles ) | SWT.NO_FOCUS );
    }


    public ImageHyperlink createImageHyperlink( Composite parent, int... styles ) {
        return delegate.createImageHyperlink( parent, stylebits( styles ) | SWT.NO_FOCUS );
    }


    public Label createLabel( Composite parent, String text, int... styles ) {
        Label result = adapt( delegate.createLabel( parent, text, stylebits( styles ) /*
                                                                                       * |
                                                                                       * SWT
                                                                                       * .
                                                                                       * NO_FOCUS
                                                                                       */) );
        // result.setForeground( labelForeground );
        return result;
    }


    // public Label createLabel( Composite parent, String text ) {
    // Label result = adapt( delegate.createLabel( parent, text/*, SWT.NO_FOCUS*/ )
    // );
    // // result.setForeground( labelForeground );
    // // result.setFont( Graphics.getFont( result.getFont().getFontData()[0].
    // return result;
    // }

    public ScrolledPageBook createPageBook( Composite parent, int... styles ) {
        return delegate.createPageBook( parent, stylebits( styles ) );
    }


    public ScrolledForm createScrolledForm( Composite parent ) {
        return delegate.createScrolledForm( parent );
    }


    public Section createSection( Composite parent, int sectionStyle ) {
        return delegate.createSection( parent, sectionStyle | SWT.NO_FOCUS );
    }


    public Label createSeparator( Composite parent, int... styles ) {
        return delegate.createSeparator( parent, stylebits( styles ) | SWT.NO_FOCUS );
    }


    public Table createTable( Composite parent, int... styles ) {
        return delegate.createTable( parent, stylebits( styles ) );
    }


    public List createList( Composite parent, int... styles ) {
        List result = adapt( new List( parent, stylebits( styles ) ) );
        // result.setBackground( textBackground );
        return result;
    }


    public Text createText( Composite parent, String value, int... styles ) {
        Text result = adapt( delegate.createText( parent, value, stylebits( styles ) ) );
        return result;
    }


    public Tree createTree( Composite parent, int... styles ) {
        return delegate.createTree( parent, stylebits( styles ) );
    }


    public Upload createUpload( Composite parent, int style, int flags ) {
        Upload upload = new Upload( parent, style, flags );
        delegate.adapt( upload, false, false );
        adapt( upload );
        // upload.setBackground( textBackground );
        return upload;
    }


    public Combo createCombo( Composite parent, Set<String> values ) {
        return createCombo( parent, values, SWT.DROP_DOWN );
    }


    public Combo createCombo( Composite parent, Set<String> values, int... styles ) {
        Combo combo = new Combo( parent, stylebits( styles ) );
        delegate.adapt( combo );
        adapt( combo );
        // combo.setBackground( textBackground );
        combo.setVisibleItemCount( 12 );
        SortedSet<String> set = Sets.newTreeSet( (String s1, String s2) -> s1.compareTo(s2));
        set.addAll( values );
        for (String value : set) {
            combo.add( value );
        }
        if(combo.getItems().length > 0) {
            combo.select( 0 );
        }
        return combo;
    }


    public DateTime createDateTime( Composite parent, Date value ) {
        return createDateTime( parent, value, SWT.DROP_DOWN );
    }


    public DateTime createDateTime( Composite parent, Date value, int style ) {
        DateTime result = new DateTime( parent, style | SWT.BORDER );
        delegate.adapt( result );
        adapt( result );
        // result.setBackground( textBackground );

        if (value != null) {
            Calendar cal = Calendar.getInstance();
            result.setDate( cal.get( Calendar.YEAR ), cal.get( Calendar.MONTH ), cal.get( Calendar.DATE ) );
            result.setTime( cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ) );
        }
        return result;
    }


    public Spinner createSpinner( Composite parent, int min, int max, int defaultValue ) {
        Spinner spinner = new Spinner( parent, SWT.NONE );
        spinner.setMinimum( min );
        spinner.setMaximum( max );
        spinner.setSelection( defaultValue );
        return spinner;
    }


    public Spinner createSpinner( Composite parent, int min, int max, double d ) {
        String doubleStr = String.valueOf( d );
        String[] parts = doubleStr.split( "\\." );
        int digitCount = parts.length > 1 ? parts[1].length() : 0;
        int times = Double.valueOf( Math.pow( 10, digitCount ) ).intValue();
        int defaultValue = Double.valueOf( d * times ).intValue();
        Spinner spinner = createSpinner( parent, min, max * times, defaultValue );
        spinner.setDigits( digitCount );
        return spinner;
    }


    public Composite createTabFolder( Composite parent, java.util.List<String> tabItems,
            Map<String,Function<Composite,Composite>> tabContents ) {
        Composite tabFolder = new Composite( parent, SWT.NONE );
        GridLayout tabFolderLayout = new GridLayout( 1, false );
        tabFolderLayout.verticalSpacing = 0;
        tabFolder.setLayout( tabFolderLayout );
        Composite tabBar = new Composite( tabFolder, SWT.NONE );
        GridLayout tabBarGridLayout = new org.eclipse.swt.layout.GridLayout( tabItems.size(), true );
        tabBarGridLayout.verticalSpacing = 0;
        tabBarGridLayout.horizontalSpacing = 0;
        tabBarGridLayout.marginHeight = 0;
        tabBarGridLayout.marginWidth = 0;
        tabBar.setLayout( tabBarGridLayout );
        GridData data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = SWT.FILL;
        tabBar.setLayoutData( data );
        Map<String,Button> tabButtons = new HashMap<String,Button>();
        for (String label : tabItems) {
            Button button = new Button( tabBar, SWT.PUSH );
            button.setText( label );
            button.setLayoutData( data );
            UIUtils.setVariant( button, TABITEM_DEFAULT_STYLE );
            tabButtons.put( label, button );
        }

        final Composite tabContent = new Composite( tabFolder, SWT.NONE );
        tabContent.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        final StackLayout layout = new StackLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        tabContent.setLayout( layout );
        Map<String,Composite> map = new HashMap<String,Composite>();
        tabContents.entrySet().stream()
                .forEach( entry -> map.put( entry.getKey(), entry.getValue().apply( tabContent ) ) );
        if (tabItems.size() > 0) {
            layout.topControl = map.get( tabItems.get( 0 ) );
            UIUtils.setVariant( tabButtons.get( tabItems.get( 0 ) ), TABITEM_SELECTED_STYLE );
        }
        tabContent.layout();
        for (String label : tabItems) {
            tabButtons.get( label ).addSelectionListener( new SelectionAdapter() {

                public void widgetSelected( SelectionEvent e ) {
                    layout.topControl = map.get( label );
                    tabContent.layout();
                    UIUtils.setVariant( (Button)e.widget, TABITEM_SELECTED_STYLE );
                    tabButtons.values().stream().filter( button -> button != e.widget )
                            .forEach( button -> UIUtils.setVariant( button, TABITEM_DEFAULT_STYLE ) );
                }
            } );
        }
        return tabFolder;
    }
}
