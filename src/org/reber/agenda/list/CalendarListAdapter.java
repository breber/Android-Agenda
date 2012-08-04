/*
 * Copyright (C) 2012 Brian Reber
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

import java.util.List;

import org.reber.agenda.util.CalendarUtilities;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

/**
 * A specialized ListAdapter that allows us to show data in a ListView,
 * using a specific layout for each row.
 * 
 * @author brianreber
 */
public class CalendarListAdapter extends ArrayAdapter<ListItem> implements ListAdapter {
	private CalendarUtilities util;

	public CalendarListAdapter(Context context, int textViewResourceId,	List<ListItem> objects, CalendarUtilities util) {
		super(context, textViewResourceId, objects);

		this.util = util;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return this.getItem(position).getLayout(getContext(), parent, util);
	}

	/* (non-Javadoc)
	 * @see android.widget.BaseAdapter#areAllItemsEnabled()
	 */
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see android.widget.BaseAdapter#isEnabled(int)
	 */
	@Override
	public boolean isEnabled(int position) {
		return this.getItem(position).getType() != ListItem.ItemType.SEPARATOR;
	}
}