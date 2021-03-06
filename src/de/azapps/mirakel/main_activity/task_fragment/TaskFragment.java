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
package de.azapps.mirakel.main_activity.task_fragment;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
//import android.support.v7.view.ActionMode;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import de.azapps.mirakel.custom_views.BaseTaskDetailRow.OnTaskChangedListner;
import de.azapps.mirakel.custom_views.TaskDetailContent.OnEditChanged;
import de.azapps.mirakel.custom_views.TaskDetailFilePart.OnFileClickListner;
import de.azapps.mirakel.custom_views.TaskDetailFilePart.OnFileMarkedListner;
import de.azapps.mirakel.custom_views.TaskDetailView;
import de.azapps.mirakel.custom_views.TaskSummary.OnTaskClickListner;
import de.azapps.mirakel.custom_views.TaskSummary.OnTaskMarkedListner;
import de.azapps.mirakel.helper.Helpers;
import de.azapps.mirakel.helper.Helpers.ExecInterfaceWithTask;
import de.azapps.mirakel.helper.MirakelPreferences;
import de.azapps.mirakel.helper.TaskDialogHelpers;
import de.azapps.mirakel.main_activity.MainActivity;
import de.azapps.mirakel.model.file.FileMirakel;
import de.azapps.mirakel.model.task.Task;
import de.azapps.mirakelandroid.R;
import de.azapps.tools.FileUtils;
import de.azapps.tools.Log;

public abstract class TaskFragment extends Fragment {
	protected enum ActionbarState {
		CONTENT, FILE, SUBTASK;
	}

	private static final String	TAG	= "TaskActivity";

	private ActionbarState		cabState;
	protected TaskDetailView	detailView;

	protected MainActivity		main;
	SparseArray<FileMirakel>	markedFiles;

	Map<Long, Task>				markedSubtasks;

	protected Menu				mMenu;

	protected Task				task;

	private Runnable			updateThread;

	abstract protected void changeVisiblity(boolean visible, MenuItem item);

	abstract public void closeActionMode();

