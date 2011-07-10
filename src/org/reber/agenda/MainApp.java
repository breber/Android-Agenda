package org.reber.agenda;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Delegates the Activity based on our SDK version
 * 
 * @author breber
 */
public class MainApp extends Activity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (android.os.Build.VERSION.SDK_INT >= 11) {// Honeycomb
			startActivity(new Intent(this, HCAgendaActivity.class));
		} else {
			startActivity(new Intent(this, AgendaActivity.class));
		}
		
		finish();
	}
	
}
