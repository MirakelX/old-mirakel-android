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
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ViewSwitcher;
import de.azapps.mirakel.helper.Helpers.ExecInterface;
import de.azapps.mirakel.helper.MirakelPreferences;
import de.azapps.mirakel.helper.TaskDialogHelpers;
import de.azapps.mirakel.helper.TaskHelper;
import de.azapps.mirakel.reminders.ReminderAlarm;
import de.azapps.mirakelandroid.R;
import de.azapps.tools.Log;

public class TaskDetailHeader extends BaseTaskDetailRow {

	private final static String TAG="TaskDetailHeader";
	private ViewSwitcher	switcher;
	private CheckBox		taskDone;
	private TextView		taskName;
	private TextView		taskPrio;
	private EditText		txt;

	public TaskDetailHeader(Context ctx) {
		super(ctx);
		inflate(ctx, R.layout.task_head_line, this);
		this.taskName = (TextView) findViewById(R.id.task_name);
		this.taskDone = (CheckBox) findViewById(R.id.task_done);
		this.taskPrio = (TextView) findViewById(R.id.task_prio);
		this.switcher = (ViewSwitcher) findViewById(R.id.switch_name);
		this.txt = (EditText) findViewById(R.id.edit_name);
		final InputMethodManager imm = (InputMethodManager) TaskDetailHeader.this.context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		this.taskName
		.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//				if (MirakelPreferences.isTablet()) {
				//					((EditText) ((Activity) context
				//							.findViewById(R.id.tasks_new))
				//							.setOnFocusChangeListener(null);
				//				}
				clearFocus();
				TaskDetailHeader.this.switcher.showNext(); // or switcher.showPrevious();
				CharSequence name =TaskDetailHeader.this.taskName.getText();
				TaskDetailHeader.this.txt.setText(name);
				TaskDetailHeader.this.txt
				.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View view, boolean hasFocus) {
						if (hasFocus) {
							imm.showSoftInput(
									TaskDetailHeader.this.txt,
									InputMethodManager.SHOW_IMPLICIT);
							Log.w(TAG, "handle keyboard show");
						} else {
							imm.showSoftInput(
									TaskDetailHeader.this.txt,
									InputMethodManager.HIDE_IMPLICIT_ONLY);
							Log.wtf(TAG, "handle keyboard hidde");
						}
						imm.restartInput(TaskDetailHeader.this.txt);
					}
				});
				TaskDetailHeader.this.txt.requestFocus();
				TaskDetailHeader.this.txt.setOnEditorActionListener(new OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
						TaskDetailHeader.this.txt.clearFocus();
						imm.restartInput(TaskDetailHeader.this.txt);
						TaskDetailHeader.this.txt
						.setOnFocusChangeListener(null);
						if (actionId == EditorInfo.IME_ACTION_DONE
								&& TaskDetailHeader.this.task != null) {
							TaskDetailHeader.this.task.setName(TaskDetailHeader.this.txt.getText()
									.toString());
							save();
							TaskDetailHeader.this.taskName.setText(TaskDetailHeader.this.task.getName());
							TaskDetailHeader.this.txt.setOnFocusChangeListener(null);
							imm.hideSoftInputFromWindow(
									TaskDetailHeader.this.txt
									.getWindowToken(), 0);
							TaskDetailHeader.this.switcher.showPrevious();

							return true;
						}
						return false;
					}

				});
				TaskDetailHeader.this.txt.setSelection(name.length());
			}
		});
		this.taskPrio.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TaskDialogHelpers.handlePriority( TaskDetailHeader.this.context,  TaskDetailHeader.this.task,
						new ExecInterface() {
					@Override
					public void exec() {
						TaskHelper.setPrio(TaskDetailHeader.this.taskPrio, TaskDetailHeader.this.task);
						save();

					}
				});
			}
		});
	}


	@Override
	protected void updateView() {
		String tname = this.task.getName();
		this.taskName.setText(tname == null ? "" : tname);
		if (MirakelPreferences.isTablet()) {
			this.taskName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
		}

		if (this.switcher.getCurrentView().getId() == R.id.edit_name
				&& !this.task.getName().equals(this.txt.getText().toString())) {
			this.switcher.showPrevious();
		}

		// Task done
		this.taskDone.setChecked(this.task.isDone());
		this.taskDone.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				TaskDetailHeader.this.task.setDone(isChecked);
				ReminderAlarm.updateAlarms(TaskDetailHeader.this.context);
				save();
			}
		});

		// Task priority
		TaskHelper.setPrio(this.taskPrio, this.task);

	}

}
