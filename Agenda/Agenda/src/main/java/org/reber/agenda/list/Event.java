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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.reber.agenda.R;
import org.reber.agenda.util.CalendarUtilities;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A representation of an event in a user's calendar.
 *
 * @author brianreber
 */
public class Event extends ListItem {

    private GregorianCalendar start;
    private GregorianCalendar end;
    private boolean allDay;
    private String color;
    private String location;
    private final int id;

    /**
     * Creates an event with the given values.
     *
     * @param title
     * The title of the event
     * @param start
     * The start <code>Date</code> of the event
     * @param end
     * The end <code>Date</code> of the event
     * @param allDay
     * Whether this is an all-day event
     * @param color
     * The color of the calendar it belongs to
     * @param location
     * The location of the event
     */
    public Event(String title, GregorianCalendar start, GregorianCalendar end, boolean allDay, String color, String location, int id) {
        this.title = title;
        this.start = start;
        this.end = end;
        this.allDay = allDay;
        this.color = color;
        this.location = location;
        this.id = id;
    }

    /**
     * Gets the location of this event
     *
     * @return
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of this event
     *
     * @param location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the color of this event
     *
     * @return
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the color of this event
     *
     * @param color
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Gets the starting date
     *
     * @return
     */
    public GregorianCalendar getStart() {
        return start;
    }

    /**
     * Sets the starting date
     *
     * @param start
     */
    public void setStart(GregorianCalendar start) {
        this.start = start;
    }

    /**
     * Gets the ending date
     *
     * @return
     */
    public GregorianCalendar getEnd() {
        return end;
    }

    /**
     * Sets the ending date
     *
     * @param end
     */
    public void setEnd(GregorianCalendar end) {
        this.end = end;
    }

    /**
     * Gets whether this is an all day event
     *
     * @return
     */
    public boolean isAllDay() {
        return allDay;
    }

    /**
     * Sets whether this is an all day event
     *
     * @param allDay
     */
    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    /**
     * Compares the start time of this event to the given event.
     *
     * @param another
     * The event to compare to
     */
    @Override
    public int compareTo(ListItem another) {
        if (another.getType() == ItemType.SEPARATOR) {
            return 0;
        }

        return getStart().compareTo(((Event) another).getStart());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Event [title=" + title + ", start=" + start.get(Calendar.MONTH) + "/" + start.get(Calendar.DAY_OF_MONTH) +
                "/" + start.get(Calendar.YEAR) + " " + start.get(Calendar.HOUR_OF_DAY) + ":" + start.get(Calendar.MINUTE) + ", end=" +
                + end.get(Calendar.MONTH) + "/" + end.get(Calendar.DAY_OF_MONTH) + "/" + end.get(Calendar.YEAR) + " " +
                end.get(Calendar.HOUR_OF_DAY) + ":" + end.get(Calendar.MINUTE) + ", allDay=" + allDay + ", color=" + color + ", location=" + location + "]";
    }

    /* (non-Javadoc)
     * @see org.reber.agenda.ListItem#getType()
     */
    @Override
    public ItemType getType() {
        return ItemType.EVENT;
    }

    /* (non-Javadoc)
     * @see org.reber.agenda.ListItem#getLayout(android.content.Context, android.view.ViewGroup)
     */
    @Override
    public LinearLayout getLayout(Context ctx, ViewGroup parent, CalendarUtilities util) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.rows, parent, false);
        String text = getTitle();
        String date = util.getFormattedTimeString(ctx, this);
        String location = getLocation();

        TextView label  = (TextView) v.findViewById(R.id.label);
        TextView labelDate  = (TextView) v.findViewById(R.id.labelDate);
        ImageView iv = (ImageView) v.findViewById(R.id.icon);
        TextView labelLocation = (TextView) v.findViewById(R.id.labelLocation);

        if (location == null || location.equals("")) {
            label.setText(text);
            labelDate.setText(date);
            labelLocation.setHeight(0);
        } else {
            label.setText(text);
            labelDate.setText(date);
            labelLocation.setText(location);
        }

        int[] colors = new int[15 * 80];
        Arrays.fill(colors, Color.parseColor(getColor()));

        Bitmap bm = Bitmap.createBitmap(colors, 15, 80, Bitmap.Config.RGB_565);

        iv.setImageBitmap(bm);

        return v;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (allDay ? 1231 : 1237);
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result
                + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
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
        Event other = (Event) obj;
        if (allDay != other.allDay) {
            return false;
        }
        if (color == null) {
            if (other.color != null) {
                return false;
            }
        } else if (!color.equals(other.color)) {
            return false;
        }
        if (end == null) {
            if (other.end != null) {
                return false;
            }
        } else if (!end.equals(other.end)) {
            return false;
        }
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        }
        if (start == null) {
            if (other.start != null) {
                return false;
            }
        } else if (!start.equals(other.start)) {
            return false;
        }
        return super.equals(other);
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
}
