///*******************************************************************************
// * Mirakel is an Android App for managing your ToDo-Lists
// *
// * Copyright (c) 2013-2014 Anatolij Zelenin, Georg Semmler.
// *
// *     This program is free software: you can redistribute it and/or modify
// *     it under the terms of the GNU General Public License as published by
// *     the Free Software Foundation, either version 3 of the License, or
// *     any later version.
// *
// *     This program is distributed in the hope that it will be useful,
// *     but WITHOUT ANY WARRANTY; without even the implied warranty of
// *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *     GNU General Public License for more details.
// *
// *     You should have received a copy of the GNU General Public License
// *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
// ******************************************************************************/
//package de.azapps.mirakel.main_activity.task_fragment;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.GregorianCalendar;
//import java.util.List;
//import java.util.Locale;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.content.Context;
//import android.content.res.Configuration;
//import android.content.res.Resources;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Point;
//import android.graphics.drawable.Drawable;
//import android.graphics.drawable.GradientDrawable;
//import android.os.Build;
//import android.os.Handler;
//import android.support.v4.app.FragmentManager;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.text.util.Linkify;
//import android.util.Pair;
//import android.view.ActionMode;
//import android.view.Display;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.View.OnFocusChangeListener;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.CheckBox;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.LinearLayout.LayoutParams;
//import android.widget.RelativeLayout;
//import android.widget.SeekBar;
//import android.widget.SeekBar.OnSeekBarChangeListener;
//import android.widget.TextView;
//import android.widget.ViewSwitcher;
//
//import com.fourmob.datetimepicker.date.DatePicker;
//import com.fourmob.datetimepicker.date.DatePickerDialog;
//
//import de.azapps.mirakel.Mirakel.NoSuchListException;
//import de.azapps.mirakel.adapter.MirakelArrayAdapter;
//import de.azapps.mirakel.helper.DateTimeHelper;
//import de.azapps.mirakel.helper.Helpers;
//import de.azapps.mirakel.helper.Helpers.ExecInterface;
//import de.azapps.mirakel.helper.MirakelPreferences;
//import de.azapps.mirakel.helper.TaskDialogHelpers;
//import de.azapps.mirakel.helper.TaskHelper;
//import de.azapps.mirakel.main_activity.MainActivity;
//import de.azapps.mirakel.model.file.FileMirakel;
//import de.azapps.mirakel.model.recurring.Recurring;
//import de.azapps.mirakel.model.task.Task;
//import de.azapps.mirakel.reminders.ReminderAlarm;
//import de.azapps.mirakelandroid.R;
//import de.azapps.tools.Log;
//
//public class TaskFragmentAdapter extends MirakelArrayAdapter<Pair<Integer, Integer>> implements OnFocusChangeListener {// TYPE,INDEX
//	static class ContentHolder {
//		View			divider;
//		ImageButton		editContent;
//		TextView		taskContent;
//		EditText		taskContentEdit;
//		ViewSwitcher	taskContentSwitcher;
//	}
//	static class DueHolder {
//		ImageButton	reccurence;
//		TextView	taskDue;
//	}
//	static class FileHolder {
//		ImageView	fileImage;
//		TextView	fileName;
//		TextView	filePath;
//	}
//	static class HeaderHolder {
//		ViewSwitcher	switcher;
//		CheckBox		taskDone;
//		TextView		taskName;
//		TextView		taskPrio;
//		EditText		txt;
//	}
//	static class ReminderHolder {
//		ImageButton	recurringButton;
//		TextView	taskReminder;
//	}
//	static class SubtitleHolder {
//		ImageButton	audioButton;
//		ImageButton	button;
//		ImageButton	cameraButton;
//		View		divider;
//		TextView	title;
//	}
//	static class TaskHolder {
//		CheckBox		taskRowDone;
//		RelativeLayout	taskRowDoneWrapper;
//		TextView		taskRowDue, taskRowList;
//		ImageView		taskRowHasContent;
//		TextView		taskRowName;
//		TextView		taskRowPriority;
//	}
//	public static class TYPE {
//		public static class NoSuchItemException extends Exception {
//			private static final long	serialVersionUID	= 4952441280983309615L;
//
//			public NoSuchItemException() {
//				super();
//			}
//		}
//		public final static int	CONTENT		= 4;
//		public final static int	DUE			= 2;
//		public final static int	FILE		= 1;
//		public final static int	HEADER		= 0;
//		public final static int	PROGRESS	= 7;
//		public final static int	REMINDER	= 3;
//		public final static int	SUBTASK		= 6;
//
//		public final static int	SUBTITLE	= 5;
//
//		public static String getName(int item) throws NoSuchItemException {
//			switch (item) {
//				case HEADER:
//					return "header";
//				case FILE:
//					return "file";
//				case DUE:
//					return "due";
//				case REMINDER:
//					return "reminder";
//				case CONTENT:
//					return "content";
//				case SUBTASK:
//					return "subtask";
//				case PROGRESS:
//					return "progress";
//				default:
//					throw new NoSuchItemException(); // Throw exception;
//			}
//		}
//
//		public static String getTranslatedName(Context ctx, int item) throws NoSuchItemException {
//			switch (item) {
//				case HEADER:
//					return ctx.getString(R.string.task_fragment_header);
//				case FILE:
//					return ctx.getString(R.string.task_fragment_file);
//				case DUE:
//					return ctx.getString(R.string.task_fragment_due);
//				case REMINDER:
//					return ctx.getString(R.string.task_fragment_reminder);
//				case CONTENT:
//					return ctx.getString(R.string.task_fragment_content);
//				case SUBTASK:
//					return ctx.getString(R.string.task_fragment_subtask);
//				case PROGRESS:
//					return ctx.getString(R.string.task_fragment_progress);
//				default:
//					throw new NoSuchItemException(); // Throw exception;
//			}
//
//		}
//	}
//	private static final Integer	inactive_color				= android.R.color.darker_gray;
//	private static int				minDueNextToReminderSize	= 800;
//	private static final int		SUBTITLE_SUBTASKS			= 0,
//			SUBTITLE_FILES = 1, SUBTITLE_PROGRESS = 2;
//	private static final String		TAG							= "TaskFragmentAdapter";
//
//
//	@SuppressWarnings("deprecation")
//	@SuppressLint("NewApi")
//	public static void setRecurringImage(ImageButton image, Context ctx, int id) {
//		Drawable d = ctx.getResources().getDrawable(
//				id == -1 ? android.R.drawable.ic_menu_mylocation
//						: android.R.drawable.ic_menu_rotate);
//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//			image.setBackgroundDrawable(d);
//		} else {
//			image.setBackground(d);
//		}
//	}
//
//	private TaskFragmentAdapter		adapter;
//
//	private View.OnClickListener	audioButtonClick			= null;
//
//	private View.OnClickListener	cameraButtonClick			= null;
//
//	private ContentHolder	contentHolder;
//
//	protected int				cursorPos;
//
//	private boolean					editContent;
//
//	private List<FileMirakel>		files;
//
//	private HeaderHolder	headerHolder;
//
//	private LayoutInflater			inflater;
//	protected boolean	lastWasShowKeyboard;
//
//	protected ActionMode			mActionMode;
//
//	private final ActionMode.Callback	mActionModeCallback	= new ActionMode.Callback() {
//
//		// Called when the user selects a
//		// contextual
//		// menu item
//		@Override
//		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//			switch (item
//					.getItemId()) {
//						case R.id.save:
//							TaskFragmentAdapter.this.editContent = !TaskFragmentAdapter.this.editContent;
//							saveContent();
//							notifyDataSetChanged();
//							break;
//						case R.id.cancel:
//							TaskFragmentAdapter.this.editContent = !TaskFragmentAdapter.this.editContent;
//							notifyDataSetChanged();
//							mode.finish();
//							break;
//						default:
//							Log.d(TAG,
//									"unkown menubutton");
//							break;
//			}
//			return true;
//		}
//
//		// Called when the action mode is
//		// created;
//		// startActionMode() was called
//		@Override
//		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//			// Inflate a menu resource
//			// providing
//			// context menu items
//			MenuInflater menuInflater = mode
//					.getMenuInflater();
//			menuInflater
//			.inflate(
//					R.menu.save,
//					menu);
//			return true;
//		}
//
//		// Called when the user exits the
//		// action
//		// mode
//		@Override
//		public void onDestroyActionMode(ActionMode mode) {
//			TaskFragmentAdapter.this.mActionMode = null;
//			TaskFragmentAdapter.this.editContent = false;
//			notifyDataSetChanged();
//		}
//
//		// Called each time the action mode
//		// is
//		// shown. Always called after
//		// onCreateActionMode, but
//		// may be called multiple times if
//		// the mode
//		// is invalidated.
//		@Override
//		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//			return false; // Return false if
//			// nothing
//			// is done
//		}
//
//	};
//
//	protected Handler				mHandler;
//
//	protected boolean				mIgnoreTimeSet;
//
//	Bitmap	preview;
//
//	private boolean	recordeContent;
//	private List<Task>				subtasks;
//	private Task					task;
//
//	private String				taskEditText;
//
//	public TaskFragmentAdapter(Context c) {
//		// Do not call!!
//		super(c, 0, new ArrayList<Pair<Integer, Integer>>());
//	}
//
//	public TaskFragmentAdapter(Context context, int textViewResourceId, Task t) {
//		super(context, textViewResourceId, generateData(t));
//		this.task = t;
//		if (this.task == null) {
//			this.task = Task.getDummy(context);
//		}
//		this.subtasks = this.task.getSubtasks();
//		this.files = this.task.getFiles();
//		this.inflater = ((Activity) context).getLayoutInflater();
//		this.adapter = this;
//		this.editContent = false;
//
//	}
//
//	public void cancelEditing() {
//		// TaskName
//		if (this.headerHolder != null) {
//			InputMethodManager imm = (InputMethodManager) this.context
//					.getSystemService(Context.INPUT_METHOD_SERVICE);
//			this.headerHolder.taskName.setText(this.task.getName());
//			this.headerHolder.txt.setText(this.task.getName());
//			this.headerHolder.txt.setOnFocusChangeListener(null);
//			imm.hideSoftInputFromWindow(this.headerHolder.txt.getWindowToken(), 0);
//			this.headerHolder.switcher.showPrevious();
//		}
//
//	}
//
//	public void closeActionMode() {
//		if (this.mActionMode != null) {
//			this.mActionMode.finish();
//		}
//
//	}
//	public List<Pair<Integer, Integer>> getData() {
//		return this.data;
//	}
//	@Override
//	public int getItemViewType(int position) {
//		return this.data.get(position).first;
//	}
//
//	public Task getTask() {
//		return this.task;
//	}
//
//	@SuppressLint("NewApi")
//	@SuppressWarnings("deprecation")
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		View row = new View(this.context);
//		if (position >= this.data.size()) {
//			Log.w(TAG, "position > data");
//			return row;
//		}
//		int width;
//		Display display = ((Activity) this.context).getWindowManager()
//				.getDefaultDisplay();
//		try {
//			Point size = new Point();
//			display.getRealSize(size);
//			width = size.x;
//		} catch (NoSuchMethodError e) {
//			// API<17
//			width = display.getWidth();
//		}
//		switch (getItemViewType(position)) {
//			case TYPE.DUE:
//				row = setupDue(parent, convertView, width, position);
//				break;
//			case TYPE.FILE:
//				row = setupFile(parent, this.files.get(this.data.get(position).second),
//						convertView, position);
//				break;
//			case TYPE.HEADER:
//				row = setupHeader(convertView);
//				break;
//			case TYPE.REMINDER:
//				if (width < minDueNextToReminderSize
//						|| position > 1
//						&& this.data.get(position - 1).first != TYPE.DUE
//						&& position < this.data.size() && this.data
//						.get(position + 1).first != TYPE.DUE) {
//					row = setupReminder(parent, convertView);
//				}
//				break;
//			case TYPE.SUBTASK:
//				if (this.data.get(position).second >= this.subtasks.size()) {
//					setData(this.task);
//					break;
//				}
//				row = setupSubtask(parent, convertView,
//						this.subtasks.get(this.data.get(position).second), position);
//				break;
//			case TYPE.SUBTITLE:
//				String title = null;
//				OnClickListener action = null;
//				boolean pencilButton = false;
//				boolean cameraButton = false;
//				switch (this.data.get(position).second) {
//					case SUBTITLE_SUBTASKS:
//						title = this.context.getString(R.string.add_subtasks);
//						action = new OnClickListener() {
//							@Override
//							public void onClick(View v) {
//								TaskDialogHelpers.handleSubtask(TaskFragmentAdapter.this.context, TaskFragmentAdapter.this.task,
//										TaskFragmentAdapter.this.adapter, false);
//							}
//						};
//						break;
//					case SUBTITLE_FILES:
//						cameraButton = true;
//						title = this.context.getString(R.string.add_files);
//						action = new OnClickListener() {
//							@Override
//							public void onClick(View v) {
//								Helpers.showFileChooser(
//										MainActivity.RESULT_ADD_FILE,
//										TaskFragmentAdapter.this.context.getString(R.string.file_select),
//										(Activity) TaskFragmentAdapter.this.context);
//							}
//						};
//						break;
//					case SUBTITLE_PROGRESS:
//						title = this.context
//						.getString(R.string.task_fragment_progress);
//						break;
//					default:
//						Log.w(TAG, "unknown subtitle");
//						break;
//				}
//				row = setupSubtitle(parent, title, pencilButton, cameraButton,
//						action, convertView);
//				break;
//			case TYPE.CONTENT:
//				row = setupContent(parent, convertView);
//				break;
//			case TYPE.PROGRESS:
//				row = setupProgress(parent, convertView);
//				break;
//			default:
//				Log.d(TAG, "unknown view-type");
//				break;
//
//		}
//
//		return row;
//	}
//
//
//	@Override
//	public int getViewTypeCount() {
//		return 8;
//	}
//
//	private void handleKeyboard(boolean hasFocus) {
//		InputMethodManager imm = (InputMethodManager) TaskFragmentAdapter.this.context
//				.getSystemService(Context.INPUT_METHOD_SERVICE);
//		if (hasFocus) {
//			if (!TaskFragmentAdapter.this.lastWasShowKeyboard) {
//				TaskFragmentAdapter.this.lastWasShowKeyboard = true;
//				imm.showSoftInput(
//						TaskFragmentAdapter.this.contentHolder.taskContentEdit,
//						InputMethodManager.SHOW_IMPLICIT);
//				Log.w(TAG, "handle keyboard show");
//			}
//
//		} else {
//			if (TaskFragmentAdapter.this.lastWasShowKeyboard) {
//				TaskFragmentAdapter.this.lastWasShowKeyboard = false;
//				imm.showSoftInput(
//						TaskFragmentAdapter.this.contentHolder.taskContentEdit,
//						InputMethodManager.HIDE_IMPLICIT_ONLY);
//				Log.wtf(TAG, "handle keyboard hidde");
//			}
//
//		}
//
//	}
//
//	public boolean isEditContent() {
//		return this.editContent;
//	}
//
//
//	@Override
//	public void onFocusChange(View v, final boolean hasFocus) {
//		Log.i(TAG, "hasFocus: " + hasFocus);
//		Log.i(TAG, "editContent: "
//				+ TaskFragmentAdapter.this.editContent);
//		if (TaskFragmentAdapter.this.editContent == hasFocus) {
//			handleKeyboard(hasFocus);
//			if (!hasFocus
//					&& TaskFragmentAdapter.this.contentHolder.taskContentSwitcher
//					.getCurrentView().getId() != R.id.switcher_content) {
//				TaskFragmentAdapter.this.contentHolder.taskContentSwitcher
//				.showNext();
//			}
//			TaskFragmentAdapter.this.contentHolder.editContent
//			.postDelayed(new Runnable() {
//
//				@Override
//				public void run() {
//					handleKeyboard(hasFocus);
//				}
//			}, 10);
//		} else {
//			this.cursorPos++;
//		}
//	}
//
//	private void saveContent() {
//		if (!this.editContent) {// End Edit, save content
//			if (this.mActionMode != null) {
//				this.mActionMode.finish();
//			}
//			EditText txt = (EditText) ((MainActivity) this.context)
//					.findViewById(R.id.task_content_edit);
//			if (txt != null) {
//				// ((InputMethodManager) this.context
//				// .getSystemService(Context.INPUT_METHOD_SERVICE))
//				// .showSoftInput(txt,
//				// InputMethodManager.HIDE_IMPLICIT_ONLY);
//				if (!txt.getText().toString().trim().equals(this.task.getContent())) {
//					this.task.setContent(txt.getText().toString().trim());
//					try {
//						this.task.save();
//					} catch (NoSuchListException e) {
//						Log.w(TAG, "List did vanish");
//					}
//				} else {
//					Log.d(TAG, "content equal");
//				}
//			} else {
//				Log.d(TAG, "edit_content not found");
//			}
//		}
//	}
//
//	public void setaudioButtonClick(OnClickListener audioButtonClick) {
//		this.audioButtonClick = audioButtonClick;
//	}
//
//	public void setcameraButtonClick(OnClickListener cameraButtonClick) {
//		this.cameraButtonClick = cameraButtonClick;
//	}
//
//
//	private void setContentCursorPosition(final ContentHolder holder) {
//		holder.taskContentEdit.setText(this.taskEditText);
//		if (this.cursorPos == 0
//				|| this.cursorPos > holder.taskContentEdit.getText().length()) {
//			holder.taskContentEdit.setSelection(holder.taskContentEdit
//					.getText().length());
//		} else {
//			holder.taskContentEdit.setSelection(this.cursorPos);
//		}
//	}
//
//	public void setData(Task t) {
//		if (t == null) {
//			Log.wtf(TAG, "task null");
//			return;
//		}
//		List<Pair<Integer, Integer>> generateData = generateData(t);
//		super.changeData(generateData);
//		this.task = t;
//		this.subtasks = this.task.getSubtasks();
//		this.files = this.task.getFiles();
//		notifyDataSetInvalidated();
//	}
//
//	public void setEditContent(boolean edit) {
//		this.editContent = edit;
//		notifyDataSetChanged();
//		if (this.mActionMode != null && edit == false) {
//			this.mActionMode.finish();
//		}
//	}
//
//	private View setupContent(ViewGroup parent, View convertView) {
//		final View content = convertView == null ? this.inflater.inflate(
//				R.layout.task_content, parent, false) : convertView;
//				if (convertView == null) {
//					Log.d(TAG, "create");
//					this.contentHolder = new ContentHolder();
//					this.contentHolder.taskContent = (TextView) content
//							.findViewById(R.id.task_content);
//					this.contentHolder.taskContentSwitcher = (ViewSwitcher) content
//							.findViewById(R.id.switcher_content);
//					this.contentHolder.taskContentEdit = (EditText) content
//							.findViewById(R.id.task_content_edit);
//					this.contentHolder.taskContentEdit.setOnFocusChangeListener(this);
//					this.contentHolder.taskContentEdit.addTextChangedListener(new TextWatcher() {
//
//						@Override
//						public void afterTextChanged(Editable s) {
//							TaskFragmentAdapter.this.taskEditText = TaskFragmentAdapter.this.contentHolder.taskContentEdit.getText().toString();
//
//						}
//
//						@Override
//						public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//							if (TaskFragmentAdapter.this.editContent
//									&& TaskFragmentAdapter.this.recordeContent) {
//								TaskFragmentAdapter.this.cursorPos = TaskFragmentAdapter.this.contentHolder.taskContentEdit.getSelectionEnd();
//							}
//
//						}
//
//						@Override
//						public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//						}
//					});
//					this.contentHolder.editContent = (ImageButton) content
//							.findViewById(R.id.edit_content);
//					this.contentHolder.editContent.setOnClickListener(new OnClickListener() {
//						@Override
//						public void onClick(View v) {
//							Log.d(TAG, "click");
//							TaskFragmentAdapter.this.editContent = !TaskFragmentAdapter.this.editContent;
//							saveContent();
//							notifyDataSetChanged();
//						}
//					});
//					this.contentHolder.divider = content.findViewById(R.id.item_separator);
//					content.setTag(this.contentHolder);
//				} else {
//					this.contentHolder = (ContentHolder) content.getTag();
//				}
//				while (this.contentHolder.taskContentSwitcher.getCurrentView().getId() != (this.editContent ? R.id.task_content_edit
//						: R.id.task_content)) {
//					this.contentHolder.taskContentSwitcher.showNext();
//				}
//
//				if (this.editContent) {
//					((MainActivity) TaskFragmentAdapter.this.context)
//					.getWindow()
//					.setSoftInputMode(
//							WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//					this.contentHolder.editContent.setImageDrawable(this.context.getResources()
//							.getDrawable(android.R.drawable.ic_menu_save));
//					this.contentHolder.divider.setBackgroundColor(this.context.getResources().getColor(
//							inactive_color));
//					this.recordeContent = false;
//
//					setContentCursorPosition(this.contentHolder);
//
//					Linkify.addLinks(this.contentHolder.taskContentEdit, Linkify.WEB_URLS);
//					this.contentHolder.taskContentEdit.requestFocus();
//
//					InputMethodManager imm = (InputMethodManager) this.context
//							.getSystemService(Context.INPUT_METHOD_SERVICE);
//					imm.showSoftInput(this.contentHolder.taskContentEdit,
//							InputMethodManager.SHOW_IMPLICIT);
//					this.contentHolder.taskContentEdit.setSelected(true);
//					if (this.mActionMode == null) {
//						this.mActionMode = ((Activity) this.context)
//								.startActionMode(this.mActionModeCallback);
//
//						View doneButton;
//						int doneButtonId = Resources.getSystem().getIdentifier(
//								"action_mode_close_button", "id", "android");
//						doneButton = ((Activity) this.context)
//								.findViewById(doneButtonId);
//						if (doneButton != null) {
//							doneButton.setOnClickListener(new View.OnClickListener() {
//
//								@Override
//								public void onClick(View v) {
//									saveContent();
//									TaskFragmentAdapter.this.editContent = false;
//									notifyDataSetChanged();
//									if (TaskFragmentAdapter.this.mActionMode != null) {
//										TaskFragmentAdapter.this.mActionMode.finish();
//									}
//								}
//							});
//
//						}
//					}
//					this.recordeContent = true;
//				} else {
//					// Task content
//					((MainActivity) TaskFragmentAdapter.this.context).getWindow()
//					.setSoftInputMode(
//							WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//					this.contentHolder.editContent.setImageDrawable(this.context.getResources()
//							.getDrawable(android.R.drawable.ic_menu_edit));
//					if (this.task.getContent().length() > 0) {
//						this.contentHolder.taskContent.setText(this.task.getContent());
//						this.taskEditText = this.task.getContent();
//						this.cursorPos = this.taskEditText.length();
//						Linkify.addLinks(this.contentHolder.taskContent, Linkify.WEB_URLS);
//						this.contentHolder.divider.setBackgroundColor(this.context.getResources()
//								.getColor(inactive_color));
//						this.contentHolder.taskContent.setTextColor(this.context.getResources()
//								.getColor(
//										this.darkTheme ? android.R.color.white
//												: android.R.color.black));
//					} else {
//						this.contentHolder.taskContent.setText(R.string.add_content);
//						this.contentHolder.divider.setBackgroundColor(this.context.getResources()
//								.getColor(inactive_color));
//						this.contentHolder.taskContent.setTextColor(this.context.getResources()
//								.getColor(inactive_color));
//						this.taskEditText = "";
//						if (!this.editContent) {
//							this.cursorPos = 0;
//						}
//					}
//					InputMethodManager imm = (InputMethodManager) this.context
//							.getSystemService(Context.INPUT_METHOD_SERVICE);
//					imm.hideSoftInputFromWindow(
//							this.contentHolder.taskContentEdit.getWindowToken(), 0);
//				}
//				return content;
//	}
//
//	private View setupDue(ViewGroup parent, View convertView, int width, int pos) {
//		if (width < minDueNextToReminderSize) {
//			final View due = convertView == null || convertView.getId() != R.id.due_wrapper ? this.inflater
//					.inflate(R.layout.task_due, parent, false) : convertView;
//					setupDueView(convertView, due);
//					return due;
//		}
//		View due_reminder = convertView == null || convertView.getId() != R.id.wrapper_reminder_due ? this.inflater
//				.inflate(R.layout.due_reminder_row, parent, false)
//				: convertView;
//				View due = due_reminder.findViewById(R.id.wrapper_due);
//				View reminder = due_reminder.findViewById(R.id.wrapper_reminder);
//				setupDueView(due, due);
//				if (pos < this.data.size() && this.data.get(pos + 1).first == TYPE.REMINDER
//						|| pos > 1 && this.data.get(pos - 1).first == TYPE.REMINDER) {
//					setupReminderView(reminder, reminder);
//				} else {
//					reminder.setVisibility(View.GONE);
//				}
//				return due_reminder;
//	}
//
//	@SuppressLint("NewApi")
//	private void setupDueView(View convertView, final View due) {
//		if (due == null) {
//			Log.wtf(TAG, "due=null");
//			return;
//		}
//		final DueHolder holder;
//		if (convertView == null || convertView.getTag() == null) {
//			holder = new DueHolder();
//			holder.taskDue = (TextView) due.findViewById(R.id.task_due);
//			holder.reccurence = (ImageButton) due
//					.findViewById(R.id.reccuring_due);
//			holder.reccurence.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					TaskDialogHelpers.handleRecurrence((Activity) TaskFragmentAdapter.this.context,
//							TaskFragmentAdapter.this.task, true, holder.reccurence, TaskFragmentAdapter.this.darkTheme);
//				}
//			});
//			holder.taskDue.setOnClickListener(new View.OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					TaskFragmentAdapter.this.mIgnoreTimeSet = false;
//					final Calendar dueLocal = TaskFragmentAdapter.this.task.getDue() == null ? new GregorianCalendar()
//					: TaskFragmentAdapter.this.task.getDue();
//					final FragmentManager fm = ((MainActivity) TaskFragmentAdapter.this.context)
//							.getSupportFragmentManager();
//					final DatePickerDialog datePickerDialog = DatePickerDialog
//							.newInstance(
//									new DatePicker.OnDateSetListener() {
//
//										@Override
//										public void onDateSet(DatePicker dp, int year, int month, int day) {
//											if (TaskFragmentAdapter.this.mIgnoreTimeSet) return;
//											TaskFragmentAdapter.this.task.setDue(new GregorianCalendar(
//													year, month, day));
//											((MainActivity) TaskFragmentAdapter.this.context)
//											.saveTask(TaskFragmentAdapter.this.task);
//											holder.taskDue
//											.setText(new SimpleDateFormat(
//													TaskFragmentAdapter.this.context.getString(R.string.dateFormat),
//													Locale.getDefault())
//											.format(TaskFragmentAdapter.this.task
//													.getDue()
//													.getTime()));
//
//										}
//
//										@Override
//										public void onNoDateSet() {
//											TaskFragmentAdapter.this.task.setDue(null);
//											((MainActivity) TaskFragmentAdapter.this.context)
//											.saveTask(TaskFragmentAdapter.this.task);
//											holder.taskDue
//											.setText(TaskFragmentAdapter.this.context
//													.getString(R.string.no_date));
//
//										}
//									}, dueLocal.get(Calendar.YEAR), dueLocal
//									.get(Calendar.MONTH), dueLocal
//									.get(Calendar.DAY_OF_MONTH), false,
//									TaskFragmentAdapter.this.darkTheme, true);
//					// datePickerDialog.setYearRange(2005, 2036);// must be <
//					// 2037
//					datePickerDialog.show(fm, "datepicker");
//				}
//			});
//			due.setTag(holder);
//		} else {
//			holder = (DueHolder) due.getTag();
//		}
//		// Task due
//		Drawable dueImg = this.context.getResources().getDrawable(
//				android.R.drawable.ic_menu_today);
//		dueImg.setBounds(0, 1, 42, 42);
//		Configuration config = this.context.getResources().getConfiguration();
//		setRecurringImage(holder.reccurence, this.context, this.task.getRecurrenceId());
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
//				&& config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
//			holder.taskDue.setCompoundDrawables(null, null, dueImg, null);
//		} else {
//			holder.taskDue.setCompoundDrawables(dueImg, null, null, null);
//		}
//		setupRecurrenceDrawable(holder.reccurence, this.task.getRecurring());
//		if (this.task.getDue() == null) {
//			holder.taskDue.setText(this.context.getString(R.string.no_date));
//			holder.taskDue.setTextColor(this.context.getResources().getColor(
//					inactive_color));
//		} else {
//			holder.taskDue.setText(DateTimeHelper.formatDate(this.context,
//					this.task.getDue()));
//			holder.taskDue.setTextColor(this.context.getResources().getColor(
//					TaskHelper.getTaskDueColor(this.task.getDue(), this.task.isDone())));
//		}
//	}
//
//
//
//
//
//
//
//	@SuppressLint("NewApi")
//	private void setupRecurrenceDrawable(ImageButton reccurence, Recurring recurring) {
//		if (Build.VERSION.SDK_INT < 16) return;
//		if (recurring == null || recurring.getId() == -1) {
//			reccurence.setBackground(this.context.getResources().getDrawable(
//					android.R.drawable.ic_menu_mylocation));
//		} else {
//			reccurence.setBackground(this.context.getResources().getDrawable(
//					android.R.drawable.ic_menu_rotate));
//		}
//
//	}
//
//	private View setupReminder(ViewGroup parent, View convertView) {
//		final View reminder = convertView == null
//				|| convertView.getId() != R.id.reminder_wrapper ? this.inflater
//						.inflate(R.layout.task_reminder, parent, false) : convertView;
//						setupReminderView(convertView, reminder);
//						return reminder;
//	}
//
//	@SuppressLint("NewApi")
//	private void setupReminderView(View convertView, final View reminder) {
//		if (reminder == null) {
//			Log.wtf(TAG, "reminder=null");
//			return;
//		}
//		final ReminderHolder holder;
//		if (convertView == null || convertView.getTag() == null) {
//			holder = new ReminderHolder();
//			holder.taskReminder = (TextView) reminder
//					.findViewById(R.id.task_reminder);
//			holder.taskReminder.setOnClickListener(new View.OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					TaskDialogHelpers.handleReminder((Activity) TaskFragmentAdapter.this.context, TaskFragmentAdapter.this.task,
//							new ExecInterface() {
//
//						@Override
//						public void exec() {
//							notifyDataSetChanged();
//							ReminderAlarm.updateAlarms(TaskFragmentAdapter.this.context);
//
//						}
//					}, TaskFragmentAdapter.this.darkTheme);
//				}
//			});
//			holder.recurringButton = (ImageButton) reminder
//					.findViewById(R.id.reccuring_reminder);
//			holder.recurringButton.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					TaskDialogHelpers.handleRecurrence((Activity) TaskFragmentAdapter.this.context,
//							TaskFragmentAdapter.this.task, false, holder.recurringButton, TaskFragmentAdapter.this.darkTheme);
//
//				}
//			});
//			reminder.setTag(holder);
//		} else {
//			holder = (ReminderHolder) reminder.getTag();
//		}
//		// Task Reminder
//		Drawable reminder_img = this.context.getResources().getDrawable(
//				android.R.drawable.ic_menu_recent_history);
//		reminder_img.setBounds(0, 1, 42, 42);
//		Configuration config = this.context.getResources().getConfiguration();
//		setRecurringImage(holder.recurringButton, this.context,
//				this.task.getRecurringReminderId());
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
//				&& config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
//			holder.taskReminder.setCompoundDrawables(null, null, reminder_img,
//					null);
//		} else {
//			holder.taskReminder.setCompoundDrawables(reminder_img, null, null,
//					null);
//		}
//
//		if (this.task.getReminder() == null) {
//			holder.taskReminder
//			.setText(this.context.getString(R.string.no_reminder));
//			holder.taskReminder.setTextColor(this.context.getResources().getColor(
//					inactive_color));
//		} else {
//			holder.taskReminder.setText(DateTimeHelper.formatDate(
//					this.task.getReminder(),
//					this.context.getString(R.string.humanDateTimeFormat)));
//			holder.taskReminder.setTextColor(this.context.getResources().getColor(
//					inactive_color));
//		}
//	}
//
//
//
//	private View setupSubtitle(ViewGroup parent, String title, boolean pencilButton, boolean cameraButton, OnClickListener action, View convertView) {
//		if (title == null) return new View(this.context);
//
//		final View subtitle = convertView == null ? this.inflater.inflate(
//				R.layout.task_subtitle, parent, false) : convertView;
//
//				final SubtitleHolder holder;
//				if (convertView == null) {
//					holder = new SubtitleHolder();
//					holder.title = (TextView) subtitle
//							.findViewById(R.id.task_subtitle);
//					holder.button = (ImageButton) subtitle
//							.findViewById(R.id.task_subtitle_button);
//					holder.audioButton = (ImageButton) subtitle
//							.findViewById(R.id.task_subtitle_audio_button);
//					holder.cameraButton = (ImageButton) subtitle
//							.findViewById(R.id.task_subtitle_camera_button);
//					holder.divider = subtitle.findViewById(R.id.item_separator);
//					subtitle.setTag(holder);
//				} else {
//					holder = (SubtitleHolder) subtitle.getTag();
//				}
//				holder.title.setText(title);
//				if (action != null) {
//					holder.button.setOnClickListener(action);
//					holder.button.setVisibility(View.VISIBLE);
//				} else {
//					holder.button.setVisibility(View.GONE);
//				}
//
//				if (cameraButton) {
//
//					holder.cameraButton.setVisibility(View.VISIBLE);
//					holder.audioButton.setVisibility(View.VISIBLE);
//					if (this.cameraButtonClick != null) {
//						holder.cameraButton.setOnClickListener(this.cameraButtonClick);
//					}
//					if (this.audioButtonClick != null) {
//						holder.audioButton.setOnClickListener(this.audioButtonClick);
//					}
//				} else {
//					holder.cameraButton.setVisibility(View.INVISIBLE);
//					holder.audioButton.setVisibility(View.INVISIBLE);
//				}
//				if (pencilButton) {
//					if (this.editContent) {
//						holder.button.setImageDrawable(this.context.getResources()
//								.getDrawable(android.R.drawable.ic_menu_save));
//					} else {
//						holder.button.setImageDrawable(this.context.getResources()
//								.getDrawable(android.R.drawable.ic_menu_edit));
//					}
//				} else {
//					holder.button.setImageDrawable(this.context.getResources().getDrawable(
//							android.R.drawable.ic_menu_add));
//				}
//				holder.divider.setBackgroundColor(this.context.getResources().getColor(
//						inactive_color));
//				holder.title.setTextColor(this.context.getResources().getColor(
//						inactive_color));
//				return subtitle;
//	}
//
//	public void showKeyboardForContent() {
//		if (this.contentHolder != null) {
//			Runnable r = new Runnable() {
//
//				@Override
//				public void run() {
//					InputMethodManager imm = (InputMethodManager) TaskFragmentAdapter.this.context
//							.getSystemService(Context.INPUT_METHOD_SERVICE);
//					imm.showSoftInput(
//							TaskFragmentAdapter.this.contentHolder.taskContentEdit,
//							InputMethodManager.SHOW_IMPLICIT);
//
//				}
//			};
//			r.run();
//			this.contentHolder.taskContentEdit.postDelayed(r, 100);
//		}
//	}
//
// }
