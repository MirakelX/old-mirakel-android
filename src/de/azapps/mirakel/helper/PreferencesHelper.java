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
package de.azapps.mirakel.helper;

import java.util.ArrayList;
import java.util.List;

import sheetrock.panda.changelog.ChangeLog;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.view.animation.DecelerateInterpolator;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;
import de.azapps.mirakel.Mirakel;
import de.azapps.mirakel.helper.export_import.AnyDoImport;
import de.azapps.mirakel.helper.export_import.ExportImport;
import de.azapps.mirakel.main_activity.MainActivity;
import de.azapps.mirakel.model.account.AccountMirakel;
import de.azapps.mirakel.model.list.ListMirakel;
import de.azapps.mirakel.model.list.SpecialList;
import de.azapps.mirakel.model.task.Task;
import de.azapps.mirakel.reminders.ReminderAlarm;
import de.azapps.mirakel.services.NotificationService;
import de.azapps.mirakel.settings.ColorPickerPref;
import de.azapps.mirakel.settings.recurring.RecurringActivity;
import de.azapps.mirakel.settings.semantics.SemanticsSettingsActivity;
import de.azapps.mirakel.settings.special_list.SpecialListsSettingsActivity;
import de.azapps.mirakel.static_activities.CreditsActivity;
import de.azapps.mirakel.static_activities.SettingsActivity;
import de.azapps.mirakel.static_activities.SettingsFragment;
import de.azapps.mirakel.sync.AuthenticatorActivity;
import de.azapps.mirakel.sync.SyncAdapter;
import de.azapps.mirakel.sync.mirakel.MirakelSync;
import de.azapps.mirakel.sync.taskwarrior.TaskWarriorSync;
import de.azapps.mirakel.widget.MainWidgetSettingsFragment;
import de.azapps.mirakelandroid.R;
import de.azapps.tools.Log;

@SuppressLint("SimpleDateFormat")
public class PreferencesHelper {

