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
import java.util.NoSuchElementException;

import org.reber.agenda.ColorPickerDialog.OnColorChangedListener;
import org.reber.agenda.list.PackageInfoWrapper;
import org.reber.agenda.list.PackageListAdapter;
import org.reber.agenda.util.CalendarUtilities;
import org.reber.agenda.util.Constants;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the Activity that shows up when the user first says
 * they want to add our Agenda widget to the homescreen.  It is
 * used to configure the widget to act how they want it to.
 * 
 * @author brianreber
 */
public class AgendaConfigure extends Activity {
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	private static ArrayList<String> textSizes;

	private int textColor;
	private int bgColor;
	private int transparency;
	private int numDays;
	private CalendarUtilities util;

	// UI Elements
	private Spinner appsSpinner;
	private Button configOkButton;
	private ImageView textColorSwatch;
	private ImageView bgColorSwatch;
	private Button chooseCalendarsButton;
	private Button textColorButton;
	private Button bgColorButton;
	private TextView numDaysTextView;
	private CheckBox twentyFourHourCheckBox;
	private SeekBar transparencySeekBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configure);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}

		initializeUIElementVars();

		textColor = getResources().getColor(R.color.white);
		bgColor = getResources().getColor(R.color.black);

		// Prevent auto opening of keyboard
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// Set the list of proposed apps for the click handler
		appsSpinner.setAdapter(new PackageListAdapter(this, android.R.layout.simple_spinner_dropdown_item));

		configOkButton.setOnClickListener(configOkButtonOnClickListener);

		SharedPreferences pref = getSharedPreferences(Constants.Widget.WIDGET_PREFS + "" + mAppWidgetId, 0);
		util = new CalendarUtilities(this, pref.getBoolean(Constants.Widget.USE_24_HR, false));

		chooseCalendarsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final CalendarPickerDialog dlg = new CalendarPickerDialog(AgendaConfigure.this, util);
				try {
					util.setSelectedCalendars(util.getSelectedCalendarFromPref(Constants.Widget.WIDGET_PREFS + "" + mAppWidgetId));
				} catch (NoSuchElementException e) {
					// If there aren't any selected calendars before we open the dialog, we just let them re-choose
				}
				dlg.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						dlg.getCalendars();
						util.saveSelectedCalendarsPref(Constants.Widget.WIDGET_PREFS + "" + mAppWidgetId);
						try {
							util.setSelectedCalendars(util.getSelectedCalendarFromPref(Constants.Widget.WIDGET_PREFS + "" + mAppWidgetId));
						} catch (NoSuchElementException e) {
							// This shouldn't ever happen because we save the calendars in the previous step, but just in case
						}
						AgendaWidgetProvider.saveUtil(util);
					}
				});
				dlg.show();
			}
		});

		textColorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				OnColorChangedListener ocl = new ColorPickerDialog.OnColorChangedListener() {
					@Override
					public void colorChanged(int color) {
						textColor = color;
						textColorSwatch.setBackgroundColor(color);
					}
				};
				Dialog dlg = new ColorPickerDialog(AgendaConfigure.this, ocl, textColor);
				dlg.show();
			}
		});

		bgColorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				OnColorChangedListener ocl = new ColorPickerDialog.OnColorChangedListener() {
					@Override
					public void colorChanged(int color) {
						bgColor = color;
						bgColorSwatch.setBackgroundColor(color);
					}
				};
				Dialog dlg = new ColorPickerDialog(AgendaConfigure.this, ocl, bgColor);
				dlg.show();
			}
		});

		transparencySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {	}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				transparency = progress;
			}
		});

		setValuesBasedOnPrefs();
	}

	/**
	 * Initialize the member variables for the UI elements
	 */
	private void initializeUIElementVars() {
		textSizes = new ArrayList<String>();
		textSizes.add(getResources().getString(R.string.large));
		textSizes.add(getResources().getString(R.string.medium));
		textSizes.add(getResources().getString(R.string.small));

		appsSpinner = (Spinner) findViewById(R.id.recommendedapps);
		configOkButton = (Button) findViewById(R.id.enablewidgets);
		textColorSwatch = (ImageView) findViewById(R.id.widgetTextColorSwatch);
		bgColorSwatch = (ImageView) findViewById(R.id.widgetBGColorSwatch);
		chooseCalendarsButton = (Button) findViewById(R.id.widgetChooseCalsButton);
		textColorButton = (Button) findViewById(R.id.widgetTextColorButton);
		bgColorButton = (Button) findViewById(R.id.widgetBGColorButton);
		twentyFourHourCheckBox = (CheckBox) findViewById(R.id.widgetUse24Hour);
		transparencySeekBar = (SeekBar) findViewById(R.id.widgetTransparencyBar);
		transparencySeekBar.setMax(0xFF);

		numDaysTextView = (TextView) findViewById(R.id.widgetNumDays);
	}

	/**
	 * Set the values of the UI elements with the proper values from the
	 * preferences for the widget.
	 */
	private void setValuesBasedOnPrefs() {
		SharedPreferences pref = getSharedPreferences(Constants.Widget.WIDGET_PREFS + "" + mAppWidgetId, 0);

		int selection = 0;

		// START APP SPINNER
		String temp = pref.getString(Constants.Widget.PACKAGE_NAME, "");
		for (int i = 0; i < appsSpinner.getCount(); i++) {
			if (temp.equals(((PackageInfoWrapper) appsSpinner.getItemAtPosition(i)).packageName)) {
				selection = i;
				break;
			}
		}
		appsSpinner.setSelection(selection);
		// END APP SPINNER

		// START TEXT COLOR
		textColor = pref.getInt(Constants.Widget.TEXT_COLOR, getResources().getColor(R.color.white));
		textColorSwatch.setBackgroundColor(textColor);
		// END TEXT COLOR

		// START BG COLOR
		bgColor = pref.getInt(Constants.Widget.BG_COLOR, getResources().getColor(R.color.black));
		bgColorSwatch.setBackgroundColor(bgColor);
		// END BG COLOR

		// START TRANSPARENCY
		transparency = pref.getInt(Constants.Widget.TRANSPARENCY, transparencySeekBar.getMax() / 2);
		transparencySeekBar.setProgress(transparency);
		// END TRANSPARENCY

		// START NUM DAYS
		numDays = pref.getInt(Constants.Widget.NUM_DAYS, 2);
		numDaysTextView.setText(numDays + "");
		// END NUM DAYS

		// START 24-HOUR TIME
		twentyFourHourCheckBox.setChecked(pref.getBoolean(Constants.Widget.USE_24_HR, false));
		// END 24-HOUR TIME
	}

	private Button.OnClickListener configOkButtonOnClickListener = new Button.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			String str = numDaysTextView.getText().toString();
			int numDaysInt = 2;

			if (str != null && !str.equals("")) {
				try {
					numDaysInt = Integer.parseInt(str);

					if (numDaysInt <= 0) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException e) {
					Toast.makeText(AgendaConfigure.this, getResources().getString(R.string.invalidNumberOfDays), Toast.LENGTH_SHORT).show();
					return;
				}
			}

			// Save the package name in the preferences
			SharedPreferences pref = getSharedPreferences(Constants.Widget.WIDGET_PREFS + "" + mAppWidgetId, 0);
			Editor edit = pref.edit();
			edit.putString(Constants.Widget.PACKAGE_NAME, ((PackageInfoWrapper) appsSpinner.getSelectedItem()).packageName);
			edit.putInt(Constants.Widget.BG_COLOR, bgColor);
			edit.putInt(Constants.Widget.TEXT_COLOR, textColor);
			edit.putInt(Constants.Widget.TRANSPARENCY, transparency);
			edit.putBoolean(Constants.Widget.USE_24_HR, twentyFourHourCheckBox.isChecked());
			edit.putInt(Constants.Widget.VERSION, getResources().getIntArray(R.array.versions)[0]);
			edit.putInt(Constants.Widget.NUM_DAYS, numDaysInt);
			edit.commit();
			util.setUse24Hour(twentyFourHourCheckBox.isChecked());

			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(AgendaConfigure.this);
			AgendaWidgetProvider.updateAppWidget(AgendaConfigure.this, appWidgetManager, mAppWidgetId, 0);

			AgendaWidgetProvider.saveUtil(util);

			Intent widgetUpdate = new Intent();
			widgetUpdate.setAction(AgendaWidgetProvider.WIDGET_UPDATE);
			widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { mAppWidgetId });
			sendBroadcast(widgetUpdate);

			// Actually show the widget
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
		}
	};

	/**
	 * Sets a recurring alarm for the given appwidget id.
	 * 
	 * @param ctx
	 * @param mAppWidgetId
	 * The id of the app widget for which this alarm is being set
	 */
	public static void setAlarm(Context ctx, int mAppWidgetId) {
		Log.d(Constants.TAG, "Setting alarm - " + mAppWidgetId);
		Intent widgetUpdate = new Intent();
		widgetUpdate.setAction(AgendaWidgetProvider.WIDGET_UPDATE);
		widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { mAppWidgetId });
		PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, mAppWidgetId, widgetUpdate, 0);
		AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
		// Set it to update every 14 mins
		alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 3000, 14 * 60 * 1000, pendingIntent);

		AgendaWidgetProvider.addIdToAlarm(ctx, mAppWidgetId);
	}

	/**
	 * Removes a recurring alarm for the given appwidget id.
	 * 
	 * @param ctx
	 * @param mAppWidgetId
	 * The id of the app widget to remove the alarm for
	 */
	public static void cancelAlarm(Context ctx, int mAppWidgetId) {
		Log.d(Constants.TAG, "Cancelling alarm - " + mAppWidgetId);
		Intent widgetUpdate = new Intent();
		widgetUpdate.setAction(AgendaWidgetProvider.WIDGET_UPDATE);
		widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { mAppWidgetId });
		PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, mAppWidgetId, widgetUpdate, 0);
		AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);

		AgendaWidgetProvider.removeIdFromList(ctx, mAppWidgetId);
	}
}