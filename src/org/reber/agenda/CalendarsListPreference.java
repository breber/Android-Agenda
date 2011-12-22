package org.reber.agenda;

import java.util.Set;

import org.reber.agenda.util.CalendarUtilities;

import android.content.Context;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

public class CalendarsListPreference extends MultiSelectListPreference {

	private CalendarUtilities util;
	private Set<org.reber.agenda.AndroidCalendar> events;

	public CalendarsListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		util = new CalendarUtilities(context, false);
		util.getSelectedCalendarFromPref(null);

		events = util.getAvailableCalendars();

		CharSequence[] titles = new CharSequence[events.size()];
		CharSequence[] values = new CharSequence[events.size()];
		int i = 0;

		for (AndroidCalendar cal : events) {
			values[i]	= cal.getId();
			titles[i++] = cal.getName();
		}

		setEntries(titles);
		setEntryValues(values);
	}

}