	static NumberPicker			numberPicker;
	private static final String	TAG	= "PreferencesHelper";
	public static void createAuthActivity(boolean newValue, final Object activity, final Object box, final boolean fragment) {
		final Context ctx;
		if (fragment) {
			ctx = ((Fragment) activity).getActivity();
		} else {
			ctx = (Activity) activity;
		}
		final AccountManager am = AccountManager.get(ctx);
		final Account[] accounts = am
				.getAccountsByType(AccountMirakel.ACCOUNT_TYPE_MIRAKEL);
		if (newValue) {
			new AlertDialog.Builder(ctx)
					.setTitle(R.string.sync_warning)
					.setMessage(R.string.sync_warning_message)
					.setPositiveButton(android.R.string.ok,
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									for (Account a : accounts) {
										try {
											am.removeAccount(a, null, null);
										} catch (Exception e) {
											Log.e(TAG, "Cannot remove Account");
										}
									}
									Intent intent = new Intent(ctx,
											AuthenticatorActivity.class);
									intent.setAction(MainActivity.SHOW_LISTS);
									if (fragment) {
										((Fragment) activity)
												.startActivityForResult(
														intent,
														SettingsActivity.NEW_ACCOUNT);
									} else {
										((Activity) activity)
												.startActivityForResult(
														intent,
														SettingsActivity.NEW_ACCOUNT);
									}
									SharedPreferences.Editor editor = MirakelPreferences
											.getEditor();
									editor.putBoolean("syncUse", true);
									editor.commit();
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									SharedPreferences.Editor editor = MirakelPreferences
											.getEditor();
									editor.putBoolean("syncUse", false);
									editor.commit();
									((Switch) box).setChecked(false);
								}
							}).show();
		} else {
			SharedPreferences.Editor editor = MirakelPreferences.getEditor();
			editor.putBoolean("syncUse", false);
			editor.commit();
			try {
				am.removeAccount(accounts[0], null, null);
			} catch (Exception e) {
				Log.e(TAG, "Cannot remove Account");
			}
		}
	}
	public static void updateSyncText(CheckBoxPreference sync, Preference server, Preference syncFrequency, Context ctx) {
		AccountManager am = AccountManager.get(ctx);
		Account[] accounts = am
				.getAccountsByType(AccountMirakel.ACCOUNT_TYPE_MIRAKEL);
		if (accounts.length > 0) {
			if (sync != null) {
				if (am.getUserData(accounts[0], SyncAdapter.BUNDLE_SERVER_TYPE)
						.equals(TaskWarriorSync.TYPE)) {
					sync.setSummary(ctx.getString(
							R.string.sync_use_summary_taskwarrior,
							accounts[0].name));
				} else if (am.getUserData(accounts[0],
						SyncAdapter.BUNDLE_SERVER_TYPE)
						.equals(MirakelSync.TYPE)) {
					sync.setSummary(ctx
							.getString(R.string.sync_use_summary_mirakel,
									accounts[0].name));
				} else {
					sync.setChecked(false);
					sync.setSummary(R.string.sync_use_summary_nothing);
					am.removeAccount(accounts[0], null, null);
				}
			}
			server.setSummary(ctx.getString(R.string.sync_server_summary,
					am.getUserData(accounts[0], SyncAdapter.BUNDLE_SERVER_URL)));
		} else {
			if (sync != null) {
				sync.setChecked(false);
				sync.setSummary(R.string.sync_use_summary_nothing);
			}
			server.setSummary("");
		}
		Editor editor = MirakelPreferences.getEditor();
		editor.putString("syncFrequency", "-1");
		editor.commit();
		if (syncFrequency != null) {
			syncFrequency.setSummary(ctx
					.getString(R.string.sync_frequency_summary_man));
		}
	}
	public Switch				actionBarSwitch;

	private final Activity		activity;

	private final Object		ctx;

	public PreferencesHelper(MainWidgetSettingsFragment c) {
		this.ctx = c;
		this.activity = c.getActivity();
	}

	public PreferencesHelper(SettingsFragment c) {
		this.ctx = c;
		this.activity = c.getActivity();
	}

	private Preference findPreference(String key) {
		return ((PreferenceFragment) this.ctx).findPreference(key);
	}

	private void removePreference(String which) {
		Preference pref = findPreference(which);
		if (pref != null) {
			((PreferenceFragment) this.ctx).getPreferenceScreen().removePreference(
					pref);
		}
	}

	@SuppressLint("NewApi")
	public void setFunctionsApp() {

		// Initialize needed Arrays
		final List<ListMirakel> lists = ListMirakel.all();
		CharSequence entryValues[] = new String[lists.size()];
		CharSequence entries[] = new String[lists.size()];
		CharSequence entryValuesWithDefault[] = new String[lists.size() + 1];
		CharSequence entriesWithDefault[] = new String[lists.size() + 1];
		int i = 0;
		for (ListMirakel list : lists) {
			String id = String.valueOf(list.getId());
			String name = list.getName();
			entryValues[i] = id;
			entries[i] = name;
			entryValuesWithDefault[i + 1] = id;
			entriesWithDefault[i + 1] = name;
			i++;
		}
		entriesWithDefault[0] = this.activity.getString(R.string.default_list);
		entryValuesWithDefault[0] = "default";

		// Load the preferences from an XML resource

		// Notifications List
		final ListPreference notificationsListPreference = (ListPreference) findPreference("notificationsList");
		if (notificationsListPreference != null) {
			final ListPreference notificationsListOpenPreference = (ListPreference) findPreference("notificationsListOpen");
			notificationsListPreference.setEntries(entries);
			notificationsListPreference.setEntryValues(entryValues);
			ListMirakel notificationsList = MirakelPreferences
					.getNotificationsList();

			notificationsListPreference.setSummary(this.activity.getString(
					R.string.notifications_list_summary,
					notificationsList.getName()));
			notificationsListPreference
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							String list = ListMirakel.getList(
									Integer.parseInt((String) newValue))
									.getName();
							notificationsListPreference.setSummary(PreferencesHelper.this.activity
									.getString(
											R.string.notifications_list_summary,
											list));
							if (MirakelPreferences
									.isNotificationListOpenDefault()) {
								notificationsListOpenPreference.setSummary(PreferencesHelper.this.activity
										.getString(
												R.string.notifications_list_summary,
												list));
							}
							return true;
						}
					});

			notificationsListOpenPreference.setEntries(entriesWithDefault);
			notificationsListOpenPreference
					.setEntryValues(entryValuesWithDefault);
			ListMirakel notificationsListOpen = MirakelPreferences
					.getNotificationsListOpen();
			notificationsListOpenPreference.setSummary(this.activity.getString(
					R.string.notifications_list_open_summary,
					notificationsListOpen.getName()));
			notificationsListOpenPreference
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							String list;
							if (!"default".equals(newValue.toString())) {
								list = ListMirakel.getList(
										Integer.parseInt((String) newValue))
										.getName();
							} else {
								list = MirakelPreferences
										.getNotificationsList().getName();
							}
							notificationsListOpenPreference.setSummary(PreferencesHelper.this.activity
									.getString(
											R.string.notifications_list_summary,
											list));
							return true;
						}
					});
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			removePreference("dashclock");
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			PreferenceCategory cat = (PreferenceCategory) findPreference("notifications");
			Preference notificationsBig = findPreference("notificationsBig");
			if (cat != null && notificationsBig != null) {
				cat.removePreference(notificationsBig);
			}
		}

		final CheckBoxPreference darkTheme = (CheckBoxPreference) findPreference("DarkTheme");
		if (darkTheme != null) {
			darkTheme
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							PreferencesHelper.this.activity.finish();
							PreferencesHelper.this.activity.startActivity(PreferencesHelper.this.activity.getIntent());

							return true;
						}
					});
		}

		// Startup
		final CheckBoxPreference startupAllListPreference = (CheckBoxPreference) findPreference("startupAllLists");
		if (startupAllListPreference != null) {
			final ListPreference startupListPreference = (ListPreference) findPreference("startupList");
			startupAllListPreference
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							if (!(Boolean) newValue) {
								startupListPreference.setSummary(PreferencesHelper.this.activity
										.getString(
												R.string.startup_list_summary,
												MirakelPreferences
														.getStartupList()
														.getName()));
								startupListPreference.setEnabled(true);
							} else {
								startupListPreference.setSummary(" ");
								startupListPreference.setEnabled(false);
							}
							return true;
						}
					});
			startupListPreference.setEntries(entries);
			startupListPreference.setEntryValues(entryValues);
			if (MirakelPreferences.isStartupAllLists()) {
				startupListPreference.setSummary(" ");
				startupListPreference.setEnabled(false);
			} else {
				ListMirakel startupList = MirakelPreferences.getStartupList();
				if (startupList == null) {
					startupList = SpecialList.firstSpecialSafe(this.activity);
				}
				startupListPreference.setSummary(this.activity.getString(
						R.string.startup_list_summary, startupList.getName()));
				startupListPreference.setEnabled(true);
			}
			startupListPreference
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							startupListPreference.setSummary(PreferencesHelper.this.activity
									.getString(
											R.string.startup_list_summary,
											ListMirakel.getList(Integer
													.parseInt((String) newValue))));
							return true;
						}
					});
		}

		final CheckBoxPreference notificationsUse = (CheckBoxPreference) findPreference("notificationsUse");
		if (notificationsUse != null) {
			notificationsUse
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							if ((Boolean) newValue) {
								PreferencesHelper.this.activity.startService(new Intent(PreferencesHelper.this.activity,
										NotificationService.class));
							} else {
								if (PreferencesHelper.this.activity.startService(new Intent(PreferencesHelper.this.activity,
										NotificationService.class)) != null) {
									PreferencesHelper.this.activity.stopService(new Intent(PreferencesHelper.this.activity,
											NotificationService.class));
								}
							}
							Editor e = preference.getEditor();
							e.putBoolean("notificationsUse", (Boolean) newValue);
							e.commit();
							NotificationService
									.updateNotificationAndWidget(PreferencesHelper.this.activity);
							return true;
						}
					});
		}
		String[] settings = { "notificationsPersistent",
				"notificationsZeroHide", "notificationsBig" };
		for (final String key : settings) {
			final CheckBoxPreference notifSetting = (CheckBoxPreference) findPreference(key);
			if (notifSetting != null) {
				notifSetting
						.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

							@Override
							public boolean onPreferenceChange(Preference preference, Object newValue) {
								Editor e = preference.getEditor();
								e.putBoolean(key, (Boolean) newValue);
								e.commit();
								NotificationService
										.updateNotificationAndWidget(PreferencesHelper.this.activity);
								return true;
							}
						});

			}
		}

		final CheckBoxPreference remindersPersistent = (CheckBoxPreference) findPreference("remindersPersistent");
		if (remindersPersistent != null) {
			remindersPersistent
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							Editor e = preference.getEditor();
							e.putBoolean("remindersPersistent",
									(Boolean) newValue);
							e.commit();
							ReminderAlarm.stopAll(PreferencesHelper.this.activity);
							ReminderAlarm.updateAlarms(PreferencesHelper.this.activity);
							return true;
						}
					});

		}

		Intent startSpecialListsIntent = new Intent(this.activity,
				SpecialListsSettingsActivity.class);
		Preference specialLists = findPreference("special_lists");
		if (specialLists != null) {
			specialLists.setIntent(startSpecialListsIntent);
		}

		Preference backup = findPreference("backup");
		if (backup != null) {

			backup.setSummary(this.activity.getString(R.string.backup_click_summary,
					ExportImport.getBackupDir().getAbsolutePath()));

			backup.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					ExportImport.exportDB(PreferencesHelper.this.activity);
					return true;
				}
			});
		}

		final ListPreference isTablet = (ListPreference) findPreference("useTabletLayoutNew");
		if (isTablet != null) {
			String[] values = { "0", "1", "2", "3" };
			final String[] e = this.activity.getResources().getStringArray(
					R.array.tablet_options);
			isTablet.setEntries(e);
			isTablet.setEntryValues(values);
			isTablet.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					int value = Integer.parseInt(newValue.toString());
					isTablet.setSummary(e[value]);
					return true;
				}
			});
		}

		Preference importDB = findPreference("import");
		if (importDB != null) {
			importDB.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Helpers.showFileChooser(SettingsActivity.FILE_IMPORT_DB,
							PreferencesHelper.this.activity.getString(R.string.import_title), PreferencesHelper.this.activity);
					return true;
				}
			});
		}

		Preference changelog = findPreference("changelog");
		if (changelog != null) {
			changelog
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference preference) {
							ChangeLog cl = new ChangeLog(PreferencesHelper.this.activity);
							cl.getFullLogDialog().show();
							return true;
						}
					});
		}
		Preference anyDo = findPreference("import_any_do");
		if (anyDo != null) {
			anyDo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					AnyDoImport.handleImportAnyDo(PreferencesHelper.this.activity);
					return true;
				}
			});
		}
		Preference importAstrid = findPreference("import_astrid");
		if (importAstrid != null) {
			importAstrid
					.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference preference) {
							Helpers.showFileChooser(
									SettingsActivity.FILE_ASTRID,
									PreferencesHelper.this.activity.getString(R.string.astrid_import_title),
									PreferencesHelper.this.activity);
							return true;
						}
					});
		}
		Preference importWunderlist = findPreference("import_wunderlist");
		if (importWunderlist != null) {
			importWunderlist
					.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference preference) {
							new AlertDialog.Builder(PreferencesHelper.this.activity)
									.setTitle(R.string.import_wunderlist_howto)
									.setMessage(
											R.string.import_wunderlist_howto_text)
									.setPositiveButton(android.R.string.ok,
											new OnClickListener() {

												@Override
												public void onClick(DialogInterface dialog, int which) {

													Helpers.showFileChooser(
															SettingsActivity.FILE_WUNDERLIST,
															PreferencesHelper.this.activity.getString(R.string.import_wunderlist_title),
															PreferencesHelper.this.activity);

												}
											}).show();
							return true;
						}
					});
		}
		final CheckBoxPreference killButton = (CheckBoxPreference) findPreference("KillButton");
		if (killButton != null) {
			killButton
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							if ((Boolean) newValue) {
								AlertDialog.Builder builder = new AlertDialog.Builder(
										PreferencesHelper.this.activity);
								builder.setTitle(R.string.kill_sure);
								builder.setMessage(R.string.kill_sure_message)
										.setPositiveButton(
												android.R.string.yes,
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {}
												})
										.setNegativeButton(
												android.R.string.cancel,
												new OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														((CheckBoxPreference) findPreference("KillButton"))
																.setChecked(false);

													}
												}).show();
							}
							return true;
						}
					});
		}
		final EditTextPreference importFileType = (EditTextPreference) findPreference("import_file_title");
		if (importFileType != null) {
			importFileType.setSummary(MirakelPreferences.getImportFileTitle());
		}
		final CheckBoxPreference importDefaultList = (CheckBoxPreference) findPreference("importDefaultList");
		if (importDefaultList != null) {
			ListMirakel list = MirakelPreferences.getImportDefaultList(false);
			if (list != null) {
				importDefaultList.setSummary(this.activity.getString(
						R.string.import_default_list_summary, list.getName()));
			} else {
				importDefaultList
						.setSummary(R.string.import_no_default_list_summary);
			}

			importDefaultList
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							if ((Boolean) newValue) {
								AlertDialog.Builder builder = new AlertDialog.Builder(
										PreferencesHelper.this.activity);
								builder.setTitle(R.string.import_to);
								final List<CharSequence> items = new ArrayList<CharSequence>();
								final List<Integer> list_ids = new ArrayList<Integer>();
								int currentItem = 0;
								for (ListMirakel list : ListMirakel.all()) {
									if (list.getId() > 0) {
										items.add(list.getName());
										list_ids.add(list.getId());
									}

								}
								builder.setSingleChoiceItems(
										items.toArray(new CharSequence[items
												.size()]), currentItem,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int item) {
												importDefaultList.setSummary(PreferencesHelper.this.activity
														.getString(
																R.string.import_default_list_summary,
																items.get(item)));
												SharedPreferences.Editor editor = MirakelPreferences
														.getEditor();
												editor.putInt(
														"defaultImportList",
														list_ids.get(item));
												editor.commit();
												dialog.dismiss();
											}
										});
								builder.setOnCancelListener(new OnCancelListener() {
									@Override
									public void onCancel(DialogInterface dialog) {
										importDefaultList.setChecked(false);
										importDefaultList
												.setSummary(R.string.import_no_default_list_summary);
									}
								});
								builder.create().show();
							} else {
								importDefaultList
										.setSummary(R.string.import_no_default_list_summary);
							}
							return true;
						}
					});
		}

		// Delete done tasks
		Preference deleteDone = findPreference("deleteDone");
		if (deleteDone != null) {
			deleteDone
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {

						@Override
						public boolean onPreferenceClick(Preference preference) {
							new AlertDialog.Builder(PreferencesHelper.this.activity)
									.setTitle(R.string.delete_done_warning)
									.setMessage(
											R.string.delete_done_warning_message)
									.setPositiveButton(android.R.string.ok,
											new OnClickListener() {
												@Override
												public void onClick(DialogInterface dialogInterface, int i) {
													Task.deleteDoneTasks();
													Toast.makeText(
															PreferencesHelper.this.activity,
															R.string.delete_done_success,
															Toast.LENGTH_SHORT)
															.show();
													android.os.Process
															.killProcess(android.os.Process
																	.myPid());
												}
											})
									.setNegativeButton(android.R.string.cancel,
											new OnClickListener() {
												@Override
												public void onClick(DialogInterface dialogInterface, int i) {}
											}).show();
							return true;
						}
					});
		}
		final Preference autoBackupIntervall = findPreference("autoBackupIntervall");
		if (autoBackupIntervall != null) {
			autoBackupIntervall.setSummary(this.activity.getString(
					R.string.auto_backup_intervall_summary,
					MirakelPreferences.getAutoBackupIntervall()));
			autoBackupIntervall
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {

						@Override
						public boolean onPreferenceClick(Preference preference) {

							final int old_val = MirakelPreferences
									.getAutoBackupIntervall();
							final int max = 31;
							final int min = 0;
							numberPicker = new NumberPicker(PreferencesHelper.this.activity);
							numberPicker.setMaxValue(max);
							numberPicker.setMinValue(min);
							numberPicker.setWrapSelectorWheel(false);
							numberPicker.setValue(old_val);
							numberPicker
									.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
							new AlertDialog.Builder(PreferencesHelper.this.activity)
									.setTitle(R.string.auto_backup_intervall)
									.setView(numberPicker)
									.setPositiveButton(
											android.R.string.ok,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int whichButton) {
													int val = numberPicker
															.getValue();
													MirakelPreferences
															.setAutoBackupIntervall(val);
													autoBackupIntervall
															.setSummary(PreferencesHelper.this.activity
																	.getString(
																			R.string.auto_backup_intervall_summary,
																			val));
												}
											})
									.setNegativeButton(
											android.R.string.cancel,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int whichButton) {
													// Do nothing.
												}
											}).show();
							return false;
						}
					});
		}
		final Preference undoNumber = findPreference("UndoNumber");
		if (undoNumber != null) {
			undoNumber.setSummary(this.activity.getString(
					R.string.undo_number_summary,
					MirakelPreferences.getUndoNumber()));
			undoNumber
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {

						@Override
						public boolean onPreferenceClick(Preference preference) {
							final int old_val = MirakelPreferences
									.getUndoNumber();
							final int max = 25;
							final int min = 1;
							numberPicker = new NumberPicker(PreferencesHelper.this.activity);
							numberPicker.setMaxValue(max);
							numberPicker.setMinValue(min);
							numberPicker.setWrapSelectorWheel(false);
							numberPicker.setValue(old_val);
							numberPicker
									.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
							new AlertDialog.Builder(PreferencesHelper.this.activity)
									.setTitle(R.string.undo_number)
									.setMessage(
											PreferencesHelper.this.activity.getString(
													R.string.undo_number_summary,
													MirakelPreferences
															.getUndoNumber()))
									.setView(numberPicker)
									.setPositiveButton(
											android.R.string.ok,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int whichButton) {
													SharedPreferences.Editor editor = MirakelPreferences
															.getEditor();
													int val = numberPicker
															.getValue();
													editor.putInt("UndoNumber",
															val);
													undoNumber
															.setSummary(PreferencesHelper.this.activity
																	.getString(
																			R.string.undo_number_summary,
																			val));
													if (old_val > val) {
														for (int i = val; i < max; i++) {
															editor.putString(
																	UndoHistory.UNDO
																			+ i,
																	"");
														}
													}
													editor.commit();
												}
											})
									.setNegativeButton(
											android.R.string.cancel,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int whichButton) {
													// Do nothing.
												}
											}).show();
							return true;
						}
					});
		}

		Preference dashclock = findPreference("dashclock");
		if (dashclock != null) {
			Intent startdashclockIntent = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("http://play.google.com/store/apps/details?id=de.azapps.mirakel.dashclock"));
			dashclock.setIntent(startdashclockIntent);
		}
		Preference credits = findPreference("credits");
		if (credits != null) {
			Intent startCreditsIntent = new Intent(this.activity,
					CreditsActivity.class);
			credits.setIntent(startCreditsIntent);
		}

		Preference contact = findPreference("contact");
		if (contact != null) {
			contact.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					Helpers.contact(PreferencesHelper.this.activity);
					return false;
				}
			});
		}

		Intent startSemanticsIntent = new Intent(this.activity,
				SemanticsSettingsActivity.class);
		Preference semantics = findPreference("semanticNewTaskSettings");
		if (semantics != null) {
			semantics.setIntent(startSemanticsIntent);
		}

		Intent startRecurringIntent = new Intent(this.activity,
				RecurringActivity.class);
		Preference recurring = findPreference("recurring");
		if (recurring != null) {
			recurring.setIntent(startRecurringIntent);
		}
		// Intent startTaskFragmentIntent = new Intent(activity,
		// TaskFragmentSettings.class);
		Preference taskFragment = findPreference("task_fragment");
		if (taskFragment != null) {
			// taskFragment.setIntent(startTaskFragmentIntent);
			taskFragment
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {

						@Override
						public boolean onPreferenceClick(Preference preference) {
							((SettingsFragment) PreferencesHelper.this.ctx).showTaskFragmentSettings();
							return false;
						}
					});
		}

		final CheckBoxPreference subTaskAddToSameList = (CheckBoxPreference) findPreference("subtaskAddToSameList");
		if (subTaskAddToSameList != null) {
			if (!MirakelPreferences.addSubtaskToSameList()) {
				subTaskAddToSameList.setSummary(this.activity.getString(
						R.string.settings_subtask_add_to_list_summary,
						MirakelPreferences.subtaskAddToList().getName()));
			} else {
				subTaskAddToSameList
						.setSummary(R.string.settings_subtask_add_to_same_list_summary);
			}

			subTaskAddToSameList
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							if (!(Boolean) newValue) {
								AlertDialog.Builder builder = new AlertDialog.Builder(
										PreferencesHelper.this.activity);
								builder.setTitle(R.string.import_to);
								final List<CharSequence> items = new ArrayList<CharSequence>();
								final List<Integer> list_ids = new ArrayList<Integer>();
								int currentItem = 0;
								for (ListMirakel list : ListMirakel.all()) {
									if (list.getId() > 0) {
										items.add(list.getName());
										list_ids.add(list.getId());
									}

								}
								builder.setSingleChoiceItems(
										items.toArray(new CharSequence[items
												.size()]), currentItem,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int item) {
												subTaskAddToSameList
														.setSummary(items
																.get(item));
												subTaskAddToSameList.setSummary(PreferencesHelper.this.activity
														.getString(
																R.string.settings_subtask_add_to_list_summary,
																items.get(item)));
												SharedPreferences.Editor editor = MirakelPreferences
														.getEditor();
												editor.putInt(
														"subtaskAddToList",
														list_ids.get(item));
												editor.commit();
												dialog.dismiss();
											}
										});
								builder.setOnCancelListener(new OnCancelListener() {
									@Override
									public void onCancel(DialogInterface dialog) {
										subTaskAddToSameList.setChecked(false);
										subTaskAddToSameList
												.setSummary(R.string.settings_subtask_add_to_same_list_summary);
									}
								});
								builder.create().show();
							} else {
								subTaskAddToSameList
										.setSummary(R.string.settings_subtask_add_to_same_list_summary);
							}
							return true;
						}
					});
		}
		final ListPreference language = (ListPreference) findPreference("language");
		if (language != null) {
			setLanguageSummary(language, MirakelPreferences.getLanguage());
			language.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					setLanguageSummary(language, newValue.toString());
					MirakelPreferences.getEditor()
							.putString("language", newValue.toString())
							.commit();
					Helpers.restartApp(PreferencesHelper.this.activity);
					return false;
				}
			});

		}

		final CheckBoxPreference useTabletLayout = (CheckBoxPreference) findPreference("useTabletLayout");
		if (useTabletLayout != null) {
			useTabletLayout
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							MirakelPreferences
									.getEditor()
									.putBoolean("useTabletLayout",
											(Boolean) newValue).commit();
							Helpers.restartApp(PreferencesHelper.this.activity);
							return false;
						}
					});
		}

		final Preference version = findPreference("version");
		if (version != null) {
			version.setSummary(Mirakel.VERSIONS_NAME);
		}
	}

	@SuppressLint("NewApi")
	public void setFunctionsWidget(final Context context, final int widgetId) {
		List<ListMirakel> lists = ListMirakel.all();
		CharSequence entryValues[] = new String[lists.size()];
		CharSequence entries[] = new String[lists.size()];
		int i = 0;
		for (ListMirakel list : lists) {
			entryValues[i] = String.valueOf(list.getId());
			entries[i] = list.getName();
			i++;
		}

		ListMirakel list = WidgetHelper.getList(context, widgetId);
		if (list == null) return;

		final CheckBoxPreference isDark = (CheckBoxPreference) findPreference("isDark");
		final ListPreference widgetListPreference = (ListPreference) findPreference("widgetList");
		widgetListPreference.setEntries(entries);
		widgetListPreference.setEntryValues(entryValues);
		widgetListPreference.setSummary(this.activity.getString(
				R.string.widget_list_summary, list));
		widgetListPreference.setValue(String.valueOf(list.getId()));
		widgetListPreference
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						WidgetHelper.setList(context, widgetId,
								Integer.parseInt((String) newValue));
						String list = ListMirakel.getList(
								Integer.parseInt((String) newValue)).getName();
						widgetListPreference.setSummary(PreferencesHelper.this.activity.getString(
								R.string.notifications_list_summary, list));
						return true;
					}
				});

		isDark.setChecked(WidgetHelper.isDark(context, widgetId));
		isDark.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				WidgetHelper.setDark(context, widgetId, (Boolean) newValue);
				return true;
			}
		});
