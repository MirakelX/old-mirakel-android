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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import de.azapps.mirakel.custom_views.BaseTaskDetailRow.OnTaskChangedListner;
import de.azapps.mirakel.custom_views.TaskDetailView;
import de.azapps.mirakel.custom_views.TaskSummary.OnTaskClickListner;
import de.azapps.mirakel.helper.Helpers;
import de.azapps.mirakel.helper.Helpers.ExecInterfaceWithTask;
import de.azapps.mirakel.helper.MirakelPreferences;
import de.azapps.mirakel.helper.TaskDialogHelpers;
import de.azapps.mirakel.main_activity.MainActivity;
import de.azapps.mirakel.model.task.Task;
import de.azapps.mirakelandroid.R;
import de.azapps.tools.FileUtils;
import de.azapps.tools.Log;

public class TaskFragment extends Fragment {
	private static final String	TAG			= "TaskActivity";

	private TaskDetailView		detailView;

	private Task				task;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.w(TAG, "taskfragment");
		final MainActivity main=(MainActivity)getActivity();
		View view;
		try {
			view = inflater.inflate(R.layout.task_fragment, container, false);
		} catch (Exception e) {
			Log.i(TAG, Log.getStackTraceString(e));
			return null;
		}
		this.detailView = (TaskDetailView) view
				.findViewById(R.id.taskFragment);
		this.detailView.setOnTaskChangedListner(new OnTaskChangedListner() {

			@Override
			public void onTaskChanged(Task newTask) {
				main.getTasksFragment().updateList();
				main.getListFragment().update();

			}
		});
		this.detailView.setOnSubtaskClick(new OnTaskClickListner() {

			@Override
			public void onTaskClick(Task t) {
				main.setCurrentTask(t);

			}
		});

		if (MirakelPreferences.useBtnCamera()
				&& Helpers.isIntentAvailable(main,
						MediaStore.ACTION_IMAGE_CAPTURE)) {
			this.detailView.setAudioButtonClick(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TaskDialogHelpers.handleAudioRecord(getActivity(),
							main.getCurrentTask(), new ExecInterfaceWithTask() {
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
						main.setFileUri(fileUri);
						cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
						getActivity().startActivityForResult(cameraIntent,
								MainActivity.RESULT_ADD_PICTURE);

					} catch (ActivityNotFoundException a) {
						Toast.makeText(
								main,
								"Opps! Your device doesn't support taking photos",
								Toast.LENGTH_SHORT).show();
					}

				}
			});
		}

		if (this.task != null) {
			update(this.task);
		}

		return view;
	}


	public void update(Task t) {
		this.task = t;
		if (this.detailView != null) {
			this.detailView.update(t);
		}

	}

	public void updateLayout() {
		if (this.detailView != null) {
			this.detailView.updateLayout();
		}

	}

}
