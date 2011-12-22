package org.reber.agenda.list;

import org.reber.agenda.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * A fragment that shows the user's calendars, and if there is an AgendaListFragment,
 * notifies it that there has been an update allowing it to refresh the events for the selected calendars.
 * 
 * @author breber
 */
public class CalendarListFragment extends PreferenceFragment {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.calendarsettings);
	}

}