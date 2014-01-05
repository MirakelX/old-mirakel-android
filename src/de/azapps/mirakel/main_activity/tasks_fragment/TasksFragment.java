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
package de.azapps.mirakel.main_activity.tasks_fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import de.azapps.mirakel.Mirakel.NoSuchListException;
import de.azapps.mirakel.helper.Helpers;
import de.azapps.mirakel.helper.Helpers.ExecInterface;
import de.azapps.mirakel.helper.Helpers.ExecInterfaceWithTask;
import de.azapps.mirakel.helper.Log;
import de.azapps.mirakel.helper.MirakelPreferences;
import de.azapps.mirakel.helper.TaskDialogHelpers;
import de.azapps.mirakel.main_activity.MainActivity;
import de.azapps.mirakel.main_activity.task_fragment.TaskFragment;
import de.azapps.mirakel.model.list.ListMirakel;
import de.azapps.mirakel.model.semantic.Semantic;
import de.azapps.mirakel.model.task.Task;
import de.azapps.mirakel.reminders.ReminderAlarm;
import de.azapps.mirakelandroid.R;
import de.azapps.tools.FileUtils;

public class TasksFragment extends Fragment {
	private static final String	TAG				= "TasksFragment";
	private TaskAdapter			adapter;
	private MainActivity		main;
	View						view;
	protected EditText			newTask;
	private boolean				created			= false;
	private boolean				finishLoad;
	private boolean				loadMore;
	private ListView			listView;
	private int					ItemCount;
	private List<Task>			values;
	private int					listId;
	private boolean				showDone		= true;
	private ActionMode			mActionMode		= null;
	final Handler				mHandler		= new Handler();

	final Runnable				mUpdateResults	= new Runnable() {
													public void run() {
														adapter.changeData(
																new ArrayList<Task>(
																		values.subList(
																				0,
																				ItemCount > values
																						.size() ? values
																						.size()
																						: ItemCount)),
																listId);
														adapter.notifyDataSetChanged();
													}
												};

	public void setActivity(MainActivity activity) {
		main = activity;
	}

