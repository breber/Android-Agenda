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

import org.reber.agenda.R;
import org.reber.agenda.util.CalendarUtilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Represents a separator in the ListView
 *
 * @author brianreber
 */
public class ListSeparator extends ListItem {

    /**
     * Creates a new Separator for the ListView with the given title
     *
     * @param title
     */
    public ListSeparator(String title) {
        this.title = title;
    }

    /* (non-Javadoc)
     * @see org.reber.agenda.ListItem#getType()
     */
    @Override
    public ItemType getType() {
        return ItemType.SEPARATOR;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ListItem another) {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.reber.agenda.ListItem#getLayout(android.content.Context, android.view.ViewGroup)
     */
    @Override
    public LinearLayout getLayout(Context ctx, ViewGroup parent, CalendarUtilities util) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.separator, parent, false);

        TextView label  = (TextView) v.findViewById(R.id.label);
        label.setText(getTitle());

        return v;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return title;
    }

}
