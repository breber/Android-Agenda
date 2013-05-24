/*
 * Copyright (C) 2011 Brian Reber
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by Brian Reber.  
 * THIS SOFTWARE IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.reber.agenda.list;

import android.graphics.drawable.Drawable;

/**
 * A small helper class that allows us to store an app's
 * name, package name and icon for use by the Spinner.
 *
 * @author brianreber
 */
public class PackageInfoWrapper {
    public CharSequence appName;
    public String packageName;
    public Drawable icon;

    public PackageInfoWrapper(CharSequence appName, String packageName, Drawable icon) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
    }

    public String toString() {
        return appName.toString();
    }
}
