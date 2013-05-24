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
package org.reber.agenda;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.reber.agenda.list.Event;
import org.reber.agenda.util.CalendarUtilities;
import org.reber.agenda.util.Constants;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

/**
 * This is the class that gets run when the widget is added to the homescreen,
 * when the widget is updated, and when the widget is removed from the homescreen.
 * 
 * @author brianreber
 */
public class AgendaWidgetProvider extends AppWidgetProvider {

	public static String WIDGET_NEXT_EVENT = "org.reber.agenda.AGENDA_WIDGET_NEXT_EVENT";
	public static String WIDGET_UPDATE = "org.reber.agenda.AGENDA_WIDGET_UPDATE";

	public static String NEXT_POS_EXTRA = "NEXT_POS";
	public static String WIDGET_ID_EXTRA = "WIDGET_ID";
	private static CalendarUtilities util;
	private static ArrayList<Integer> ids = new ArrayList<Integer>();

	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (WIDGET_UPDATE.equals(action)) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				int[] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
				if (appWidgetIds != null && appWidgetIds.length > 0) {
					this.onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
				}
			}
		} else if (WIDGET_NEXT_EVENT.equals(action)) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				int appWidgetId = extras.getInt(WIDGET_ID_EXTRA);
				updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId, intent.getIntExtra(NEXT_POS_EXTRA, 0));
			}
		} else {
			super.onReceive(context, intent);
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, 0);

            if (!ids.contains(Integer.valueOf(appWidgetId))) {
                AgendaConfigure.setAlarm(context, appWidgetId);
            }
        }
	}

	/**
	 * Updates the widget with the given widgetId.
	 * 
	 * @param context
	 * @param appWidgetManager
	 * @param appWidgetId
	 * The id of the widget to update
	 */
	public static void updateAppWidget(final Context context, AppWidgetManager appWidgetManager, final int appWidgetId, int eventPos) {
		// Get the package name of the app we want to use when the user clicks on the widget
		SharedPreferences pref = context.getSharedPreferences(Constants.Widget.WIDGET_PREFS + "" + appWidgetId, 0);
		final String packageName = pref.getString(Constants.Widget.PACKAGE_NAME, "");
		int textColor = pref.getInt(Constants.Widget.TEXT_COLOR, context.getResources().getColor(R.color.white));
		int bgColor = pref.getInt(Constants.Widget.BG_COLOR, context.getResources().getColor(R.color.black));
		int transparency = pref.getInt(Constants.Widget.TRANSPARENCY, 0xFF000000);
		int widgetVersion = pref.getInt(Constants.Widget.VERSION, 0);
		int numDays = pref.getInt(Constants.Widget.NUM_DAYS, 2);

		bgColor &= 0x00FFFFFF;
		bgColor |= (transparency << 24);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		Intent intent = new Intent();
		PendingIntent pendingIntent = null;

		if (packageName.equals("org.reber.agenda")) {
			intent = new Intent(context, AgendaActivity.class);
			intent.putExtra(AgendaActivity.WIDGET_EXTRA, appWidgetId);
			pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);
		} else if (!packageName.equals("")) {
			// Open app
			intent = new Intent(Intent.ACTION_MAIN);
			intent.setPackage(packageName);
			pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

			// Go to configuration screen
			//			intent = new Intent(context, AgendaConfigure.class);
			//			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			//			pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		}


		// Get the layout for the App Widget and attach an on-click listener to the button
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.agenda);
		views.setTextViewText(R.id.day_agenda, calendar.get(Calendar.DATE)+"");
		views.setTextViewText(R.id.month_agenda, CalendarUtilities.monthFormat.format(calendar.getTime()));
		views.setInt(R.id.agenda_frame, "setBackgroundColor", bgColor);
		setTextColor(views, textColor);

		Collection<Event> events = null;
		if (util == null) {
			util = new CalendarUtilities(context, pref.getBoolean(Constants.Widget.USE_24_HR, false));
		}

		try {
			util.setSelectedCalendars(util.getSelectedCalendarFromPref(Constants.Widget.WIDGET_PREFS + "" + appWidgetId));
		} catch (NoSuchElementException e) {
			try {
				util.setSelectedCalendars(util.getSelectedCalendarFromPref(Constants.Widget.WIDGET_PREFS));
			} catch (NoSuchElementException ex) {
				try {
					util.setSelectedCalendars(util.getSelectedCalendarFromPref(Constants.AgendaList.APP_PREFS));
				} catch (NoSuchElementException exc) {
					views.setViewVisibility(R.id.event_view, View.INVISIBLE);
					views.setViewVisibility(R.id.empty_msg, View.VISIBLE);
					views.setTextViewText(R.id.empty_msg, "Please re-add widget to home screen");
					appWidgetManager.updateAppWidget(appWidgetId, views);
					return;
				}
			}
		}
		events = util.getCalendarData(numDays, false);

		if (pendingIntent != null) {
			views.setOnClickPendingIntent(R.id.agenda_frame, pendingIntent);
		}

		if (context.getResources().getIntArray(R.array.versions)[0] > widgetVersion) {
			// On the first time opening the widget, we need to have them select their preferences
			//			views.setViewVisibility(R.id.event_view, View.INVISIBLE);
			//			views.setViewVisibility(R.id.empty_msg, View.VISIBLE);
			//			views.setTextViewText(R.id.empty_msg, "Please re-add widget to home screen");

			Editor edit = pref.edit();
			edit.putInt(Constants.Widget.VERSION, context.getResources().getIntArray(R.array.versions)[0]);
			edit.commit();
		} else if (events == null || events.isEmpty()) {
			// There aren't any events in the near future, so we display the default message
			views.setViewVisibility(R.id.event_view, View.INVISIBLE);
			views.setViewVisibility(R.id.empty_msg, View.VISIBLE);
		} else {
			views.setViewVisibility(R.id.empty_msg, View.INVISIBLE);
			views.setViewVisibility(R.id.event_view, View.VISIBLE);

			Iterator<Event> iter = events.iterator();
			Event first = iter.next();

			for (int i = 0; i < eventPos; i++) {
				try {
					first = iter.next();
				} catch (NoSuchElementException e) {
					// If this event is null, then we will start over at beginning
					iter = events.iterator();
					first = iter.next();
				}
			}

			String tomorrow = CalendarUtilities.getDateString(context, first);
			String location = first.getLocation();
			// If there is no location, we want to center the stuff on the widget
			if (location == null || location.equals("")) {
				views.setViewVisibility(R.id.event_view_text, View.INVISIBLE);
				views.setViewVisibility(R.id.event_view_text_no_loc, View.VISIBLE);

				views.setTextViewText(R.id.when_no_loc,  (!tomorrow.contains(context.getResources().getText(R.string.today)) ? tomorrow + " ": "") +
						util.getFormattedTimeString(context, first));

				views.setTextViewText(R.id.item_title_no_loc, first.getTitle());
				views.setViewVisibility(R.id.event_num_no_loc, View.INVISIBLE);
				views.setViewVisibility(R.id.widgetGoBackToFirstNoLoc, View.INVISIBLE);
			} else {
				views.setViewVisibility(R.id.event_view_text_no_loc, View.INVISIBLE);
				views.setViewVisibility(R.id.event_view_text, View.VISIBLE);

				views.setTextViewText(R.id.where, location);
				views.setTextViewText(R.id.when,  (!tomorrow.contains(context.getResources().getText(R.string.today)) ? tomorrow + " ": "") +
						util.getFormattedTimeString(context, first));

				views.setTextViewText(R.id.item_title, first.getTitle());
				views.setViewVisibility(R.id.event_num, View.INVISIBLE);
				views.setViewVisibility(R.id.widgetGoBackToFirst, View.INVISIBLE);
			}

			views.setImageViewBitmap(R.id.calendar_item, CalendarUtilities.getColorCalendarBitmap(context, first.getColor()));
		}

		// Tell the AppWidgetManager to perform an update on the current App Widget
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	/**
	 * Sets the text color on the widget
	 * 
	 * @param views
	 * The RemoteView that contains the textviews
	 * @param color
	 * The color to set the text to
	 */
	private static void setTextColor(RemoteViews views, int color) {
		views.setTextColor(R.id.empty_msg, color);
		views.setTextColor(R.id.when, color);
		views.setTextColor(R.id.item_title, color);
		views.setTextColor(R.id.where, color);
		views.setTextColor(R.id.when_no_loc, color);
		views.setTextColor(R.id.item_title_no_loc, color);
	}

	/**
	 * Adds the id of a widget to the list of widgets that have alarms set
	 * @param ctx
	 * @param id
	 * The id of the widget
	 */
	public static void addIdToAlarm(Context ctx, int id) {
		ids.add(id);
	}

	/**
	 * Removes the id of a widget from the list of widgets that have alarms set
	 * @param ctx
	 * @param id
	 * The id of the widget
	 */
	public static void removeIdFromList(Context ctx, int id) {
		ids.remove(Integer.valueOf(id));
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);

		for (int appWidgetId : appWidgetIds) {
			Log.d(Constants.TAG, "DELETING - " + appWidgetId);
			SharedPreferences pref = context.getSharedPreferences(Constants.Widget.WIDGET_PREFS + "" + appWidgetId, 0);
			Editor edit = pref.edit();
			edit.putInt(Constants.Widget.BG_COLOR, context.getResources().getColor(R.color.black));
			edit.putInt(Constants.Widget.TEXT_COLOR, context.getResources().getColor(R.color.white));
			edit.putInt(Constants.Widget.TRANSPARENCY, 0xFF000000);
			edit.commit();

			if (util != null) {
				util.setSelectedCalendars(new HashSet<AndroidCalendar>());
				util.saveSelectedCalendarsPref(Constants.Widget.WIDGET_PREFS + "" + appWidgetId);
			} else {
				new CalendarUtilities(context, false).saveSelectedCalendarsPref(Constants.Widget.WIDGET_PREFS + "" + appWidgetId);
			}

			// When the widget is disabled, we want to remove the update alarm
			AgendaConfigure.cancelAlarm(context, appWidgetId);
		}
	}

	/**
	 * Adds the instance of CalendarUtil to the widget for future use
	 * 
	 * @param utl
	 * The instance of CalendarUtilities to use
	 */
	public static void saveUtil(CalendarUtilities utl) {
		util = utl;
	}
}
