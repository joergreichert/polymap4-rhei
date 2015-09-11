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
package org.polymap.rhei.batik.toolkit.md;

import org.eclipse.swt.widgets.Composite;

/**
 * A floating snack bar.
 * 
 * @see <a href="http://www.google.com/design/spec/components/snackbars-toasts.html">Material Design</a>.
 * @author Joerg Reichert <joerg@mapzone.io>
 */
@SuppressWarnings("javadoc")
public class Snackbar
        extends AbstractFeedbackComponent {

    private static final long serialVersionUID = -4892328447169729063L;

    public Snackbar( MdToolkit tk, Composite parent, int style ) {
        super( tk, parent, 90, 100, style );
    }
}
