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
package org.reber.agenda;

import java.util.HashSet;
import java.util.NoSuchElementException;

import org.reber.agenda.util.CalendarUtilities;
import org.reber.agenda.util.Constants;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * This is the Activity that gets loaded when the user clicks on the app icon,
 * just like they would do to open any other application.
 * 
 * @author brianreber
 */
public class SettingsActivity extends Activity {
	private CalendarUtilities util;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		// Prevent auto opening of keyboard
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		final SharedPreferences pref = getSharedPreferences(Constants.AgendaList.APP_PREFS, MODE_WORLD_WRITEABLE);
		final CheckBox use24 = (CheckBox) findViewById(R.id.appUse24Hour);
		use24.setChecked(pref.getBoolean(Constants.AgendaList.USE_24_HR, false));
		
		final CheckBox enableClick = (CheckBox) findViewById(R.id.appEnableClick);
		enableClick.setChecked(pref.getBoolean(Constants.AgendaList.ENABLE_CLICK_EVENT, true));
		
		final EditText numDays = (EditText) findViewById(R.id.numDays);
		numDays.setText(pref.getInt(Constants.AgendaList.NUM_DAYS, 7) + "");
		
		util = new CalendarUtilities(this, pref.getBoolean(Constants.AgendaList.USE_24_HR, false));
		
		Button configChooseCalendarsButton = (Button) findViewById(R.id.appChooseCalsButton);
		configChooseCalendarsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final CalendarPickerDialog dlg = new CalendarPickerDialog(SettingsActivity.this, util);
				try {
					util.setSelectedCalendars(util.getSelectedCalendarFromPref(Constants.AgendaList.APP_PREFS));
				} catch (NoSuchElementException e) {
					util.setSelectedCalendars(new HashSet<AndroidCalendar>());
				}
				dlg.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						dlg.getCalendars();
						util.saveSelectedCalendarsPref(Constants.AgendaList.APP_PREFS);
						try {
							util.setSelectedCalendars(util.getSelectedCalendarFromPref(Constants.AgendaList.APP_PREFS));
						} catch (NoSuchElementException e) {
							// This should never happen because we just saved the preferences, but just in case
							util.setSelectedCalendars(new HashSet<AndroidCalendar>());
						}
					}
				});
				dlg.show();			
			}
		});
		
		Button configSaveSettings = (Button) findViewById(R.id.saveAppSettings);
		configSaveSettings.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Save the package name in the preferences
				Editor edit = pref.edit();
				edit.putBoolean(Constants.AgendaList.USE_24_HR, use24.isChecked());
				edit.putBoolean(Constants.AgendaList.ENABLE_CLICK_EVENT, enableClick.isChecked());
				try {
					edit.putInt(Constants.AgendaList.NUM_DAYS, Integer.parseInt(numDays.getText().toString()));
				} catch (NumberFormatException e) {
					edit.putInt(Constants.AgendaList.NUM_DAYS, 7);
				}
				edit.commit();
				util.setUse24Hour(use24.isChecked());
				SettingsActivity.this.setResult(RESULT_OK);
				SettingsActivity.this.finish();
			}
		});
	}

}