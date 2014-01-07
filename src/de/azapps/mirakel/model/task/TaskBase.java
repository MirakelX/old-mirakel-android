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
package de.azapps.mirakel.model.task;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import android.content.ContentValues;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import de.azapps.mirakel.Mirakel.NoSuchListException;
import de.azapps.mirakel.helper.DateTimeHelper;
import de.azapps.mirakel.model.DatabaseHelper;
import de.azapps.mirakel.model.list.ListMirakel;
import de.azapps.mirakel.model.list.SpecialList;
import de.azapps.mirakel.model.recurring.Recurring;
import de.azapps.mirakel.sync.SyncAdapter;
import de.azapps.mirakel.sync.SyncAdapter.SYNC_STATE;
import de.azapps.tools.Log;

class TaskBase {
	public static final String		PRIORITY			= "priority";
	public static final String		CONTENT				= "content";
	public static final String		DUE					= "due";
	public static final String		DONE				= "done";
	public static final String		UUID				= "uuid";
	public static final String		LIST_ID				= "list_id";
	public static final String		REMINDER			= "reminder";
	public static final String		RECURRING			= "recurring";
	public static final String		RECURRING_REMINDER	= "recurring_reminder";
	public static final String		ADDITIONAL_ENTRIES	= "additional_entries";
	public static final String		PROGRESS			= "progress";

	private static final String		TAG					= "TaskBase";
	private long					id					= 0;
	private String					uuid				= "";
	private ListMirakel				list;
	private String					name;
	private String					content;
	private boolean					done;
	private Calendar				due;
	private int						priority;
	private Calendar				createdAt;
	private Calendar				updatedAt;
	protected Map<String, Boolean>	edited				= new HashMap<String, Boolean>();
	private Map<String, String>		additionalEntries	= null;
	private String					additionalEntriesString;
	private SYNC_STATE				sync_state;
	private Calendar				reminder;
	private int						recurrence;
	private int						recurring_reminder;
	private int						progress;

	TaskBase(long id, String uuid, ListMirakel list, String name, String content, boolean done, Calendar due, Calendar reminder, int priority, Calendar created_at, Calendar updated_at, SYNC_STATE sync_state, String additionalEntriesString, int recurring, int recurring_reminder, int progress) {
		this.id = id;
		this.uuid = uuid;
		this.setList(list);
		this.setName(name);
		this.setContent(content);
		this.setDone(done);
		this.setDue(due);
		this.setReminder(reminder);
		this.setPriority(priority);
		this.setCreatedAt(created_at);
		this.setUpdatedAt(updated_at);
		this.setSyncState(sync_state);
		this.additionalEntriesString = additionalEntriesString;
		this.recurrence = recurring;
		this.recurring_reminder = recurring_reminder;
		this.progress = progress;
	}

	TaskBase() {

	}

	TaskBase(String name) {
		this.id = 0;
		this.uuid = java.util.UUID.randomUUID().toString();
		this.setList(SpecialList.first());
		this.setName(name);
		this.setContent("");
		this.setDone(false);
		this.setDue(null);
		this.setReminder(null);
		this.setPriority(0);
		this.setCreatedAt((Calendar) null);
		this.setUpdatedAt((Calendar) null);
		this.setSyncState(SYNC_STATE.NOTHING);
		this.recurrence = -1;
		this.recurring_reminder = -1;
		this.progress = 0;
	}

	public Recurring getRecurring() {
		Recurring r = Recurring.get(recurrence);
		return r;
	}

	public int getRecurrenceId() {
		return recurrence;
	}

	public void setRecurrence(int recurring) {
		this.recurrence = recurring;
		edited.put(RECURRING, true);
	}

	public Recurring getRecurringReminder() {
		return Recurring.get(recurring_reminder);
	}

	public boolean hasRecurringReminder() {
		return recurring_reminder > 0;
	}

	public int getRecurringReminderId() {
		return recurring_reminder;
	}

	public void setRecurringReminder(int recurring) {
		this.recurring_reminder = recurring;
		edited.put(RECURRING_REMINDER, true);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
		edited.put(DatabaseHelper.ID, true);
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
		edited.put(UUID, true);
	}

	public ListMirakel getList() {
		return list;
	}

	public void setList(ListMirakel list) {
		this.list = list;
		edited.put(LIST_ID, true);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		edited.put(DatabaseHelper.NAME, true);
	}

	public String getContent() {
		if (content == null) return "";
		return content;
	}

	public void setContent(String content) {
		if (content != null) {
			this.content = StringUtils.replaceEach(content.trim(),
					new String[] { "\\n", "\\\"", "\b" }, new String[] { "\n",
							"\"", "" });
		} else {
			this.content = null;
		}
		edited.put(CONTENT, true);
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
		edited.put(DONE, true);
		if (done && recurrence != -1 && due != null) {
			if (getRecurring() != null) {
				due = getRecurring().addRecurring(due);
				if (reminder != null) {
					// Fix for #84
					// Update Reminder if set Task done
					reminder = getRecurring().addRecurring(reminder);
				}
				this.done = false;
			} else {
				Log.wtf(TAG, "Reccuring vanish");
			}
		} else if (done) {
			this.progress = 100;
		}
	}

