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

import org.reber.agenda.util.CalendarUtilities;

import android.content.Context;
import android.view.ViewGroup;

/**
 * An abstraction of a ListItem in our Agenda ListView.  Can
 * specify what type of ListItem it is by overriding the getType()
 * method.
 * 
 * @author brianreber
 */
public abstract class ListItem implements Comparable<ListItem> {
	/**
	 * The title of this ListItem
	 */
	protected String title;
	
	/**
	 * Gets the title of this ListItem
	 * 
	 * @return
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Sets the title of this ListItem
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Gets the type of this ListItem
	 * 
	 * @return
	 * A value from the enum ItemType
	 */
	public abstract ItemType getType();
	
	/**
	 * Gets the layout this ListItem will use to display in the UI
	 * 
	 * @param ctx
	 * The context in which to open the layout
	 * @param parent
	 * The parent ViewGroup to the layout to be returned
	 * @return
	 * The LinearLayout to be used in the ListView
	 */
	public abstract ViewGroup getLayout(Context ctx, ViewGroup parent, CalendarUtilities util);
	
	/**
	 * A list of the different types of ListItems that can be in 
	 * the Agenda ListView
	 */
	public static enum ItemType {
		EVENT, SEPARATOR;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ListItem other = (ListItem) obj;
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		return true;
	}
}
