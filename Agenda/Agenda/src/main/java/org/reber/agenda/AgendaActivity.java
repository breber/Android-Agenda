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

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.reber.agenda.list.AgendaListFragment;
import org.reber.agenda.util.CalendarUtilities;
import org.reber.agenda.util.Util;

import java.util.*;

/**
 * This is the Activity that gets loaded when the user clicks on the app icon,
 * just like they would do to open any other application.
 *
 * @author brianreber
 */
public class AgendaActivity extends Activity {

    public static final String WIDGET_EXTRA = "appWidgetId";

    private AgendaListFragment frag;
    private ActionBarDrawerToggle mDrawerToggle;
    private CalendarUtilities mCalendarUtils;
    private List<AndroidCalendar> mAvailableCalendars;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setResult(RESULT_OK, new Intent(AgendaWidgetProvider.WIDGET_UPDATE));

        mCalendarUtils = new CalendarUtilities(this, false);

        try {
            mCalendarUtils.setSelectedCalendars(mCalendarUtils.getSelectedCalendarFromPref(null));
        } catch (NoSuchElementException e) {
            mCalendarUtils.setSelectedCalendars(new HashSet<AndroidCalendar>());
        }

        ListView drawerList = (ListView) findViewById(R.id.left_drawer);
        mAvailableCalendars = new ArrayList<AndroidCalendar>(mCalendarUtils.getAvailableCalendars());
        Collections.sort(mAvailableCalendars);

        drawerList.setAdapter(new ArrayAdapter<AndroidCalendar>(AgendaActivity.this,
                android.R.layout.simple_list_item_multiple_choice, mAvailableCalendars));

        drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                Set<AndroidCalendar> selectedCalendars = mCalendarUtils.getSelectedCalendars();
                selectedCalendars.add(mAvailableCalendars.get(position));
                mCalendarUtils.setSelectedCalendars(selectedCalendars);
                mCalendarUtils.saveSelectedCalendarsPref(null);
            }
        });

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer,
                R.string.appName, R.string.appName) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                frag.notifyUtilUpdated();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                ListView drawerList = (ListView) findViewById(R.id.left_drawer);
                List<AndroidCalendar> selectedCalendars = new ArrayList<AndroidCalendar>(mCalendarUtils.getSelectedCalendars());

                for (AndroidCalendar cal : mAvailableCalendars) {
                    drawerList.setItemChecked(mAvailableCalendars.indexOf(cal), selectedCalendars.contains(cal));
                }

                super.onDrawerOpened(drawerView);
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        frag = (AgendaListFragment) getFragmentManager().findFragmentById(R.id.list_frag);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (frag != null) {
            frag.notifyUtilUpdated();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Gets called when the menu button is pressed.
     *
     * @param menu The menu instance that we apply a menu to
     * @return true so that it uses our own implementation
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appmenu, menu);
        return true;
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        if (!Util.isIntentAvailable(this, intent)) {
            menu.removeItem(R.id.newEventMenuItem);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();

        if (id == R.id.chooseSettings) {
            Intent temp = new Intent(Intent.ACTION_CHOOSER);
            temp.setClass(this, SettingsActivity.class);
            startActivity(temp);
        } else if (id == R.id.newEventMenuItem) {
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setType("vnd.android.cursor.item/event");

            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10000 && resultCode != RESULT_CANCELED) {
            frag.notifyUtilUpdated();
        }
    }
}
