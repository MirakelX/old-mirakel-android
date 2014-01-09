/*******************************************************************************
 * Mirakel is an Android App for managing your ToDo-Lists
 * 
 * Copyright (c) 2013-2014 Anatolij Zelenin, Georg Semmler.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.azapps.mirakel.widget;

import java.util.Locale;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.FrameLayout;
import de.azapps.mirakel.helper.Helpers;
import de.azapps.mirakel.helper.MirakelPreferences;
import de.azapps.mirakelandroid.R;
import de.azapps.tools.Log;

public class MainWidgetSettingsActivity extends PreferenceActivity {
	private static int			mAppWidgetId	= AppWidgetManager.INVALID_APPWIDGET_ID;

	@SuppressWarnings("unused")
	private static final String	TAG				= "MainWidgetSettingsActivity";

	@Override
	protected boolean isValidFragment(String fragmentName) {

		return fragmentName.equals(MainWidgetSettingsFragment.class.getCanonicalName());
	}

	@Override
	public void onBackPressed() {
		/*
		 * Show Homescreen
		 */
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		Locale.setDefault(Helpers.getLocal(this));
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (MirakelPreferences.isDark()) {
			setTheme(R.style.AppBaseThemeDARK);
		}
		super.onCreate(savedInstanceState);
		mAppWidgetId = getIntent().getIntExtra(
				MainWidgetProvider.EXTRA_WIDGET_ID, 0);
		// Display the fragment as the main content.
		((FrameLayout) findViewById(android.R.id.content)).removeAllViews();
		MainWidgetSettingsFragment fragment = new MainWidgetSettingsFragment();
		getFragmentManager().beginTransaction()
		.replace(android.R.id.content, fragment).commit();
		fragment.setup(mAppWidgetId);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.e("WIDGET", "updated");
		Intent intent = new Intent(this, MainWidgetProvider.class);
		intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
		// Use an array and EXTRA_APPWIDGET_IDS instead of
		// AppWidgetManager.EXTRA_APPWIDGET_ID,
		// since it seems the onUpdate() is only fired on that:
		int widgets[] = { mAppWidgetId };
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgets);
		AppWidgetManager.getInstance(this).notifyAppWidgetViewDataChanged(
				mAppWidgetId, R.id.widget_tasks_list);
		sendBroadcast(intent);
		// Finish this activity
		finish();
	}

}