	protected boolean handleActionBarClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.save:
				if (TaskFragment.this.detailView != null) {
					TaskFragment.this.detailView.saveContent();
				}
				break;
			case R.id.cancel:
				if (TaskFragment.this.detailView != null) {
					TaskFragment.this.detailView.cancelContent();
				}
				break;
			case R.id.menu_delete:
				if (TaskFragment.this.cabState == ActionbarState.FILE) {
					List<FileMirakel> selectedItems = new ArrayList<FileMirakel>();
					for (int i = 0; i < TaskFragment.this.markedFiles.size(); i++) {
						selectedItems.add(TaskFragment.this.markedFiles
								.valueAt(i));
					}
					TaskDialogHelpers.handleDeleteFile(selectedItems,
							getActivity(), TaskFragment.this.task,
							TaskFragment.this);
				} else {// Subtask

					List<Task> selectedItems = new ArrayList<Task>();

					for (Map.Entry<Long, Task> e : TaskFragment.this.markedSubtasks
							.entrySet()) {
						selectedItems.add(e.getValue());
					}
					TaskDialogHelpers.handleRemoveSubtask(selectedItems,
							getActivity(), TaskFragment.this,
							TaskFragment.this.task);
				}
				break;
			case R.id.edit_task:
				if (TaskFragment.this.main != null) {
					TaskFragment.this.main
					.setCurrentTask(TaskFragment.this.markedSubtasks
							.entrySet().iterator().next().getValue());
				}
				break;
			case R.id.done_task:
				for (Map.Entry<Long, Task> e : TaskFragment.this.markedSubtasks
						.entrySet()) {
					if (e.getValue() != null) {
						e.getValue().setDone(true);
						e.getValue().safeSave();
					}
				}
				update(TaskFragment.this.task);
				TaskFragment.this.main.getTasksFragment().updateList(false);
				TaskFragment.this.main.getListFragment().update();
				break;
			default:
				return false;
		}
		Log.i(TAG, "item clicked");
		closeActionMode();
		return true;

	}

	protected boolean handleCabCreateMenu(MenuInflater inflater, Menu menu) {
		if (TaskFragment.this.cabState == null) return false;
		switch (TaskFragment.this.cabState) {
			case CONTENT:
				inflater.inflate(R.menu.save, menu);
				break;
			case FILE:
				inflater.inflate(R.menu.context_file, menu);
				break;
			case SUBTASK:
				inflater.inflate(R.menu.context_subtask, menu);
				break;
			default:
				Log.d(TAG, "where are the dragons");
				return false;

		}
		return true;
	}

	protected void handleCloseCab() {
		TaskFragment.this.cabState = null;
		if (TaskFragment.this.detailView != null) {
			TaskFragment.this.detailView.unmark();
		}
		TaskFragment.this.markedFiles = new SparseArray<FileMirakel>();
		TaskFragment.this.markedSubtasks = new HashMap<Long, Task>();
		Log.d(TAG, "kill mode");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.cabState = null;
		this.markedFiles = new SparseArray<FileMirakel>();
		this.markedSubtasks = new HashMap<Long, Task>();
		this.main = (MainActivity) getActivity();
		View view;
		this.updateThread = new Runnable() {
			@Override
			public void run() {
				TaskFragment.this.detailView.update(TaskFragment.this.task);
			}
		};
		try {
			view = inflater.inflate(R.layout.task_fragment, container, false);
		} catch (Exception e) {
			Log.i(TAG, Log.getStackTraceString(e));
			return null;
		}
		this.detailView = (TaskDetailView) view.findViewById(R.id.taskFragment);
		this.detailView.setOnTaskChangedListner(new OnTaskChangedListner() {

			@Override
			public void onTaskChanged(Task newTask) {
				TaskFragment.this.main.getTasksFragment().updateList(false);
				TaskFragment.this.main.getListFragment().update();
			}
		});
		this.detailView.setOnSubtaskClick(new OnTaskClickListner() {

			@Override
			public void onTaskClick(Task t) {
				TaskFragment.this.main.setCurrentTask(t);
			}
		});

		if (MirakelPreferences.useBtnCamera()
				&& Helpers.isIntentAvailable(this.main,
						MediaStore.ACTION_IMAGE_CAPTURE)) {
			this.detailView.setAudioButtonClick(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TaskDialogHelpers.handleAudioRecord(getActivity(),
							TaskFragment.this.task,
							new ExecInterfaceWithTask() {
						@Override
						public void exec(Task t) {
							update(t);
						}
					});
				}
			});
			this.detailView.setCameraButtonClick(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						Intent cameraIntent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						Uri fileUri = FileUtils
								.getOutputMediaFileUri(FileUtils.MEDIA_TYPE_IMAGE);
						if (fileUri == null) return;
						TaskFragment.this.main.setFileUri(fileUri);
						cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
						getActivity().startActivityForResult(cameraIntent,
								MainActivity.RESULT_ADD_PICTURE);

					} catch (ActivityNotFoundException a) {
						Toast.makeText(
								TaskFragment.this.main,
								"Opps! Your device doesn't support taking photos",
								Toast.LENGTH_SHORT).show();
					}

				}
			});
		}

		if (this.task != null) {
			update(this.task);
		}

		this.detailView.setOnSubtaskMarked(new OnTaskMarkedListner() {

			@Override
			public void markTask(View v, Task t, boolean marked) {
				if (t == null || TaskFragment.this.cabState != null
						&& TaskFragment.this.cabState != ActionbarState.SUBTASK)
					return;
				if (marked) {
					TaskFragment.this.cabState = ActionbarState.SUBTASK;
					startCab();
					v.setBackgroundColor(Helpers
							.getHighlightedColor(getActivity()));
					TaskFragment.this.markedSubtasks.put(t.getId(), t);

				} else {
					Log.d(TAG, "not marked");
					v.setBackgroundColor(getActivity().getResources().getColor(
							android.R.color.transparent));
					TaskFragment.this.markedSubtasks.remove(t.getId());
					if (TaskFragment.this.markedSubtasks.isEmpty()) {
						closeActionMode();
					}
				}
				if (TaskFragment.this.mMenu != null) {
					MenuItem item = TaskFragment.this.mMenu
							.findItem(R.id.edit_task);
					changeVisiblity(
							TaskFragment.this.markedSubtasks.size() == 1, item);
				}
			}
		});

		this.detailView.setOnFileMarked(new OnFileMarkedListner() {

			@Override
			public void markFile(View v, FileMirakel e, boolean marked) {
				if (e == null || TaskFragment.this.cabState != null
						&& TaskFragment.this.cabState != ActionbarState.FILE)
					return;
				if (marked) {
					TaskFragment.this.cabState = ActionbarState.FILE;
					startCab();
					v.setBackgroundColor(Helpers
							.getHighlightedColor(getActivity()));
					TaskFragment.this.markedFiles.put(e.getId(), e);
				} else {
					v.setBackgroundColor(getActivity().getResources().getColor(
							android.R.color.transparent));
					TaskFragment.this.markedFiles.remove(e.getId());
					if (TaskFragment.this.markedFiles.size() == 0) {
						closeActionMode();
					}
				}
			}
		});

		this.detailView.setOnContentEdit(new OnEditChanged() {

			@Override
			public void handleCab(boolean startEdit) {
				if (startEdit) {
					Log.d(TAG, "start actionmode");
					TaskFragment.this.cabState = ActionbarState.CONTENT;
					startCab();
				} else {
					Log.d(TAG, "end actionmode");
					closeActionMode();;
				}

			}
		});
		this.detailView.setOnFileClicked(new OnFileClickListner() {

			@Override
			public void clickOnFile(final FileMirakel file) {
				final Context context = getActivity();
				String[] items;
				if (file.getPath().endsWith(".mp3")) {
					items = context.getResources().getStringArray(
							R.array.audio_playback_options);
				} else {
					TaskDialogHelpers.openFile(context, file);
					return;
				}
				new AlertDialog.Builder(context)
				.setTitle(R.string.audio_playback_select_title)
				.setItems(items, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0: // Open
								TaskDialogHelpers.openFile(context,
										file);
								break;
							default: // playback
								TaskDialogHelpers
								.playbackFile(getActivity(),
										file, which == 1);
								break;
						}
					}
				}).show();
				return;
			}
		});

		if (this.task == null) {
			this.main.setCurrentTask(this.main.getCurrentTask(), false);
		}
		return view;
	}

	abstract protected void startCab();

	public void update(final Task t) {
		this.task = t;
		if (this.detailView != null && this.updateThread != null) {
			new Thread(this.updateThread).start();
		}

	}

	public void updateLayout() {
		if (this.detailView != null) {
			this.detailView.updateLayout();
		}

	}

}
