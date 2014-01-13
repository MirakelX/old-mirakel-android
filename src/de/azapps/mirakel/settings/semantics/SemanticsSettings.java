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
package de.azapps.mirakel.settings.semantics;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import de.azapps.mirakel.helper.MirakelPreferences;
import de.azapps.mirakel.model.list.ListMirakel;
import de.azapps.mirakel.model.semantic.Semantic;
import de.azapps.mirakel.settings.ListSettings;
import de.azapps.mirakelandroid.R;
import de.azapps.widgets.DueDialog;
import de.azapps.widgets.DueDialog.VALUE;

public class SemanticsSettings implements OnPreferenceChangeListener {
	protected AlertDialog		alert;
	private final Context				ctx;
	private VALUE				dueDialogDayYear;

	private int					dueDialogValue;
	private final Semantic			semantic;
	private EditTextPreference	semanticsCondition;
	private Preference			semanticsDue;
	private ListPreference		semanticsList, semanticsPriority,
			semanticsWeekday;
	private final Object				settings;

	public SemanticsSettings(SemanticsSettingsFragment activity, Semantic semantic) {
		this.semantic = semantic;
		this.settings = activity;
		this.ctx = activity.getActivity();
	}

	private Preference findPreference(String key) {
		return ((SemanticsSettingsFragment) this.settings).findPreference(key);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object nv) {
		String newValue = String.valueOf(nv);
		String key = preference.getKey();
		if (key.equals("semantics_priority")) {
			if (newValue.equals("null")) {
				this.semantic.setPriority(null);
				this.semanticsPriority.setValueIndex(0);
				this.semanticsPriority.setSummary(this.semanticsPriority.getEntries()[0]);
			} else {
				this.semantic.setPriority(Integer.parseInt(newValue));
				this.semanticsPriority.setValue(newValue);
				this.semanticsPriority.setSummary(newValue);
			}
			this.semantic.save();
		} else if (key.equals("semantics_due")) {

		} else if (key.equals("semantics_weekday")) {
			Integer weekday = Integer.parseInt(newValue);
			if (weekday == 0) {
				weekday = null;
			}
			this.semantic.setWeekday(weekday);
			this.semanticsWeekday.setValue(newValue);
			this.semanticsWeekday.setSummary(this.semanticsWeekday.getEntry());
			this.semantic.save();
		} else if (key.equals("semantics_list")) {
			if (newValue.equals("null")) {
				this.semantic.setList(null);
				this.semanticsList.setValueIndex(0);
				this.semanticsList.setSummary(this.semanticsList.getEntries()[0]);
			} else {
				ListMirakel newList = ListMirakel.getList(Integer
						.parseInt(newValue));
				this.semantic.setList(newList);
				this.semanticsList.setValue(newValue);
				this.semanticsList.setSummary(newList.getName());
			}
			this.semantic.save();
		} else if (key.equals("semantics_condition")) {
			this.semantic.setCondition(newValue);
			this.semantic.save();
			this.semanticsCondition.setSummary(newValue);
			this.semanticsCondition.setText(newValue);
			if (MirakelPreferences.isTablet()) {
				((ListSettings) this.ctx).invalidateHeaders();
			}
		}
		return false;
	}