	public void onResume() {
		super.onResume();
		showDone = MirakelPreferences.showDoneMain();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		finishLoad = false;
		loadMore = false;
		ItemCount = 0;
		main = (MainActivity) getActivity();
		showDone = MirakelPreferences.showDoneMain();
		listId = main.getCurrentList().getId();

		if (MirakelPreferences.isTablet()) {
			view = inflater.inflate(R.layout.tasks_fragment_tablet, container,
					false);
			TaskFragment t = new TaskFragment();
			getChildFragmentManager().beginTransaction()
					.replace(R.id.task_fragment_in_tasks, t).commit();
			main.setTaskFragment(t);
		} else {
			view = inflater.inflate(R.layout.tasks_fragment_phone, container,
					false);
		}
		// getResources().getString(R.string.action_settings);
		try {
			values = main.getCurrentList().tasks(showDone);
		} catch (NullPointerException e) {
			values = null;
		}
		adapter = null;
		created = true;

		listView = (ListView) view.findViewById(R.id.tasks_list);
		listView.setDescendantFocusability(ListView.FOCUS_AFTER_DESCENDANTS);
		// Events
		newTask = (EditText) view.findViewById(R.id.tasks_new);
		if (MirakelPreferences.isTablet()) {
			newTask.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
		}
		newTask.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND
						|| (actionId == EditorInfo.IME_NULL && event
								.getAction() == KeyEvent.ACTION_DOWN)) {
					newTask(v.getText().toString());
					v.setText(null);
				}
				return false;
			}
		});
		newTask.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void afterTextChanged(Editable s) {
				ImageButton send = (ImageButton) view
						.findViewById(R.id.btnEnter);
				if (s.length() > 0) send.setVisibility(View.VISIBLE);
				else send.setVisibility(View.GONE);

			}
		});
		ItemCount = 10;// TODO get this from somewhere
		update(true);
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView v, int scrollState) {
				// Nothing

			}

			@Override
			public void onScroll(AbsListView v, int firstVisibleItem, int visibleItemCount, final int totalItemCount) {
				int lastInScreen = firstVisibleItem + visibleItemCount;
				if ((lastInScreen == totalItemCount) && !(loadMore)
						&& finishLoad && adapter != null
						&& values.size() > totalItemCount) {
					ItemCount = totalItemCount + 2;
					update(false);
				}
			}
		});

		ImageButton btnEnter = (ImageButton) view.findViewById(R.id.btnEnter);
		btnEnter.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				newTask(newTask.getText().toString());
				newTask.setText(null);

			}
		});

		updateButtons();
		// Inflate the layout for this fragment
		return view;
	}

	public void focusNew(final boolean request_focus) {
		if (newTask == null) return;
		newTask.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(final View v, final boolean hasFocus) {
				if (main.getCurrentPosition() != MainActivity
						.getTasksFragmentPosition()) return;
				newTask.post(new Runnable() {
					@Override
					public void run() {
						Log.d(TAG, "focus new " + hasFocus);
						InputMethodManager imm = (InputMethodManager) getActivity()
								.getSystemService(Context.INPUT_METHOD_SERVICE);
						if (imm == null) {
							Log.w(TAG, "Inputmanager==null");
							return;
						}
						imm.restartInput(newTask);
						if (request_focus && hasFocus) {

							getActivity()
									.getWindow()
									.setSoftInputMode(
											WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
							imm.showSoftInput(newTask,
									InputMethodManager.SHOW_IMPLICIT);
							getActivity()
									.getWindow()
									.setSoftInputMode(
											WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
						} else if (!hasFocus) {
							newTask.requestFocus();
						} else if (!request_focus) {
							clearFocus();
						}
					}
				});
			}
		});
		newTask.requestFocus();
	}

	public void clearFocus() {
		if (newTask != null) {
			newTask.postDelayed(new Runnable() {

				@Override
				public void run() {
					Log.d(TAG, "clear focus");
					if (newTask == null) return;
					newTask.setOnFocusChangeListener(null);
					newTask.clearFocus();
					if (getActivity() == null) return;

					InputMethodManager imm = (InputMethodManager) getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(newTask.getWindowToken(), 0);
					getActivity().getWindow().setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

				}
			}, 10);
		}
	}

	private boolean newTask(String name) {
		newTask.setText(null);
		InputMethodManager imm = (InputMethodManager) main
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(newTask.getWindowToken(), 0);
		newTask.clearFocus();
		if (name.equals("")) {
			newTask.setOnFocusChangeListener(null);
			imm.showSoftInput(newTask, InputMethodManager.HIDE_IMPLICIT_ONLY);
			return true;
		}

		ListMirakel list = main.getCurrentList();
		Task task = Semantic.createTask(name, list,
				MirakelPreferences.useSemanticNewTask(), getActivity());

		adapter.addToHead(task);
		values.add(0, task);
		adapter.notifyDataSetChanged();

		main.getListFragment().update();
		if (!MirakelPreferences.hideKeyboard()) {
			focusNew(true);
		}
		main.updateShare();
		return true;
	}

	public void updateList() {
		updateList(true);
	}

	public void updateList(final boolean reset) {
		try {
			listId = main.getCurrentList().getId();
			values = main.getCurrentList().tasks(showDone);
			update(reset);
		} catch (NullPointerException e) {
			values = null;
		}
	}

	public void setTasks(List<Task> tasks) {
		values = tasks;
	}

	protected void update(boolean reset) {
		if (!created) return;
		if (values == null) {
			try {
				values = main.getCurrentList().tasks(showDone);
			} catch (NullPointerException w) {
				values = null;
				return;
			}
		}
		if (adapter != null && finishLoad) {
			mHandler.post(mUpdateResults);
			if (reset) setScrollPosition(0);
			return;
		}
		if (adapter != null) {
			adapter.resetSelected();
		}

		// main.showMessageFromSync();

		final ListView lv = (ListView) view.findViewById(R.id.tasks_list);
		AsyncTask<Void, Void, TaskAdapter> asyncTask = new AsyncTask<Void, Void, TaskAdapter>() {
			@Override
			protected TaskAdapter doInBackground(Void... params) {
				adapter = new TaskAdapter(main, R.layout.tasks_row,
						new ArrayList<Task>(values.subList(0,
								ItemCount > values.size() ? values.size()
										: ItemCount)), new OnClickListener() {
							@Override
							public void onClick(View v) {
								Task task = (Task) v.getTag();
								task.toggleDone();
								main.saveTask(task);
								ReminderAlarm.updateAlarms(getActivity());
							}
						}, new OnClickListener() {
							@Override
							public void onClick(final View v) {
								final Task task = (Task) v.getTag();
								TaskDialogHelpers.handlePriority(main, task,
										new ExecInterface() {

											@Override
											public void exec() {
												main.updatesForTask(task);
											}
										});

							}
						}, main.getCurrentList().getId());
				return adapter;
			}

			@Override
			protected void onPostExecute(TaskAdapter a) {
				lv.setAdapter(adapter);
				finishLoad = true;
			}
		};

		asyncTask.execute();
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		if (adapter != null) {
			adapter.resetSelected();
		}
		lv.setHapticFeedbackEnabled(true);
		lv.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				menu.findItem(R.id.edit_task).setVisible(
						adapter.getSelectedCount() <= 1);
				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				adapter.resetSelected();
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.context_tasks, menu);
				mActionMode = mode;
				clearFocus();
				return true;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				List<Task> tasks = adapter.getSelected();
				switch (item.getItemId()) {
					case R.id.menu_delete:
						main.handleDestroyTask(tasks);
						break;
					case R.id.menu_move:
						main.handleMoveTask(tasks);
						break;
					case R.id.edit_task:
						main.setCurrentTask(tasks.get(0), true);
						break;
					case R.id.done_task:
						for (Task t : tasks) {
							t.setDone(true);
							try {
								t.save();
							} catch (NoSuchListException e) {
								Log.d(TAG, "list did vanish");
							}
						}
						adapter.notifyDataSetChanged();
						break;
					default:
						break;
				}
				mode.finish();
				return false;
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
				Log.d(TAG, "item " + position + " selected");
				int oldCount = adapter.getSelectedCount();
				adapter.setSelected(position, checked);
				int newCount = adapter.getSelectedCount();
				Log.e(TAG, "old count: " + oldCount + " | newCount: "
						+ newCount);
				mode.setTitle(main.getResources().getQuantityString(
						R.plurals.selected_tasks, newCount, newCount));
				if ((oldCount < 2 && newCount >= 2)
						|| (oldCount >= 2 && newCount < 2)) {
					mode.invalidate();
				}

			}
		});

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
				// TODO Remove Bad Hack
				Task task = values.get((int) id);
				Log.v(TAG,
						"Switch to Task " + task.getId() + " ("
								+ task.getUUID() + ")");
				main.setCurrentTask(task, true);
			}
		});
	}

	public void closeActionMode() {
		if (mActionMode != null) mActionMode.finish();
	}

	public TaskAdapter getAdapter() {
		return adapter;
	}

	public ListView getListView() {
		return listView;
	}

	public void setScrollPosition(int pos) {
		if (listView == null) return;
		if (listView.getCount() > pos) listView.setSelectionFromTop(pos, 0);
		else listView.setSelectionFromTop(0, 0);
	}

	public void updateButtons() {
		// a) Android 2.3 dosen't support speech toText
		// b) The user can switch off the button
		if (view == null) return;
		if (!MirakelPreferences.useBtnSpeak()) {
			view.findViewById(R.id.btnSpeak_tasks).setVisibility(View.GONE);
		} else {
			ImageButton btnSpeak = (ImageButton) view
					.findViewById(R.id.btnSpeak_tasks);
			// txtText = newTask;
			btnSpeak.setVisibility(View.VISIBLE);
			btnSpeak.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					Intent intent = new Intent(
							RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
							main.getString(R.string.speak_lang_code));

					try {
						getActivity().startActivityForResult(intent,
								MainActivity.RESULT_SPEECH);
						newTask.setText("");
					} catch (ActivityNotFoundException a) {
						Toast t = Toast
								.makeText(
										main,
										"Opps! Your device doesn't support Speech to Text",
										Toast.LENGTH_SHORT);
						t.show();
					}
				}
			});
		}
		if (!MirakelPreferences.useBtnAudioRecord()) {
			view.findViewById(R.id.btnAudio_tasks).setVisibility(View.GONE);
		} else {
			ImageButton btnAudio = (ImageButton) view
					.findViewById(R.id.btnAudio_tasks);
			btnAudio.setVisibility(View.VISIBLE);
			btnAudio.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO BAHHHH this is ugly!
					final Task task = new Task("");
					task.setList(main.getCurrentList());
					task.setId(0);
					TaskDialogHelpers.handleAudioRecord(main, task,
							new ExecInterfaceWithTask() {
								@Override
								public void exec(Task t) {
									main.setCurrentList(t.getList());
									main.setCurrentTask(t, true);
								}
							});
				}
			});

		}
		if (!MirakelPreferences.useBtnCamera()
				|| !Helpers.isIntentAvailable(main,
						MediaStore.ACTION_IMAGE_CAPTURE)) {
			view.findViewById(R.id.btnCamera).setVisibility(View.GONE);
		} else {
			ImageButton btnCamera = (ImageButton) view
					.findViewById(R.id.btnCamera);
			btnCamera.setVisibility(View.VISIBLE);
			btnCamera.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						Intent cameraIntent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						Uri fileUri = FileUtils
								.getOutputMediaFileUri(FileUtils.MEDIA_TYPE_IMAGE);
						if (fileUri == null) return;
						main.setFileUri(fileUri);
						cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
						getActivity().startActivityForResult(cameraIntent,
								MainActivity.RESULT_CAMERA);

					} catch (ActivityNotFoundException a) {
						Toast.makeText(
								main,
								"Opps! Your device doesn't support taking photos",
								Toast.LENGTH_SHORT).show();
					}
				}
			});
		}
	}

	public View getFragmentView() {
		return view;
	}

}
