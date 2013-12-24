/*******************************************************************************
 * Mirakel is an Android App for managing your ToDo-Lists Copyright (c) 2013 Anatolij Zelenin, Georg
 * Semmler. This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or any later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have
 * received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.azapps.mirakel.static_activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.Toast;
import de.azapps.mirakel.adapter.SettingsAdapter;
import de.azapps.mirakel.helper.Log;
import de.azapps.mirakel.helper.MirakelPreferences;
import de.azapps.mirakel.helper.export_import.AnyDoImport;
import de.azapps.mirakel.helper.export_import.ExportImport;
import de.azapps.mirakel.helper.export_import.WunderlistImport;
import de.azapps.mirakel.settings.taskfragment.TaskFragmentSettingsFragment;
import de.azapps.mirakelandroid.R;
import de.azapps.tools.FileUtils;

public class SettingsActivity extends PreferenceActivity {

	public static final int		FILE_ASTRID	= 0, FILE_IMPORT_DB = 1,
			NEW_ACCOUNT = 2, FILE_ANY_DO = 3, FILE_WUNDERLIST = 4;
	private static final String	TAG			= "SettingsActivity";
	public static final int		DONATE		= 5;
	private List<Header>		mHeaders;
	private boolean				darkTheme;
	private SettingsAdapter		mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		darkTheme = MirakelPreferences.isDark();
		if (darkTheme) setTheme(R.style.AppBaseThemeDARK);
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "Menu");
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (darkTheme != MirakelPreferences.isDark()) {
			finish();
			startActivity(getIntent());
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, int resultCode, final Intent data) {
		Log.d(TAG, "activity");
		final Context that = this;
		switch (requestCode) {
			case FILE_IMPORT_DB:
				if (resultCode != RESULT_OK) return;
				final String path_db = FileUtils.getPathFromUri(data.getData(),
						this);
				// Check if this is an database file
				if (path_db != null && !path_db.endsWith(".db")) {
					Toast.makeText(that, R.string.import_wrong_type,
							Toast.LENGTH_LONG).show();
					return;
				}
				new AlertDialog.Builder(this)
						.setTitle(R.string.import_sure)
						.setMessage(
								this.getString(R.string.import_sure_summary,
										path_db))
						.setNegativeButton(android.R.string.cancel,
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {

									}
								})
						.setPositiveButton(android.R.string.yes,
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										if (path_db != null) {
											ExportImport.importDB(that,
													new File(path_db));
										} else {
											try {
												ExportImport
														.importDB(
																that,
																(FileInputStream) getContentResolver()
																		.openInputStream(
																				data.getData()));
											} catch (FileNotFoundException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									}
								}).create().show();
				break;
			case NEW_ACCOUNT:
				break;
			case FILE_ASTRID:
			case FILE_ANY_DO:
			case FILE_WUNDERLIST:
				if (resultCode != RESULT_OK) return;
				final String file_path = FileUtils.getPathFromUri(
						data.getData(), this);

				// Do the import in a background-task
				new AsyncTask<String, Void, Boolean>() {
					ProgressDialog	dialog;

					@Override
					protected Boolean doInBackground(String... params) {
						switch (requestCode) {
							case FILE_ASTRID:
								return ExportImport.importAstrid(that,
										file_path);
							case FILE_ANY_DO:
								return AnyDoImport.exec(that, file_path);
							case FILE_WUNDERLIST:
								return WunderlistImport.exec(that, file_path);
							default:
								return false;
						}

					}

					@Override
					protected void onPostExecute(Boolean success) {
						dialog.dismiss();
						if (!success) {
							Toast.makeText(that, R.string.astrid_unsuccess,
									Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(that, R.string.astrid_success,
									Toast.LENGTH_SHORT).show();
							android.os.Process.killProcess(android.os.Process
									.myPid()); // ugly
												// but
												// simple
						}
					}

					@Override
					protected void onPreExecute() {
						dialog = ProgressDialog.show(that,
								that.getString(R.string.importing),
								that.getString(R.string.wait), true);
					}
				}.execute("");
				break;
			case DONATE:
				if (resultCode != RESULT_OK) return;
				if (!onIsMultiPane()) finish();

			default:
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.settings, target);
		mHeaders = target;
	}

	@Override
	public boolean onIsMultiPane() {
		return MirakelPreferences.isTablet();
	}

	@Override
	public void setListAdapter(ListAdapter adapter) {
		if (mHeaders == null) {
			mHeaders = new ArrayList<Header>();
			// When the saved state provides the list of headers,
			// onBuildHeaders is not called
			// so we build it from the adapter given, then use our own adapter
			for (int i = 0; i < adapter.getCount(); ++i)
				mHeaders.add((Header) adapter.getItem(i));
		}
		mAdapter = new SettingsAdapter(this, mHeaders);
		super.setListAdapter(mAdapter);
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		return fragmentName.equals(SettingsFragment.class.getCanonicalName())
				|| fragmentName.equals(TaskFragmentSettingsFragment.class
						.getCanonicalName());
	}

}
