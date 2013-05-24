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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reber.agenda.util.CalendarUtilities;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * A dialog that is used to allow the user to pick which calendars they want
 * to display
 * 
 * @author brianreber
 */
public class CalendarPickerDialog extends Dialog {
	private ListView lv;
	private ListAdapter adapter;
    private Set<org.reber.agenda.AndroidCalendar> cals;
	private CalendarUtilities util;
	private Context context;
	private List<String> ids;
	private Map<Integer, Boolean> checked;

	/**
	 * Creates a dialog box with a list of the Calendars the user has on their phone,
	 * so that they can check which calendars they want to display
	 * 
	 * @param context
	 * @param util
	 */
    public CalendarPickerDialog(Context context, CalendarUtilities util) {
        super(context);
        this.context = context;
        cals = new HashSet<org.reber.agenda.AndroidCalendar>();
        ids = new ArrayList<String>();
        this.util = util;
        checked = new HashMap<Integer, Boolean>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.cals);
        
        lv = (ListView) findViewById(R.id.ListView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (checked.containsKey(arg2)) {
					checked.put(arg2, !checked.get(arg2));
					lv.setItemChecked(arg2, checked.get(arg2));
				} else {
					checked.put(arg2, true);
					lv.setItemChecked(arg2, checked.get(arg2));
				}
			}
		});
        
        setTitle(context.getResources().getString(R.string.chooseCalendars));
        
        updateList();
        
        Button b = (Button) findViewById(R.id.saveSelectedCalendars);
        b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cals = new HashSet<org.reber.agenda.AndroidCalendar>();
				
				Set<org.reber.agenda.AndroidCalendar> events = util.getAvailableCalendars();
				Set<String> ch = new HashSet<String>();
				
				for (Integer i : checked.keySet()) {
					if (checked.get(i)) {
						ch.add(ids.get(i));
					}
				}

				for (org.reber.agenda.AndroidCalendar cal : events) {
					if (ch.contains(cal.getId())) {
						CalendarPickerDialog.this.cals.add(cal);
					}
				}
				
				util.setSelectedCalendars(CalendarPickerDialog.this.cals);
				dismiss();
			}
		});
    }
    
    /**
     * Creates the list of calendars to be shown in the dialog box
     */
	private void updateList() {
		Set<org.reber.agenda.AndroidCalendar> events = util.getAvailableCalendars();
		Set<org.reber.agenda.AndroidCalendar> selectedCals = util.getSelectedCalendars();
		
		ArrayList<String> cals = new ArrayList<String>();
		ArrayList<Integer> selectedPos = new ArrayList<Integer>();
		int temp = 0;
		for (org.reber.agenda.AndroidCalendar c : events) {
			cals.add(c.getName());
			ids.add(c.getId());
			
			if (selectedCals.contains(c)) {
				checked.put(temp, true);
				selectedPos.add(temp);
			}
			temp++;
		}
		
		adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_multiple_choice, cals);
		lv.setAdapter(adapter);
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		for (Integer i : selectedPos) {
			lv.setItemChecked(i, true);
		}
	}
	
	/**
	 * Gets the calendars that were selected in the CalendarPickerDialog
	 * 
	 * @return
	 * The calendars that were selected in the dialog
	 */
	public Set<org.reber.agenda.AndroidCalendar> getCalendars() {
		return cals;
	}
}