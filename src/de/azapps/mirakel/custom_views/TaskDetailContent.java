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
import de.azapps.tools.Log;

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
		// taskContentEdit.setOnFocusChangeListener(this);
		// this.contentHolder.taskContentEdit.addTextChangedListener(new TextWatcher() {
		//
		// @Override
		// public void afterTextChanged(Editable s) {
		// TaskFragmentAdapter.this.taskEditText =
		// TaskFragmentAdapter.this.contentHolder.taskContentEdit.getText().toString();
		//
		// }
		//
		// @Override
		// public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// if (TaskFragmentAdapter.this.editContent
		// && TaskFragmentAdapter.this.recordeContent) {
		// TaskFragmentAdapter.this.cursorPos =
		// TaskFragmentAdapter.this.contentHolder.taskContentEdit.getSelectionEnd();
		// }
		//
		// }
		//
		// @Override
		// public void onTextChanged(CharSequence s, int start, int before, int count) {
		//
		// }
		// });
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
								Log.w(TAG, "handle keyboard show");
							} else {
								imm.showSoftInput(
										TaskDetailContent.this.taskContentEdit,
										InputMethodManager.HIDE_IMPLICIT_ONLY);
								imm.hideSoftInputFromWindow(
												TaskDetailContent.this.taskContentEdit
										.getWindowToken(), 0);
								Log.wtf(TAG, "handle keyboard hidde");
							}

						}
					});
					Log.d(TAG,
							"show keyboard "
									+ TaskDetailContent.this.taskContentEdit
									.requestFocus());
				} else if (!TaskDetailContent.this.isContentEdit) {
					Log.d(TAG, "hidde keyboard");
					// taskContentEdit.requestFocus();
				}

			}
		});



		// this.divider = findViewById(R.id.item_separator);
	}

	// private void saveContent() {
	// if (!this.editContent) {// End Edit, save content
	// // if (this.mActionMode != null) {
	// // this.mActionMode.finish();
	// // }
	// if (txt != null) {
	// // ((InputMethodManager) this.context
	// // .getSystemService(Context.INPUT_METHOD_SERVICE))
	// // .showSoftInput(txt,
	// // InputMethodManager.HIDE_IMPLICIT_ONLY);
	// if (!txt.getText().toString().trim()
	// .equals(this.task.getContent())) {
	// this.task.setContent(txt.getText().toString().trim());
	// try {
	// this.task.save();
	// } catch (NoSuchListException e) {
	// Log.w(TAG, "List did vanish");
	// }
	// } else {
	// Log.d(TAG, "content equal");
	// }
	// } else {
	// Log.d(TAG, "edit_content not found");
	// }
	// }
	// }

	// private View setupContent(ViewGroup parent, View convertView) {
	// final View content = convertView == null ? this.inflater.inflate(
	// R.layout.task_content, parent, false) : convertView;
	// if (convertView == null) {
	// Log.d(TAG, "create");
	//
	// } else {
	// this.contentHolder = (ContentHolder) content.getTag();
	// }
	// while (this.contentHolder.taskContentSwitcher.getCurrentView().getId() != (this.editContent ?
	// R.id.task_content_edit
	// : R.id.task_content)) {
	// this.contentHolder.taskContentSwitcher.showNext();
	// }
	//
	// if (this.editContent) {
	// ((MainActivity) TaskFragmentAdapter.this.context)
	// .getWindow()
	// .setSoftInputMode(
	// WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	// this.contentHolder.editContent.setImageDrawable(this.context.getResources()
	// .getDrawable(android.R.drawable.ic_menu_save));
	// this.contentHolder.divider.setBackgroundColor(this.context.getResources().getColor(
	// inactive_color));
	// this.recordeContent = false;
	//
	// setContentCursorPosition(this.contentHolder);
	//
	// Linkify.addLinks(this.contentHolder.taskContentEdit, Linkify.WEB_URLS);
	// this.contentHolder.taskContentEdit.requestFocus();
	//
	// InputMethodManager imm = (InputMethodManager) this.context
	// .getSystemService(Context.INPUT_METHOD_SERVICE);
	// imm.showSoftInput(this.contentHolder.taskContentEdit,
	// InputMethodManager.SHOW_IMPLICIT);
	// this.contentHolder.taskContentEdit.setSelected(true);
	// if (this.mActionMode == null) {
	// this.mActionMode = ((Activity) this.context)
	// .startActionMode(this.mActionModeCallback);
	//
	// View doneButton;
	// int doneButtonId = Resources.getSystem().getIdentifier(
	// "action_mode_close_button", "id", "android");
	// doneButton = ((Activity) this.context)
	// .findViewById(doneButtonId);
	// if (doneButton != null) {
	// doneButton.setOnClickListener(new View.OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// saveContent();
	// TaskFragmentAdapter.this.editContent = false;
	// notifyDataSetChanged();
	// if (TaskFragmentAdapter.this.mActionMode != null) {
	// TaskFragmentAdapter.this.mActionMode.finish();
	// }
	// }
	// });
	//
	// }
	// }
	// this.recordeContent = true;
	// } else {
	// // Task content
	// ((MainActivity) TaskFragmentAdapter.this.context).getWindow()
	// .setSoftInputMode(
	// WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	// this.contentHolder.editContent.setImageDrawable(this.context.getResources()
	// .getDrawable(android.R.drawable.ic_menu_edit));
	// if (this.task.getContent().length() > 0) {
	// this.contentHolder.taskContent.setText(this.task.getContent());
	// this.taskEditText = this.task.getContent();
	// this.cursorPos = this.taskEditText.length();
	// Linkify.addLinks(this.contentHolder.taskContent, Linkify.WEB_URLS);
	// this.contentHolder.divider.setBackgroundColor(this.context.getResources()
	// .getColor(inactive_color));
	// this.contentHolder.taskContent.setTextColor(this.context.getResources()
	// .getColor(
	// this.darkTheme ? android.R.color.white
	// : android.R.color.black));
	// } else {
	// this.contentHolder.taskContent.setText(R.string.add_content);
	// this.contentHolder.divider.setBackgroundColor(this.context.getResources()
	// .getColor(inactive_color));
	// this.contentHolder.taskContent.setTextColor(this.context.getResources()
	// .getColor(inactive_color));
	// this.taskEditText = "";
	// if (!this.editContent) {
	// this.cursorPos = 0;
	// }
	// }
	// InputMethodManager imm = (InputMethodManager) this.context
	// .getSystemService(Context.INPUT_METHOD_SERVICE);
	// imm.hideSoftInputFromWindow(
	// this.contentHolder.taskContentEdit.getWindowToken(), 0);
	// }
	// return content;
	// }

	@Override
	protected void updateView() {
		this.editContent.setBackgroundResource(android.R.drawable.ic_menu_edit);
		if (this.task.getContent().length() > 0) {
			this.taskContent.setText(this.task.getContent());
			// taskEditText = this.task.getContent();
			// cursorPos = this.taskEditText.length();
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
			//			this.taskEditText = "";
			this.taskContentEdit.setText("");
			//			if (!this.editContent) {
			//				this.cursorPos = 0;
			//			}
		}
	}

}
