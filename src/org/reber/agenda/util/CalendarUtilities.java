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
package org.reber.agenda.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Set;

import org.reber.agenda.AndroidCalendar;
import org.reber.agenda.R;
import org.reber.agenda.list.Event;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.util.Log;

/**
 * A class containing some simple utilities that deal with Calendar data.
 * 
 * @author brianreber
 */
public class CalendarUtilities {

	private Set<org.reber.agenda.AndroidCalendar> calendars;
	private Set<org.reber.agenda.AndroidCalendar> selectedCalendars;
	private Context context;
	private boolean use24Hour;

	public static SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");

	private static final Map<String, Integer> colorBarResources = new HashMap<String, Integer>();
	private static final Map<String, Integer> colorCalendarResources = new HashMap<String, Integer>();

	static {
		colorBarResources.put("a32929", R.drawable.agenda_calendar_01);
		colorBarResources.put("b1365f", R.drawable.agenda_calendar_02);
		colorBarResources.put("7a367a", R.drawable.agenda_calendar_03);
		colorBarResources.put("5229a3", R.drawable.agenda_calendar_04);
		colorBarResources.put("29527a", R.drawable.agenda_calendar_05);
		colorBarResources.put("2952a3", R.drawable.agenda_calendar_06);
		colorBarResources.put("1b887a", R.drawable.agenda_calendar_07);
		colorBarResources.put("28754e", R.drawable.agenda_calendar_08);
		colorBarResources.put("0d7813", R.drawable.agenda_calendar_09);
		colorBarResources.put("528800", R.drawable.agenda_calendar_10);
		colorBarResources.put("88880e", R.drawable.agenda_calendar_11);
		colorBarResources.put("ab8b00", R.drawable.agenda_calendar_12);
		colorBarResources.put("be6d00", R.drawable.agenda_calendar_13);
		colorBarResources.put("b1440e", R.drawable.agenda_calendar_14);
		colorBarResources.put("865a5a", R.drawable.agenda_calendar_15);
		colorBarResources.put("705770", R.drawable.agenda_calendar_16);
		colorBarResources.put("4e5d6c", R.drawable.agenda_calendar_17);
		colorBarResources.put("5a6986", R.drawable.agenda_calendar_18);
		colorBarResources.put("4a716c", R.drawable.agenda_calendar_19);
		colorBarResources.put("6e6e41", R.drawable.agenda_calendar_20);
		colorBarResources.put("8d6f47", R.drawable.agenda_calendar_21);
		colorBarResources.put("853104", R.drawable.agenda_calendar_22);
		colorBarResources.put("691426", R.drawable.agenda_calendar_23);
		colorBarResources.put("5c1158", R.drawable.agenda_calendar_24);
		colorBarResources.put("23164e", R.drawable.agenda_calendar_25);
		colorBarResources.put("182c57", R.drawable.agenda_calendar_26);
		colorBarResources.put("060d5e", R.drawable.agenda_calendar_27);
		colorBarResources.put("125a12", R.drawable.agenda_calendar_28);
		colorBarResources.put("2f6213", R.drawable.agenda_calendar_29);
		colorBarResources.put("2f6309", R.drawable.agenda_calendar_30);
		colorBarResources.put("5f6b02", R.drawable.agenda_calendar_31);
		colorBarResources.put("8c500b", R.drawable.agenda_calendar_32);
		colorBarResources.put("754916", R.drawable.agenda_calendar_34);
		colorBarResources.put("6b3304", R.drawable.agenda_calendar_35);
		colorBarResources.put("5b123b", R.drawable.agenda_calendar_36);
		colorBarResources.put("42104a", R.drawable.agenda_calendar_37);
		colorBarResources.put("113f47", R.drawable.agenda_calendar_38);
		colorBarResources.put("333333", R.drawable.agenda_calendar_39);
		colorBarResources.put("0f4b38", R.drawable.agenda_calendar_40);
		colorBarResources.put("856508", R.drawable.agenda_calendar_41);
		colorBarResources.put("711616", R.drawable.agenda_calendar_42);


		colorCalendarResources.put("a32929", R.drawable.calendar_icon_01);
		colorCalendarResources.put("b1365f", R.drawable.calendar_icon_02);
		colorCalendarResources.put("7a367a", R.drawable.calendar_icon_03);
		colorCalendarResources.put("5229a3", R.drawable.calendar_icon_04);
		colorCalendarResources.put("29527a", R.drawable.calendar_icon_05);
		colorCalendarResources.put("2952a3", R.drawable.calendar_icon_06);
		colorCalendarResources.put("1b887a", R.drawable.calendar_icon_07);
		colorCalendarResources.put("28754e", R.drawable.calendar_icon_08);
		colorCalendarResources.put("0d7813", R.drawable.calendar_icon_09);
		colorCalendarResources.put("528800", R.drawable.calendar_icon_10);
		colorCalendarResources.put("88880e", R.drawable.calendar_icon_11);
		colorCalendarResources.put("ab8b00", R.drawable.calendar_icon_12);
		colorCalendarResources.put("be6d00", R.drawable.calendar_icon_13);
		colorCalendarResources.put("b1440e", R.drawable.calendar_icon_14);
		colorCalendarResources.put("865a5a", R.drawable.calendar_icon_15);
		colorCalendarResources.put("705770", R.drawable.calendar_icon_16);
		colorCalendarResources.put("4e5d6c", R.drawable.calendar_icon_17);
		colorCalendarResources.put("5a6986", R.drawable.calendar_icon_18);
		colorCalendarResources.put("4a716c", R.drawable.calendar_icon_19);
		colorCalendarResources.put("6e6e41", R.drawable.calendar_icon_20);
		colorCalendarResources.put("8d6f47", R.drawable.calendar_icon_21);
		colorCalendarResources.put("853104", R.drawable.calendar_icon_22);
		colorCalendarResources.put("691426", R.drawable.calendar_icon_23);
		colorCalendarResources.put("5c1158", R.drawable.calendar_icon_24);
		colorCalendarResources.put("23164e", R.drawable.calendar_icon_25);
		colorCalendarResources.put("182c57", R.drawable.calendar_icon_26);
		colorCalendarResources.put("060d5e", R.drawable.calendar_icon_27);
		colorCalendarResources.put("125a12", R.drawable.calendar_icon_28);
		colorCalendarResources.put("2f6213", R.drawable.calendar_icon_29);
		colorCalendarResources.put("2f6309", R.drawable.calendar_icon_30);
		colorCalendarResources.put("5f6b02", R.drawable.calendar_icon_31);
		colorCalendarResources.put("8c500b", R.drawable.calendar_icon_32);
		colorCalendarResources.put("8c500b", R.drawable.calendar_icon_33);
		colorCalendarResources.put("754916", R.drawable.calendar_icon_34);
		colorCalendarResources.put("6b3304", R.drawable.calendar_icon_35);
		colorCalendarResources.put("5b123b", R.drawable.calendar_icon_36);
		colorCalendarResources.put("42104a", R.drawable.calendar_icon_37);
		colorCalendarResources.put("113f47", R.drawable.calendar_icon_38);
		colorCalendarResources.put("333333", R.drawable.calendar_icon_39);
		colorCalendarResources.put("0f4b38", R.drawable.calendar_icon_40);
		colorCalendarResources.put("856508", R.drawable.calendar_icon_41);
		colorCalendarResources.put("711616", R.drawable.calendar_icon_42);
	}

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
			pref = context.getSharedPreferences(prefName, Activity.MODE_WORLD_READABLE);
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
		SharedPreferences pref = context.getSharedPreferences(prefName, Activity.MODE_WORLD_WRITEABLE);
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
			hex &= 0x00FFFFFF;

			return String.format("%06x", hex);
		} catch (NumberFormatException e) {
			return "";
		}
	}

	/**
	 * Actually get the events from the given calendar
	 * 
	 * @param context
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
			boolean allDay = !cursor.getString(3).equals("0");

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
	 * @param event
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
	public static int getColorBarResource(String color) {
		if (colorBarResources.containsKey(color)) {
			return colorBarResources.get(color);
		} else {
			return colorBarResources.get("060d5e");
		}
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
	public static int getColorCalendarResource(String color) {
		if (colorCalendarResources.containsKey(color)) {
			return colorCalendarResources.get(color);
		} else {
			return colorCalendarResources.get("060d5e");
		}
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
	 * @param d
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