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
package de.azapps.mirakel.custom_views;

import android.content.Context;
import android.view.View;
import de.azapps.mirakel.helper.TaskDialogHelpers;
import de.azapps.mirakel.model.task.Task;
import de.azapps.mirakelandroid.R;

public class TaskDetailSubtask extends TaskDetailSubtitleView<Task, TaskSummary> {

	public TaskDetailSubtask(Context ctx) {
		super(ctx);
		this.title.setText(R.string.add_subtasks);
		this.button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TaskDialogHelpers.handleSubtask(
						TaskDetailSubtask.this.context,
						TaskDetailSubtask.this.task,
						new OnTaskChangedListner() {

							@Override
							public void onTaskChanged(Task newTask) {
								update(newTask);

							}
						}, false);
			}
		});
		this.cameraButton.setVisibility(GONE);
		this.audioButton.setVisibility(GONE);
		this.button.setImageDrawable(this.context.getResources().getDrawable(
				android.R.drawable.ic_menu_add));
	}

	@Override
	TaskSummary newElement() {
		return new TaskSummary(this.context);

	}


	@Override
	protected void updateView() {
		updateSubviews(this.task.getSubtasks());
	}

}