//		CheckBoxPreference isMinimalistic = (CheckBoxPreference) findPreference("isMinimalistic");
//		if (isMinimalistic != null) {
//			isMinimalistic.setChecked(WidgetHelper.isMinimalistic(context,
//					widgetId));
//			isMinimalistic
//					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//
//						@Override
//						public boolean onPreferenceChange(Preference preference, Object newValue) {
//							WidgetHelper.setMinimalistic(context, widgetId,
//									(Boolean) newValue);
//							return true;
//						}
//					});
//		}

		CheckBoxPreference showDone = (CheckBoxPreference) findPreference("showDone");
		showDone.setChecked(WidgetHelper.showDone(context, widgetId));
		showDone.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				WidgetHelper.setDone(context, widgetId, (Boolean) newValue);
				return true;
			}
		});

		final CheckBoxPreference dueColors = (CheckBoxPreference) findPreference("widgetDueColors");
		dueColors.setChecked(WidgetHelper.dueColors(context, widgetId));
		dueColors
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						WidgetHelper.setDueColors(context, widgetId,
								(Boolean) newValue);
						return true;
					}
				});

		final Preference widgetTransparency = findPreference("widgetTransparency");
		final ColorPickerPref widgetFontColor = (ColorPickerPref) findPreference("widgetFontColor");
		// ((SettingsFragment)ctx).getActivity().findViewById(R.id.color_box).setBackgroundColor(WidgetHelper.getFontColor(context,
		// widgetId));
		widgetFontColor.setColor(WidgetHelper.getFontColor(context, widgetId));
		widgetFontColor.setOldColor(WidgetHelper
				.getFontColor(context, widgetId));
		widgetFontColor
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						WidgetHelper.setFontColor(context, widgetId,
								widgetFontColor.getColor());
						return false;
					}
				});
		widgetTransparency.setSummary(this.activity.getString(
				R.string.widget_transparency_summary,
				100 - Math.round(WidgetHelper.getTransparency(context,
						widgetId) / 255f * 1000) / 10));
		widgetTransparency
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						final SeekBar sb = new SeekBar(context);
						sb.setMax(255);
						sb.setInterpolator(new DecelerateInterpolator());
						sb.setProgress(255 - WidgetHelper.getTransparency(
								context, widgetId));
						sb.setPadding(20, 30, 20, 30);
						new AlertDialog.Builder(context)
								.setTitle(R.string.widget_transparency)
								.setView(sb)
								.setPositiveButton(android.R.string.ok,
										new OnClickListener() {

											@Override
											public void onClick(DialogInterface dialog, int which) {
												// Fix Direction
												WidgetHelper.setTransparency(
														context, widgetId,
														255 - sb.getProgress());
												float t = 100 - Math.round(WidgetHelper
														.getTransparency(
																context,
																widgetId) / 255f * 1000) / 10;
												widgetTransparency.setSummary(PreferencesHelper.this.activity
														.getString(
																R.string.widget_transparency_summary,
																t));

											}
										})
								.setNegativeButton(android.R.string.cancel,
										null).show();
						return false;
					}
				});

	}

	private void setLanguageSummary(ListPreference language, String current) {
		String[] keys = this.activity.getResources().getStringArray(
				R.array.language_keys);
		language.setSummary(keys[0]);
		for (int j = 0; j < keys.length; j++) {
			if (current.equals(keys[j])) {
				language.setSummary(this.activity.getResources().getStringArray(
						R.array.language_values)[j]);
				break;
			}
		}
	}

	// The „real“ helper functions

}
