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

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import de.azapps.mirakel.custom_views.BaseTaskDetailRow.OnTaskChangedListner;
import de.azapps.mirakel.custom_views.TaskDetailDueReminder.Type;
import de.azapps.mirakel.custom_views.TaskSummary.OnTaskClickListner;
import de.azapps.mirakel.helper.MirakelPreferences;
import de.azapps.mirakel.model.task.Task;
import de.azapps.mirakelandroid.R;
import de.azapps.tools.Log;

public class TaskDetailView extends BaseTaskDetailRow implements OnTaskChangedListner {

	public static class TYPE {
		public static class NoSuchItemException extends Exception {
			private static final long	serialVersionUID	= 4952441280983309615L;

			public NoSuchItemException() {
				super();
			}
		}

		public final static int	CONTENT		= 4;
		public final static int	DUE			= 2;
		public final static int	FILE		= 1;
		public final static int	HEADER		= 0;
		public final static int	PROGRESS	= 7;
		public final static int	REMINDER	= 3;
		public final static int	SUBTASK		= 6;

		public final static int	SUBTITLE	= 5;

		public static String getName(int item) throws NoSuchItemException {
			switch (item) {
				case HEADER:
					return "header";
				case FILE:
					return "file";
				case DUE:
					return "due";
				case REMINDER:
					return "reminder";
				case CONTENT:
					return "content";
				case SUBTASK:
					return "subtask";
				case PROGRESS:
					return "progress";
				default:
					throw new NoSuchItemException(); // Throw exception;
			}
		}

		public static String getTranslatedName(Context ctx, int item) throws NoSuchItemException {
			switch (item) {
				case HEADER:
					return ctx.getString(R.string.task_fragment_header);
				case FILE:
					return ctx.getString(R.string.task_fragment_file);
				case DUE:
					return ctx.getString(R.string.task_fragment_due);
				case REMINDER:
					return ctx.getString(R.string.task_fragment_reminder);
				case CONTENT:
					return ctx.getString(R.string.task_fragment_content);
				case SUBTASK:
					return ctx.getString(R.string.task_fragment_subtask);
				case PROGRESS:
					return ctx.getString(R.string.task_fragment_progress);
				default:
					throw new NoSuchItemException(); // Throw exception;
			}

		}
	}

	private static final String						TAG	= "TaskDetailView";

	private final Context							context;
	private List<Integer>							items;
	private final SparseArray<BaseTaskDetailRow>	views;

	public TaskDetailView(Context ctx) {
		super(ctx);
		this.context = ctx;
		this.items = MirakelPreferences.getTaskFragmentLayout();
		this.views = new SparseArray<BaseTaskDetailRow>();
		setupView();
	}

	public TaskDetailView(Context ctx, AttributeSet a) {
		super(ctx, a);
		this.context = ctx;
		this.items = MirakelPreferences.getTaskFragmentLayout();
		this.views = new SparseArray<BaseTaskDetailRow>();
		setupView();
	}

	public TaskDetailView(Context ctx, AttributeSet attrs, int defStyle) {
		super(ctx, attrs, defStyle);
		this.context = ctx;
		this.items = MirakelPreferences.getTaskFragmentLayout();
		this.views = new SparseArray<BaseTaskDetailRow>();
		setupView();
	}

	@Override
	public void onTaskChanged(Task newTask) {
		if (this.taskChangedListner != null) {
			this.taskChangedListner.onTaskChanged(newTask);
		}

	}

	public void setAudioButtonClick(OnClickListener l) {
		BaseTaskDetailRow v = this.views.get(TYPE.FILE);
		if (v != null) {
			((TaskDetailFile) v).setAudioClick(l);
		}
	}

	public void setCameraButtonClick(OnClickListener l) {
		BaseTaskDetailRow v = this.views.get(TYPE.FILE);
		if (v != null) {
			((TaskDetailFile) v).setCameraClick(l);
		}
	}

	public void setOnSubtaskClick(OnTaskClickListner l) {
		BaseTaskDetailRow v = this.views.get(TYPE.SUBTASK);
		if (v != null) {
			((TaskDetailSubtask) v).setOnClick(l);
		}
	}

	private void setupView() {
		removeAllViews();
		setOrientation(VERTICAL);
		for(int position=0;position<this.items.size();position++){
			int i = this.items.get(position);
			BaseTaskDetailRow item;
			switch (i) {
				case TYPE.HEADER:
					item=new TaskDetailHeader(this.context);
					break;
				case TYPE.SUBTASK:
					item=new TaskDetailSubtask(this.context);
					break;
				case TYPE.PROGRESS:
					item=new TaskDetailProgress(this.context);
					break;
				case TYPE.CONTENT:
					item=new TaskDetailContent(this.context);
					break;
				case TYPE.REMINDER:
					if (position > 1
							&& this.items.get(position - 1) != TYPE.DUE
							&& position < this.items.size() && this.items
							.get(position + 1) != TYPE.DUE) {
						TaskDetailDueReminder t = new TaskDetailDueReminder(
								this.context);
						t.setType(Type.Reminder);
						item = t;
						break;
					}
					continue;
				case TYPE.DUE:
					TaskDetailDueReminder t = new TaskDetailDueReminder(
							this.context);
					if (position > 1
							&& this.items.get(position - 1) == TYPE.REMINDER
							|| position < this.items.size()
							&& this.items.get(position + 1) == TYPE.REMINDER) {
						t.setType(Type.Combined);
					}else{
						t.setType(Type.Due);
					}

					item = t;
					break;
				case TYPE.FILE:
					item = new TaskDetailFile(this.context);
					break;
					//$FALL-THROUGH$
				case TYPE.SUBTITLE:
				default:
					//noting
					continue;
			}
			item.setOnTaskChangedListner(this);
			Log.d(TAG, "heigh mainviews: " + item.getHeight());
			addView(item);
			this.views.put(i, item);
		}

	}

	public void updateLayout() {
		this.items = MirakelPreferences.getTaskFragmentLayout();
		setupView();
	}

	@Override
	protected void updateView() {
		int key = 0;
		for (int i = 0; i < this.views.size(); i++) {
			key = this.views.keyAt(i);
			this.views.get(key).update(this.task);
		}
	}

}