	public void setup() {
		this.semanticsCondition = (EditTextPreference) findPreference("semantics_condition");
		this.semanticsCondition.setOnPreferenceChangeListener(this);
		this.semanticsCondition.setText(this.semantic.getCondition());
		this.semanticsCondition.setSummary(this.semantic.getCondition());

		// Priority
		this.semanticsPriority = (ListPreference) findPreference("semantics_priority");
		this.semanticsPriority.setOnPreferenceChangeListener(this);
		this.semanticsPriority.setEntries(R.array.priority_entries);
		this.semanticsPriority.setEntryValues(R.array.priority_entry_values);
		if (this.semantic.getPriority() == null) {
			this.semanticsPriority.setValueIndex(0);
			this.semanticsPriority.setSummary(this.ctx.getResources().getStringArray(
					R.array.priority_entries)[0]);
		} else {
			this.semanticsPriority.setValue(this.semantic.getPriority().toString());
			this.semanticsPriority.setSummary(this.semanticsPriority.getValue());
		}

		// Due
		this.semanticsDue = findPreference("semantics_due");
		this.semanticsDue.setOnPreferenceChangeListener(this);

		this.semanticsDue.setSummary(updateDueStuff());
		this.semanticsDue
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						final DueDialog dueDialog = new DueDialog(SemanticsSettings.this.ctx, false);
						dueDialog.setTitle(SemanticsSettings.this.semanticsDue.getTitle());
						dueDialog.setValue(SemanticsSettings.this.dueDialogValue, SemanticsSettings.this.dueDialogDayYear);

						dueDialog.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {}
								});
						dueDialog.setNeutralButton(R.string.no_date,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										SemanticsSettings.this.semantic.setDue(null);
										SemanticsSettings.this.semanticsDue
												.setSummary(updateDueStuff());
										SemanticsSettings.this.semantic.save();
									}
								});
						dueDialog.setPositiveButton(android.R.string.ok,
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										int val = dueDialog.getValue();
										VALUE dayYear = dueDialog.getDayYear();
										switch (dayYear) {
											case DAY:
												SemanticsSettings.this.semantic.setDue(val);
												break;
											case MONTH:
												SemanticsSettings.this.semantic.setDue(val * 30);
												break;
											case YEAR:
												SemanticsSettings.this.semantic.setDue(val * 365);
												break;
											case HOUR:
											case MINUTE:
											default:
												// The other things aren't shown in
												// the dialog so we haven't to care
												// about them
												break;
										}
										SemanticsSettings.this.semanticsDue
												.setSummary(updateDueStuff());
										SemanticsSettings.this.semantic.save();
									}
								});

						dueDialog.show();
						return false;

					}
				});

		// Weekday
		Integer weekday = this.semantic.getWeekday();
		this.semanticsWeekday = (ListPreference) findPreference("semantics_weekday");
		this.semanticsWeekday.setOnPreferenceChangeListener(this);
		this.semanticsWeekday.setEntries(R.array.weekdays);
		CharSequence[] weekdaysNum = { "0", "1", "2", "3", "4", "5", "6", "7" };

		this.semanticsWeekday.setEntryValues(weekdaysNum);
		if (weekday == null) {
			this.semanticsWeekday.setValueIndex(0);
		} else {
			this.semanticsWeekday.setValueIndex(weekday);
		}
		this.semanticsWeekday.setSummary(this.semanticsWeekday.getEntry());

		// List
		this.semanticsList = (ListPreference) findPreference("semantics_list");
		this.semanticsList.setOnPreferenceChangeListener(this);

		List<ListMirakel> lists = ListMirakel.all(false);
		final CharSequence[] listEntries = new CharSequence[lists.size() + 1];
		final CharSequence[] listValues = new CharSequence[lists.size() + 1];
		listEntries[0] = this.ctx.getString(R.string.semantics_no_list);
		listValues[0] = "null";
		for (int i = 0; i < lists.size(); i++) {
			listValues[i + 1] = String.valueOf(lists.get(i).getId());
			listEntries[i + 1] = lists.get(i).getName();
		}
		this.semanticsList.setEntries(listEntries);
		this.semanticsList.setEntryValues(listValues);

		if (this.semantic.getList() == null) {
			this.semanticsList.setValueIndex(0);
			this.semanticsList.setSummary(this.ctx.getString(R.string.semantics_no_list));
		} else {
			this.semanticsList.setValue(String.valueOf(this.semantic.getList().getId()));
			this.semanticsList.setSummary(this.semantic.getList().getName());
		}
	}

	/**
	 * Updates the variables for the due Dialog and returns the summary for the
	 * Due-Preference
	 * 
	 * @param due
	 * @return
	 */
	private String updateDueStuff() {
		Integer due = this.semantic.getDue();
		String summary;
		if (due == null) {
			this.dueDialogDayYear = VALUE.DAY;
			this.dueDialogValue = 0;
			summary = this.ctx.getString(R.string.semantics_no_due);
		} else if (due % 365 == 0 && due != 0) {
			this.dueDialogValue = due / 365;
			this.dueDialogDayYear = VALUE.YEAR;
			summary = this.dueDialogValue
					+ " "
					+ this.ctx.getResources().getQuantityString(R.plurals.due_year,
							this.dueDialogValue);
		} else if (due % 30 == 0 && due != 0) {
			this.dueDialogValue = due / 30;
			this.dueDialogDayYear = VALUE.MONTH;
			summary = this.dueDialogValue
					+ " "
					+ this.ctx.getResources().getQuantityString(R.plurals.due_month,
							this.dueDialogValue);
		} else {
			this.dueDialogValue = due;
			this.dueDialogDayYear = VALUE.DAY;
			summary = this.dueDialogValue
					+ " "
					+ this.ctx.getResources().getQuantityString(R.plurals.due_day,
							this.dueDialogValue);
		}
		return summary;
	}

}
