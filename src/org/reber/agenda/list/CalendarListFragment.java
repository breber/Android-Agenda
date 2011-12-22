package org.reber.agenda.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reber.agenda.AndroidCalendar;
import org.reber.agenda.R;
import org.reber.agenda.util.CalendarUtilities;
import org.reber.agenda.util.Constants;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A fragment that shows the user's calendars, and if there is an AgendaListFragment,
 * notifies it that there has been an update allowing it to refresh the events for the selected calendars.
 * 
 * @author breber
 */
public class CalendarListFragment extends ListFragment {

	private Set<AndroidCalendar> cals;
	private CalendarUtilities util;
	private Map<Integer, Boolean> checked;

	private String pref = Constants.AgendaList.APP_PREFS;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		util = new CalendarUtilities(getActivity(), false);
		util.setSelectedCalendars(util.getSelectedCalendarFromPref(pref));


		cals = util.getAvailableCalendars();
		checked = new HashMap<Integer, Boolean>();

		updateList();
	}

	/**
	 * By default, this fragment will use the App's preference.  If you want to change which preference is used, call
	 * this method before the user has a chance to click on anything.
	 * 
	 * @param pref - the preference to use to grab the selected calendars
	 */
	public void setPrefToGrabFrom(String pref) {
		util.setSelectedCalendars(util.getSelectedCalendarFromPref(pref));
		this.pref = pref;
		updateList();
	}

	/**
	 * Gets the list of available calendars, and the selected calendars. Sets up the checked map
	 * by comparing the selected calandars with the available calendars. Then sets the list adapter
	 */
	private void updateList() {
		Set<AndroidCalendar> selectedCals = util.getSelectedCalendars();

		ArrayList<String> tempCals = new ArrayList<String>();
		int temp = 0;
		for (AndroidCalendar c : cals) {
			tempCals.add(c.getName());

			if (selectedCals.contains(c)) {
				checked.put(temp, true);
			} else {
				checked.put(temp, false);
			}
			temp++;
		}

		setListAdapter(new CalendarListAdapter(getActivity(), R.layout.selectionrows, tempCals));
	}

	/**
	 * When an item in this list is clicked, we will save the checked state in our checked map, and then
	 * update the image appropriately. Then we will update our util instance, and save the preferences.
	 * If there is an AgendaListFragment, we will notify it that the util has been updated so it can refresh the
	 * list of events
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		checked.put(position, !checked.get(position));

		if (checked.get(position)) {
			((ImageView) v.findViewById(R.id.chec)).setImageResource(android.R.drawable.checkbox_on_background);
		} else {
			((ImageView) v.findViewById(R.id.chec)).setImageResource(android.R.drawable.checkbox_off_background);
		}

		Set<AndroidCalendar> selected = new HashSet<AndroidCalendar>();
		Iterator<AndroidCalendar> iter = cals.iterator();
		for (int i = 0; i < cals.size(); i++) {
			if (checked.get(i)) {
				selected.add(iter.next());
			} else {
				iter.next();
			}
		}

		util.setSelectedCalendars(selected);
		util.saveSelectedCalendarsPref(pref);

		AgendaListFragment frag = (AgendaListFragment) getFragmentManager().findFragmentById(R.id.list_frag);
		if (frag != null) {
			frag.notifyUtilUpdated();
		}
	}

	public class CalendarListAdapter extends ArrayAdapter<String> implements ListAdapter {
		public CalendarListAdapter(Context context, int textViewResourceId,	List<String> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.selectionrows, null);
			}

			TextView tv = (TextView) v.findViewById(R.id.checkbox);
			// Set the data in the views with the appropriate data
			tv.setText(getItem(position));

			// Get the necessary views
			ImageView cb = (ImageView) v.findViewById(R.id.chec);

			if (checked.get(position)) {
				cb.setImageResource(android.R.drawable.checkbox_on_background);
			} else {
				cb.setImageResource(android.R.drawable.checkbox_off_background);
			}

			return v;
		}
	}

}