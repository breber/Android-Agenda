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
package org.reber.agenda.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.util.Log;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import org.reber.agenda.AndroidCalendar;
import org.reber.agenda.R;
import org.reber.agenda.list.Event;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A class containing some simple utilities that deal with Calendar data.
 *
 * @author brianreber
 */
public class CalendarUtilities {

    private final Set<org.reber.agenda.AndroidCalendar> calendars;
    private Set<org.reber.agenda.AndroidCalendar> selectedCalendars;
    private final Context context;
    private boolean use24Hour;

    public static SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");

    /**
     * Creates a new CalendarUtilities instance with the given context
     *
     * @param context
     */
    public CalendarUtilities(Context context, boolean use24Hour) {
        this.context = context;
        this.use24Hour = use24Hour;
        calendars = new HashSet<org.reber.agenda.AndroidCalendar>();
        selectedCalendars = new HashSet<org.reber.agenda.AndroidCalendar>();
    }

    /**
     * @return the use24Hour
     */
    public boolean isUse24Hour() {
        return use24Hour;
    }


    /**
     * @param use24Hour the use24Hour to set
     */
    public void setUse24Hour(boolean use24Hour) {
        this.use24Hour = use24Hour;
    }

    /**
     * Gets a set of all available calendars on this device
     *
     * @return
     * A set of all available calendars on this device
     */
    public Set<org.reber.agenda.AndroidCalendar> getAvailableCalendars() {
        ContentResolver contentResolver = context.getContentResolver();

        Uri calendarsURI = CalendarContract.Calendars.CONTENT_URI;
        Cursor cursor = contentResolver.query(calendarsURI,
                new String[]{ CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_COLOR,
                        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME },
                null, null, null);
        // Get all the enabled calendars in the database
        while (cursor != null && cursor.moveToNext()) {
            AndroidCalendar current = new AndroidCalendar(cursor.getString(0),
                    getColorHex(cursor.getString(1)), cursor.getString(2));
            if (!calendars.contains(current)) {
                calendars.add(current);
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        return calendars;
    }

    /**
     * Retrieves the selected calendars from the SharedPreferences with the given key
     *
     * @param prefName
     * The key the calendars are filed under
     * @return
     * The Calendar instances associated with the calendars saved in preferences
     * @throws NoSuchElementException
     * If there isn't a preference of the given name
     */
    public Set<org.reber.agenda.AndroidCalendar> getSelectedCalendarFromPref(String prefName) {
        Set<AndroidCalendar> calsFromPref = new HashSet<AndroidCalendar>();
        SharedPreferences pref = null;

        if (prefName != null) {
            pref = context.getSharedPreferences(prefName, 0);
        } else {
            pref = PreferenceManager.getDefaultSharedPreferences(context);
        }

        Set<String> string = pref.getStringSet(Constants.CAL_PREFS, null);

        if (string != null) {
            for (AndroidCalendar c : getAvailableCalendars()) {
                if (string.contains(c.getId())) {
                    calsFromPref.add(c);
                }
            }
        }

        return calsFromPref;
    }

    /**
     * Save the list of selected calendars in a SharedPreference with the
     * given key
     *
     * @param prefName
     * The key to file the preference under
     */
    public void saveSelectedCalendarsPref(String prefName) {
        Set<String> selectedCalsInfo = new HashSet<String>();
        for (AndroidCalendar c : selectedCalendars) {
            selectedCalsInfo.add(c.getId());
        }

        // Save the package name in the preferences
        SharedPreferences pref = context.getSharedPreferences(prefName, 0);
        Editor edit = pref.edit();
        edit.putStringSet(Constants.CAL_PREFS, selectedCalsInfo);
        edit.commit();
    }

    /**
     * Set the selected calendars for this instance
     *
     * @param selected
     * The set of calendars the user has selected
     */
    public void setSelectedCalendars(Set<AndroidCalendar> selected) {
        selectedCalendars = selected;
    }

    /**
     * Gets the selected calendars for this instance
     *
     * @return
     * The set of calendars the user has selected
     */
    public Set<AndroidCalendar> getSelectedCalendars() {
        return selectedCalendars;
    }

    /**
     * Gets the event data (the current time through numDays past now)
     * from the user's enabled calendars.
     *
     * @param numDays
     * The number of days after the current time to get the events from
     * @return
     * A Collection of Events that are on the user's calendar
     */
    public Collection<Event> getCalendarData(int numDays, boolean showCurrentEvent) {
        // Using a PriorityQueue for the events because it sorts them as you add them
        PriorityQueue<Event> events = new PriorityQueue<Event>();

        // For each calendar, get the events after the current time
        for (AndroidCalendar cal : selectedCalendars) {
            events.addAll(getEventDataFromCalendar(cal, numDays, showCurrentEvent));
        }

        ArrayList<Event> sorted = new ArrayList<Event>(events);
        Collections.sort(sorted);

        return sorted;
    }

    /**
     * The color data the calendar stores is an int. We want it in hex so that
     * we can compare it to the data Google has posted in their API. So we convert
     * it to hex and do some math to get it into the form Google shares.
     *
     * @param color
     * The string the calendar database stores
     * @return
     * The hex string as Google lists in their API
     * http://code.google.com/apis/calendar/data/2.0/reference.html#gCalcolor
     */
    private static String getColorHex(String color) {
        if (color == null) {
            return "";
        }

        try {
            int hex = Integer.parseInt(color);
            hex &= 0xFFFFFFFF;

            return String.format("#%08x", hex);
        } catch (NumberFormatException e) {
            return "";
        }
    }

    /**
     * Actually get the events from the given calendar
     *
     * @param cal
     * The calendar to get events from
     * @param numDays
     * The number of days after the current time to get the events from
     * @return
     * A collection of events from the given calendar
     */
    private PriorityQueue<Event> getEventDataFromCalendar(AndroidCalendar cal, int numDays, boolean showCurrentEvent) {
        GregorianCalendar todayDate = new GregorianCalendar();
        todayDate.setTimeInMillis(System.currentTimeMillis());
        PriorityQueue<Event> events = new PriorityQueue<Event>();

        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        GregorianCalendar today = new GregorianCalendar();
        long now = todayDate.getTimeInMillis();
        long temp = today.getTimeInMillis() - (today.get(GregorianCalendar.HOUR_OF_DAY) * DateUtils.HOUR_IN_MILLIS);
        ContentUris.appendId(builder, now - 5 * DateUtils.MINUTE_IN_MILLIS);
        ContentUris.appendId(builder, temp + numDays * DateUtils.DAY_IN_MILLIS);

        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(builder.build(),
                    new String[] { CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN,
                            CalendarContract.Instances.END, CalendarContract.Instances.ALL_DAY,
                            CalendarContract.Instances.EVENT_LOCATION, CalendarContract.Instances.EVENT_ID },
                    CalendarContract.Events.CALENDAR_ID + "=" + cal.getId(), null,
                    CalendarContract.Instances.START_DAY + " ASC, " + CalendarContract.Instances.START_MINUTE + " ASC");
        } catch (SQLiteException e) {
            Log.d(Constants.TAG, e.getMessage());
        }


        while (cursor != null && cursor.moveToNext()) {
            GregorianCalendar begin = new GregorianCalendar();
            begin.setTimeInMillis(cursor.getLong(1));
            GregorianCalendar end = new GregorianCalendar();
            end.setTimeInMillis(cursor.getLong(2));
            boolean allDay = !"0".equals(cursor.getString(3));

            // All day events have times that are off by the Timezone offset, so we
            // need to fix that to get it to display correctly in the list and on the widget
            if (allDay) {
                fixAllDayEvent(begin, end);
            }

            if ((begin.after(todayDate) && !showCurrentEvent) || (showCurrentEvent && end.after(todayDate))) {
                events.add(new Event(cursor.getString(0), begin, end, allDay, cal.getColor(),
                        (cursor.getString(4) == null ? "" : cursor.getString(4)), cursor.getInt(5)));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return events;
    }

    /**
     * Oftentimes all-day events have messed up starting times, which can cause
     * them to be displayed before events that happen in the previous day.
     *
     * The times are usually off by the Timezone offset, so we should be able
     * to just subtract the offset from the given event time, and it should be
     * all fixed.
     *
     * @param eventStart
     * @param eventEnd
     * The date to modify
     */
    private static void fixAllDayEvent(GregorianCalendar eventStart, GregorianCalendar eventEnd) {
        long milliseconds = eventStart.getTimeInMillis();
        milliseconds -= eventStart.getTimeZone().getRawOffset();
        eventStart.setTimeInMillis(milliseconds);

        milliseconds = eventEnd.getTimeInMillis();
        milliseconds -= eventEnd.getTimeZone().getRawOffset();
        eventEnd.setTimeInMillis(milliseconds - 1000);
    }

    /**
     * Gets the resourceId of for the given color.  If we don't have a record of the
     * given color, we default to blue.
     *
     * @param color
     * The color to get the resource id of
     * @return
     * The resource id of for the given color, if it exists. Blue otherwise.
     */
    public static Bitmap getColorCalendarBitmap(Context ctx, String color) {
        SVG svg = SVGParser.getSVGFromResource(ctx.getResources(), R.raw.calendaricon, 0xFF0000FF, Color.parseColor(color));
        PictureDrawable pd = svg.createPictureDrawable();

        Bitmap bitmap = Bitmap.createBitmap(pd.getIntrinsicWidth(), pd.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        pd.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        pd.draw(canvas);

        return bitmap;
    }

    /**
     * Gets the time of the start and end of the given event, formatted in this manner:
     * "All Day" if the event is an all day event
     * "HH:MM AM/PM - HH:MM AM/PM"
     *
     * @param d
     * The event to get the time for
     * @return
     * A string with the given event's start and end time formatted as listed above
     */
    public String getFormattedTimeString(Context ctx, Event d) {
        GregorianCalendar todayDate = new GregorianCalendar();
        todayDate.setTimeInMillis(System.currentTimeMillis());

        Date startDate = d.getStart().getTime();
        Date endDate = d.getEnd().getTime();

        SimpleDateFormat formatter;

        if (d.isAllDay()) {
            return ctx.getResources().getString(R.string.allDay);
        } else if (use24Hour){
            formatter = new SimpleDateFormat("H:mm");
        } else {
            formatter = new SimpleDateFormat("h:mm a");
        }

        String startString = formatter.format(startDate);
        String endString = formatter.format(endDate);

        if (d.getStart().before(todayDate) && d.getEnd().after(todayDate)) {
            return ctx.getResources().getString(R.string.now) + " - " + endString;
        } else {
            return startString + " - " + endString;
        }
    }

    /**
     * Formats the given Event's date as: <br />
     * {Month} {Day}
     *
     * @param e
     * The Event to format the date of
     * @return
     * The Month and Day for the given event
     */
    private static String getFormattedDateString(Event e, String formatString) {
        Date date = e.getStart().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(formatString);
        return formatter.format(date);
    }

    /**
     * Figures out whether the given Event is today or tomorrow.  If neither,
     * returns the formatted date string.
     *
     * @param d
     * The event to check
     * @return
     * Today, Tomorrow, or the date
     */
    public static String getDateString(Context ctx, Event d) {
        return getDateString(ctx, d, null);
    }

    public static String getDateString(Context ctx, Event d, String formatString) {
        GregorianCalendar now = new GregorianCalendar();
        now.setTimeInMillis(System.currentTimeMillis());

        // If event's start date is current date + 1, or the event's start date is 1 (beginning of new year)
        // and today is the maximum day of year, we return tomorrow.
        if (d.getStart().get(Calendar.DAY_OF_YEAR) == (now.get(Calendar.DAY_OF_YEAR) + 1) ||
                (now.get(Calendar.DAY_OF_YEAR) == now.getActualMaximum(Calendar.DAY_OF_YEAR) && d.getStart().get(Calendar.DAY_OF_YEAR) == 1)) {
            return ctx.getResources().getString(R.string.tomorrow);
        } else if (d.getStart().get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
            return ctx.getResources().getString(R.string.today);
        } else {
            if (formatString == null) {
                return getFormattedDateString(d, "MMM d");
            } else {
                return getFormattedDateString(d, formatString);
            }
        }
    }

}