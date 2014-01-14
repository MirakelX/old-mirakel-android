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
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.todddavies.components.progressbar.ProgressWheel;

import de.azapps.mirakel.helper.DateTimeHelper;
import de.azapps.mirakel.helper.Helpers;
import de.azapps.mirakel.helper.Helpers.ExecInterface;
import de.azapps.mirakel.helper.MirakelPreferences;
import de.azapps.mirakel.helper.TaskDialogHelpers;
import de.azapps.mirakel.helper.TaskHelper;
import de.azapps.mirakel.model.task.Task;
import de.azapps.mirakel.reminders.ReminderAlarm;
import de.azapps.mirakelandroid.R;

public class TaskSummary extends TaskDetailSubListBase<Task> implements android.view.View.OnClickListener {
	public interface OnTaskClickListner {
		public abstract void onTaskClick(Task t);
	}
	private OnTaskClickListner	onTaskClick;
	protected Task	task;
	private final ProgressWheel taskProgress;
	private final CheckBox			taskRowDone;
	private final RelativeLayout	taskRowDoneWrapper;
	private final TextView			taskRowDue;
	private final ImageView			taskRowHasContent;
	private final TextView			taskRowList;
	private final TextView			taskRowName;

	private final TextView	taskRowPriority;

	public TaskSummary(Context ctx) {
		super(ctx);
		inflate(ctx, R.layout.tasks_row, this);
		this.taskRowDone = (CheckBox) findViewById(R.id.tasks_row_done);
		this.taskRowDoneWrapper = (RelativeLayout) findViewById(R.id.tasks_row_done_wrapper);
		this.taskRowName = (TextView) findViewById(R.id.tasks_row_name);
		this.taskRowPriority = (TextView) findViewById(R.id.tasks_row_priority);
		this.taskRowDue = (TextView) findViewById(R.id.tasks_row_due);
		this.taskRowHasContent = (ImageView) findViewById(R.id.tasks_row_has_content);
		this.taskRowList = (TextView) findViewById(R.id.tasks_row_list_name);
		this.taskProgress = (ProgressWheel) findViewById(R.id.tasks_row_progress);

		this.taskRowDoneWrapper.setOnClickListener(this);

		this.taskRowPriority.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (TaskSummary.this.task != null) {
					TaskDialogHelpers.handlePriority(TaskSummary.this.context,
							TaskSummary.this.task,
							new ExecInterface() {
						@Override
						public void exec() {
							// Update prio
						}
					});
				}

			}
		});
		setOnClickListener(new OnClickListener() {


			@Override
			public void onClick(View v) {
				TaskSummary.this.onTaskClick.onTaskClick(TaskSummary.this.task);

			}
		});
	}

	@Override
	public void onClick(View v) {
		task.toggleDone();
		ReminderAlarm.updateAlarms(TaskSummary.this.context);
		TaskSummary.this.taskRowDone.setChecked(task.isDone());
		save();
		updateName();
	}

	public void setOnTaskClick(OnTaskClickListner l) {
		this.onTaskClick = l;
	}

	private void updateName() {
		if (this.task.isDone()) {
			this.taskRowName.setTextColor(this.context.getResources().getColor(
					R.color.Grey));
		} else {
			this.taskRowName
			.setTextColor(this.context
					.getResources()
					.getColor(
							MirakelPreferences.isDark() ? android.R.color.primary_text_dark
									: android.R.color.primary_text_light));
		}
	}

	@Override
	public void updatePart(Task newValue) {
		this.task = newValue;
		if (this.task == null) return;
		// Done
		this.taskRowDone.setChecked(this.task.isDone());
		this.taskRowDone.setOnClickListener(this);
		if (this.task.getContent().length() != 0) {
			this.taskRowHasContent.setVisibility(View.VISIBLE);
		} else {
			this.taskRowHasContent.setVisibility(View.INVISIBLE);
		}
		if (this.task.getList() != null) {
			this.taskRowList.setVisibility(View.VISIBLE);
			this.taskRowList.setText(this.task.getList().getName());
		} else {
			this.taskRowList.setVisibility(View.GONE);
		}

		// Name
		this.taskRowName.setText(this.task.getName());

		updateName();

		// Priority
		this.taskRowPriority.setText("" + this.task.getPriority());

		// Progress
		this.taskProgress.setProgress(this.task.getProgress());
		if (this.task.getProgress() > 0) {
			this.taskProgress.setVisibility(VISIBLE);
		} else {
			this.taskProgress.setVisibility(GONE);
		}

		GradientDrawable bg = (GradientDrawable) this.taskRowPriority
				.getBackground();
		bg.setColor(TaskHelper.getPrioColor(this.task.getPriority()));
		this.taskRowPriority.setTag(this.task);
		// Due
		if (this.task.getDue() != null) {
			this.taskRowDue.setVisibility(View.VISIBLE);
			this.taskRowDue.setText(DateTimeHelper.formatDate(this.context,
					this.task.getDue()));
			this.taskRowDue.setTextColor(this.context.getResources().getColor(
					TaskHelper.getTaskDueColor(this.task.getDue(),
							this.task.isDone())));
		} else {
			this.taskRowDue.setVisibility(View.GONE);
		}
		if (MirakelPreferences.colorizeTasks()) {
			if (MirakelPreferences.colorizeSubTasks()) {
				int w = getWidth();
				Helpers.setListColorBackground(this.task.getList(), this, w);
			} else {
				setBackgroundColor(this.context.getResources().getColor(
						android.R.color.transparent));
			}
		} else {
			setBackgroundColor(this.context.getResources().getColor(
					android.R.color.transparent));
		}
	}

}
