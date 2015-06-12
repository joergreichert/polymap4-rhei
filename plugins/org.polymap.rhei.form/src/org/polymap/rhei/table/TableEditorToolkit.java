/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.table;

import java.util.Date;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.forms.widgets.Section;

import org.polymap.rhei.form.IFormToolkit;

import org.polymap.rap.updownload.upload.Upload;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class TableEditorToolkit
        implements IFormToolkit {

//    public static final Color   background = FormEditorToolkit.backgroundFocused;

    @Override
    public Composite createComposite( Composite parent, int style ) {
        return new Composite( parent, style );
    }

    @Override
    public Composite createComposite( Composite parent ) {
        return new Composite( parent, SWT.NONE );
    }

    @Override
    public Text createText( Composite parent, String value, int style ) {
        Text result = new Text( parent, style | SWT.BORDER );
//        result.setBackground( background );
        if (value != null) {
            result.setText( value );
        }
        return result;
    }

    @Override
    public Text createText( Composite parent, String value ) {
        return createText( parent, value, SWT.NONE );
    }

    @Override
    public Label createLabel( Composite parent, String text, int style ) {
        Label result = new Label( parent, style );
        if (text != null) {
            result.setText( text );
        }
        return result;
    }

    @Override
    public Label createLabel( Composite parent, String text ) {
        return createLabel( parent, text, SWT.NONE );
    }


    @Override
    public Combo createCombo( Composite parent, Set<String> values, int style ) {
        Combo combo = new Combo( parent, style );
//        combo.setBackground( textBackground );
        combo.setVisibleItemCount( 12 );
        for (String value : values) {
            combo.add( value );
        }
        return combo;
    }

    @Override
    public Combo createCombo( Composite parent, Set<String> values ) {
        return createCombo( parent, values, SWT.NONE );
    }

    @Override
    public FormText createFormText( Composite parent, boolean trackFocus ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public Button createButton( Composite parent, String text, int style ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public Composite createCompositeSeparator( Composite parent ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public ExpandableComposite createExpandableComposite( Composite parent, int expansionStyle ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public Form createForm( Composite parent ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public Hyperlink createHyperlink( Composite parent, String text, int style ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public ImageHyperlink createImageHyperlink( Composite parent, int style ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public ScrolledPageBook createPageBook( Composite parent, int style ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public ScrolledForm createScrolledForm( Composite parent ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public Section createSection( Composite parent, int sectionStyle ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public Label createSeparator( Composite parent, int style ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public Table createTable( Composite parent, int style ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public Tree createTree( Composite parent, int style ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public DateTime createDateTime( Composite parent, Date value ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public DateTime createDateTime( Composite parent, Date value, int style ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public Upload createUpload( Composite parent, int style, int flags ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public List createList( Composite parent, int comboStyle ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
}
