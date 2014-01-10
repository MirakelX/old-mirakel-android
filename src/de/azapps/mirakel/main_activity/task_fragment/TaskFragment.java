/*******************************************************************************
 * Mirakel is an Android App for managing your ToDo-Lists Copyright (c) 2013-2014 Anatolij Zelenin, Georg
 * Semmler. This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or any later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have
 * received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.azapps.mirakel.main_activity.task_fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.azapps.mirakel.custom_views.BaseTaskDetailRow.OnTaskChangedListner;
import de.azapps.mirakel.custom_views.TaskDetailView;
import de.azapps.mirakel.model.task.Task;
import de.azapps.mirakelandroid.R;
import de.azapps.tools.Log;

public class TaskFragment extends Fragment {
	private static final String	TAG			= "TaskActivity";

	// private final ActionMode mActionMode = null;

	private TaskDetailView		detailView;

	private Task				task;







	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.w(TAG, "taskfragment");
		getActivity();
		View view;
		try {
			view = inflater.inflate(R.layout.task_fragment, container, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.i(TAG, Log.getStackTraceString(e));
			return null;
		}
		this.detailView = (TaskDetailView) view
				.findViewById(R.id.taskFragment);
		this.detailView.setOnTaskChangedListner(new OnTaskChangedListner() {

			@Override
			public void onTaskChanged(Task newTask) {
				// TODO Auto-generated method stub
				Log.w(TAG, "implement this");

			}
		});

		//		ListView listView = (ListView) view.findViewById(R.id.taskFragment);
		//		this.adapter = new TaskFragmentAdapter(main, R.layout.task_head_line,
		//				main.getCurrentTask());
		//		listView.setAdapter(this.adapter);
		//		listView.setItemsCanFocus(true);
		//		listView.setOnItemClickListener(new OnItemClickListener() {
		//			@Override
		//			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		//				int type = TaskFragment.this.adapter.getData().get(position).first;
		//				if (type == TYPE.FILE) {
		//					if (main.getCurrentTask() == null) return;
		//					FileMirakel file = main.getCurrentTask().getFiles()
		//							.get(TaskFragment.this.adapter.getData().get(position).second);
		//					if (file.getPath().endsWith(".mp3")) {
		//						TaskDialogHelpers.handleAudioPlayback(main, file);
		//						return;
		//					}
		//					TaskDialogHelpers.openFile(main, file);
		//				} else if (type == TYPE.SUBTASK) {
		//					Task t = TaskFragment.this.adapter.getTask().getSubtasks()
		//							.get(TaskFragment.this.adapter.getData().get(position).second);
		//					main.setGoBackTo(TaskFragment.this.adapter.getTask());
		//					if (t.getList().getId() != main.getCurrentList().getId()) {
		//						main.setSkipSwipe();
		//						main.setCurrentList(t.getList(), null, false, false);
		//					}
		//					main.setCurrentTask(t, false, false);
		//				}
		//
		//			}
		//		});
		//
		//		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		//		if (this.adapter != null) {
		//			this.adapter.resetSelected();
		//		}
		//		listView.setHapticFeedbackEnabled(true);
		//		listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
		//
		//			@Override
		//			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		//
		//				switch (item.getItemId()) {
		//					case R.id.menu_delete:
		//						List<Pair<Integer, Integer>> selected = TaskFragment.this.adapter
		//						.getSelected();
		//						if (TaskFragment.this.adapter.getSelectedCount() > 0
		//								&& TaskFragment.this.adapter.getSelected().get(0).first == TYPE.FILE) {
		//							List<FileMirakel> files = TaskFragment.this.adapter.getTask()
		//									.getFiles();
		//							List<FileMirakel> selectedItems = new ArrayList<FileMirakel>();
		//							for (Pair<Integer, Integer> p : selected) {
		//								if (p.first == TYPE.FILE) {
		//									selectedItems.add(files.get(p.second));
		//								}
		//							}
		//							TaskDialogHelpers.handleDeleteFile(selectedItems,
		//									main, TaskFragment.this.adapter.getTask(), TaskFragment.this.adapter);
		//							break;
		//						} else if (TaskFragment.this.adapter.getSelectedCount() > 0
		//								&& TaskFragment.this.adapter.getSelected().get(0).first == TYPE.SUBTASK) {
		//							List<Task> subtasks = TaskFragment.this.adapter.getTask()
		//									.getSubtasks();
		//							List<Task> selectedItems = new ArrayList<Task>();
		//							for (Pair<Integer, Integer> p : selected) {
		//								if (p.first == TYPE.SUBTASK) {
		//									selectedItems.add(subtasks.get(p.second));
		//								}
		//							}
		//							TaskDialogHelpers.handleRemoveSubtask(
		//									selectedItems, main, TaskFragment.this.adapter,
		//									TaskFragment.this.adapter.getTask());
		//						} else {
		//							Log.e(TAG, "How did you get selected this?");
		//						}
		//						break;
		//					case R.id.edit_task:
		//						if (TaskFragment.this.adapter.getSelectedCount() == 1) {
		//							TaskFragment.this.adapter.setData(TaskFragment.this.adapter.getTask().getSubtasks()
		//									.get(TaskFragment.this.adapter.getSelected().get(0).second));
		//						}
		//						break;
		//					case R.id.done_task:
		//						List<Task> subtasks = TaskFragment.this.adapter.getTask().getSubtasks();
		//						for (Pair<Integer, Integer> s : TaskFragment.this.adapter.getSelected()) {
		//							Task t = subtasks.get(s.second);
		//							t.setDone(true);
		//							try {
		//								t.save();
		//							} catch (NoSuchListException e) {
		//								Log.d(TAG, "list did vanish");
		//							}
		//						}
		//						break;
		//					default:
		//						break;
		//				}
		//				mode.finish();
		//				return false;
		//			}
		//
		//			@Override
		//			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		//				MenuInflater menuInflater = mode.getMenuInflater();
		//				menuInflater.inflate(R.menu.context_task, menu);
		//				TaskFragment.this.mActionMode = mode;
		//				return true;
		//			}
		//
		//			@Override
		//			public void onDestroyActionMode(ActionMode mode) {
		//				TaskFragment.this.adapter.resetSelected();
		//			}
		//
		//			@Override
		//			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
		//				Log.d(TAG, "item " + position + " selected");
		//				Integer type = TaskFragment.this.adapter.getData().get(position).first;
		//				int count = TaskFragment.this.adapter.getSelectedCount();
		//				if (type == TYPE.FILE && (count == 0 || TaskFragment.this.adapter.getSelected()
		//						.get(0).first == TYPE.FILE)
		//						|| type == TYPE.SUBTASK && (count == 0 || TaskFragment.this.adapter
		//						.getSelected().get(0).first == TYPE.SUBTASK)) {
		//					TaskFragment.this.adapter.setSelected(position, checked);
		//					TaskFragment.this.adapter.notifyDataSetChanged();
		//					mode.invalidate();
		//				}
		//				count = TaskFragment.this.adapter.getSelectedCount();
		//				if (count == 0) {
		//					mode.finish();// No CAB
		//					return;
		//				}
		//				if (type == TYPE.FILE) {
		//					mode.setTitle(getResources().getQuantityString(
		//							R.plurals.file, count, count));
		//				} else if (type == TYPE.SUBTASK) {
		//					mode.setTitle(getResources().getQuantityString(
		//							R.plurals.subtasks, count, count));
		//				}
		//
		//			}
		//
		//			@Override
		//			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		//				if (TaskFragment.this.adapter.getSelectedCount() > 0) {
		//					menu.findItem(R.id.edit_task)
		//					.setVisible(
		//							TaskFragment.this.adapter.getSelectedCount() == 1
		//							&& TaskFragment.this.adapter.getSelected().get(0).first == TYPE.SUBTASK);
		//					menu.findItem(R.id.done_task).setVisible(
		//							TaskFragment.this.adapter.getSelected().get(0).first == TYPE.SUBTASK);
		//				}
		//				return false;
		//			}
		//		});

		//		if (MirakelPreferences.useBtnCamera()
		//				&& Helpers.isIntentAvailable(main,
		//						MediaStore.ACTION_IMAGE_CAPTURE)) {
		//			this.adapter.setaudioButtonClick(new View.OnClickListener() {
		//				@Override
		//				public void onClick(View v) {
		//					TaskDialogHelpers.handleAudioRecord(getActivity(),
		//							main.getCurrentTask(), new ExecInterfaceWithTask() {
		//						@Override
		//						public void exec(Task t) {
		//							update(t);
		//						}
		//					});
		//				}
		//			});
		//			this.adapter.setcameraButtonClick(new View.OnClickListener() {
		//
		//				@Override
		//				public void onClick(View v) {
		//					try {
		//						Intent cameraIntent = new Intent(
		//								MediaStore.ACTION_IMAGE_CAPTURE);
		//						Uri fileUri = FileUtils
		//								.getOutputMediaFileUri(FileUtils.MEDIA_TYPE_IMAGE);
		//						if (fileUri == null) return;
		//						main.setFileUri(fileUri);
		//						cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		//						getActivity().startActivityForResult(cameraIntent,
		//								MainActivity.RESULT_ADD_PICTURE);
		//
		//					} catch (ActivityNotFoundException a) {
		//						Toast.makeText(
		//								main,
		//								"Opps! Your device doesn't support taking photos",
		//								Toast.LENGTH_SHORT).show();
		//					}
		//
		//				}
		//			});
		//		}
		if (this.task != null) {
			update(this.task);
		}
		Log.d(TAG, "created");

		// this.detailView.setOnTaskChangedListner(new OnTaskChangedListner() {
		//
		// @Override
		// public void onTaskChanged(Task newTask) {
		// // TODO Auto-generated method stub
		// Log.w(TAG, "implement this");
		//
		// }
		// });
		return view;
	}

	// public void showKeyboardForContent() {
	// if (this.adapter != null) {
	// this.adapter.showKeyboardForContent();
	// }
	//
	// }

	public void update(Task t) {
		this.task = t;
		if (this.detailView != null) {
			this.detailView.update(t);
		}

	}


}
