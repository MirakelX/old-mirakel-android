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
import android.text.util.Linkify;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import de.azapps.mirakel.helper.MirakelPreferences;
import de.azapps.mirakelandroid.R;

public class TaskDetailContent extends BaseTaskDetailRow {

	protected static final String	TAG	= "TaskDetailContent";
	private final ImageButton	editContent;
	private boolean				isContentEdit;
	private final TextView		taskContent;
	private final EditText		taskContentEdit;
	private final ViewSwitcher	taskContentSwitcher;

	public TaskDetailContent(Context ctx) {
		super(ctx);
		inflate(ctx, R.layout.task_content, this);
		this.taskContent = (TextView) findViewById(R.id.task_content);
		this.taskContentSwitcher = (ViewSwitcher) findViewById(R.id.switcher_content);
		this.taskContentEdit = (EditText) findViewById(R.id.task_content_edit);
		this.isContentEdit = false;
		this.editContent = (ImageButton) findViewById(R.id.edit_content);
		this.editContent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TaskDetailContent.this.context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
				TaskDetailContent.this.isContentEdit = !TaskDetailContent.this.isContentEdit;
				TaskDetailContent.this.taskContentSwitcher.showNext();
				v.setBackgroundResource(TaskDetailContent.this.isContentEdit ? android.R.drawable.ic_menu_save
						: android.R.drawable.ic_menu_edit);
				if (TaskDetailContent.this.isContentEdit
						&& TaskDetailContent.this.task != null) {
					TaskDetailContent.this.task
					.setContent(TaskDetailContent.this.taskContentEdit
							.getText().toString());
					save();
					TaskDetailContent.this.taskContent
					.setText(TaskDetailContent.this.task.getContent());
					Linkify.addLinks(TaskDetailContent.this.taskContent,
							Linkify.WEB_URLS);
					TaskDetailContent.this.taskContentEdit
					.setOnFocusChangeListener(new OnFocusChangeListener() {

						@Override
						public void onFocusChange(View view, boolean hasFocus) {
							InputMethodManager imm = (InputMethodManager) TaskDetailContent.this.context
									.getSystemService(Context.INPUT_METHOD_SERVICE);
							if (hasFocus) {
								imm.showSoftInput(
										TaskDetailContent.this.taskContentEdit,
										InputMethodManager.SHOW_IMPLICIT);
							} else {
								imm.showSoftInput(
										TaskDetailContent.this.taskContentEdit,
										InputMethodManager.HIDE_IMPLICIT_ONLY);
								imm.hideSoftInputFromWindow(
										TaskDetailContent.this.taskContentEdit
										.getWindowToken(), 0);
							}

						}
					});
					TaskDetailContent.this.taskContentEdit.requestFocus();
				}

			}
		});
	}

	@Override
	protected void updateView() {
		this.editContent.setBackgroundResource(android.R.drawable.ic_menu_edit);
		if (this.task.getContent().length() > 0) {
			this.taskContent.setText(this.task.getContent());
			Linkify.addLinks(this.taskContent, Linkify.WEB_URLS);
			this.taskContent.setTextColor(this.context.getResources().getColor(
					MirakelPreferences.isDark() ? android.R.color.white
							: android.R.color.black));
			this.taskContentEdit.setText(this.task.getContent());
			Linkify.addLinks(this.taskContentEdit, Linkify.WEB_URLS);
		} else {
			this.taskContent.setText(R.string.add_content);
			this.taskContent.setTextColor(this.context
					.getResources().getColor(inactive_color));
			this.taskContentEdit.setText("");
		}
	}

}