	public void toggleDone() {
		setDone(!done);
	}

	public Calendar getDue() {
		return due;
	}

	public void setDue(Calendar due) {
		this.due = due;
		edited.put(DUE, true);
		if (due == null) {
			setRecurrence(-1);
		}
	}

	public Calendar getReminder() {
		return reminder;
	}

	public void setReminder(Calendar reminder) {
		this.reminder = reminder;
		edited.put(REMINDER, true);
		if (reminder == null) {
			setRecurringReminder(-1);
		}
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
		edited.put(PRIORITY, true);
	}

	public Calendar getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Calendar created_at) {
		this.createdAt = created_at;
	}

	public void setCreatedAt(String created_at) {
		try {
			setCreatedAt(DateTimeHelper.parseDateTime(created_at));
		} catch (ParseException e) {
			setCreatedAt((Calendar) null);
		}
	}

	public Calendar getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Calendar updated_at) {
		this.updatedAt = updated_at;
	}

	public void setUpdatedAt(String updated_at) {
		try {
			setUpdatedAt(DateTimeHelper.parseDateTime(updated_at));
		} catch (ParseException e) {
			setUpdatedAt((Calendar) null);
		}
	}

	public SYNC_STATE getSyncState() {
		return sync_state;
	}

	public void setSyncState(SYNC_STATE sync_state) {
		this.sync_state = sync_state;
	}

	public Map<String, Boolean> getEdited() {
		return edited;
	}

	public Map<String, String> getAdditionalEntries() {
		initAdditionalEntries();
		return additionalEntries;
	}

	public void setAdditionalEntries(Map<String, String> additionalEntries) {
		this.additionalEntries = additionalEntries;
		edited.put("additionalEntries", true);
	}

	public void addAdditionalEntry(String key, String value) {
		initAdditionalEntries();
		additionalEntries.put(key, value);
	}

	public void removeAdditionalEntry(String key) {
		initAdditionalEntries();
		additionalEntries.remove(key);
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		edited.put("progress", true);
		this.progress = progress;
	}

	boolean isEdited() {
		return edited.size() > 0;
	}

	boolean isEdited(String key) {
		return edited.containsKey(key);
	}

	void clearEdited() {
		edited.clear();
	}

	/**
	 * This function parses the additional fields only if it is necessary
	 */
	private void initAdditionalEntries() {
		if (additionalEntries == null) {
			if (additionalEntriesString == null
					|| additionalEntriesString.trim().equals("")
					|| additionalEntriesString.trim().equals("null")) {
				this.additionalEntries = new HashMap<String, String>();
			} else {
				Gson gson = new Gson();
				Type mapType = new TypeToken<Map<String, String>>() {}
						.getType();
				this.additionalEntries = gson.fromJson(additionalEntriesString,
						mapType);
			}
		}
	}

	@Override
	public String toString() {
		return name;
	}

	public ContentValues getContentValues() throws NoSuchListException {
		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.ID, id);
		cv.put(UUID, uuid);
		if (list == null) {
			// If the list of the task vanished, we should move the task in some
			// list so we can do something with it.
			list = ListMirakel.first();
			// Bad things happened… Now we can throw our beloved exception
			if (list == null) throw new NoSuchListException();
		}
		cv.put(LIST_ID, list.getId());
		cv.put(DatabaseHelper.NAME, name);
		cv.put(CONTENT, content);
		cv.put(DONE, done);
		String due = (this.due == null ? null : DateTimeHelper
				.formatDate(getDue()));
		cv.put(DUE, due);
		String reminder = null;
		if (this.reminder != null)
			reminder = DateTimeHelper.formatDateTime(this.reminder);
		cv.put(REMINDER, reminder);
		cv.put(PRIORITY, priority);
		String createdAt = null;
		if (this.createdAt != null)
			createdAt = DateTimeHelper.formatDateTime(this.createdAt);
		cv.put(DatabaseHelper.CREATED_AT, createdAt);
		String updatedAt = null;
		if (this.updatedAt != null)
			updatedAt = DateTimeHelper.formatDateTime(this.updatedAt);
		cv.put(DatabaseHelper.UPDATED_AT, updatedAt);
		cv.put(SyncAdapter.SYNC_STATE, sync_state.toInt());
		cv.put(RECURRING, recurrence);
		cv.put(RECURRING_REMINDER, recurring_reminder);
		cv.put(PROGRESS, progress);

		Gson gson = new GsonBuilder().create();
		String additionalEntries = gson.toJson(this.additionalEntries);
		cv.put("additional_entries", additionalEntries);
		return cv;
	}

}
