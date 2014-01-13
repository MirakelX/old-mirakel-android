package de.azapps.mirakel.model.list;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import de.azapps.mirakel.Mirakel;
import de.azapps.mirakel.model.DatabaseHelper;
import de.azapps.mirakel.model.account.AccountMirakel;
import de.azapps.mirakel.model.task.Task;
import de.azapps.mirakel.sync.SyncAdapter;
import de.azapps.mirakel.sync.SyncAdapter.SYNC_STATE;
import de.azapps.mirakelandroid.R;

public class SpecialList extends ListMirakel {
	private boolean active;
	private String whereQuery;
	private ListMirakel defaultList;
	private Integer defaultDate;

	public boolean isSpecialList() {
		return true;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getWhereQuery(boolean forQuery) {
		if (forQuery) {
			String tmpWhere = whereQuery;
			Pattern p = Pattern.compile(Task.LIST_ID + " in[(]([^)]*)[)]");
			Matcher m = p.matcher(whereQuery);

			if (m.find()) {
				String origQuery = m.group(1);
				String newQuery = "";
				String lists[] = origQuery.split(",");
				String listConditions = "";
				boolean first = true;
				for (String list : lists) {
					int listId = Integer.valueOf(list);
					if (listId > 0) {
						if (first)
							first = false;
						else
							newQuery += ",";

						newQuery += list;
						continue;
					}
					listConditions += " OR ("
							+ SpecialList.getSpecialList(-listId)
									.getWhereQuery(true) + ")";
				}
				if (!listConditions.equals("")) {
					tmpWhere = m.replaceFirst("(" + Task.LIST_ID + " in ("
							+ newQuery + ")");
					tmpWhere += listConditions + ")";
				}
			}
			return tmpWhere;
		} 
		return whereQuery;
	}

	public void setWhereQuery(String whereQuery) {
		this.whereQuery = whereQuery;
	}

	public ListMirakel getDefaultList() {
		if (defaultList == null) {
			return ListMirakel.first();
		}
		return defaultList;
	}

	public void setDefaultList(ListMirakel defaultList) {
		this.defaultList = defaultList;
	}

	public Integer getDefaultDate() {
		return defaultDate;
	}

	public void setDefaultDate(Integer defaultDate) {
		this.defaultDate = defaultDate;
	}

	SpecialList(int id, String name, String whereQuery, boolean active,
			ListMirakel listMirakel, Integer defaultDate, short sort_by,
			SYNC_STATE sync_state, int color, int lft, int rgt) {

		super(-id, name, sort_by, "", "", sync_state, 0, 0, color,
				AccountMirakel.getLocal());
		this.active = active;
		this.whereQuery = whereQuery;
		this.defaultList = listMirakel;
		this.defaultDate = defaultDate;
		setLft(lft);
		setRgt(rgt);
	}

	/**
	 * Get all Tasks
	 * 
	 * @return
	 */
	public List<Task> tasks() {
		return Task.getTasks(this, getSortBy(), false, getWhereQuery(true));
	}

	/**
	 * Get all Tasks
	 * 
	 * @param showDone
	 * @return
	 */
	public List<Task> tasks(boolean showDone) {
		return Task.getTasks(this, getSortBy(), showDone, getWhereQuery(true));
	}

	// Static Methods
	public static final String TABLE = "special_lists";
	// private static final String TAG = "TasksDataSource";
	private static SQLiteDatabase database;
	private static DatabaseHelper dbHelper;
	public static final String WHERE_QUERY = "whereQuery";
	public static final String ACTIVE = "active";
	public static final String DEFAULT_LIST = "def_list";
	public static final String DEFAULT_DUE = "def_date";
	private static final String[] allColumns = { DatabaseHelper.ID,
			DatabaseHelper.NAME, WHERE_QUERY, ACTIVE, DEFAULT_LIST,
			DEFAULT_DUE, SORT_BY, SyncAdapter.SYNC_STATE, COLOR, LFT, RGT };

	/**
	 * Initialize the Database and the preferences
	 * 
	 * @param context
	 *            The Application-Context
	 */
	public static void init(Context context) {
		dbHelper = new DatabaseHelper(context);
		database = dbHelper.getWritableDatabase();
	}

	/**
	 * Close the Database-Connection
	 */
	public static void close() {
		dbHelper.close();
	}

	public static SpecialList newSpecialList(String name, String whereQuery,
			boolean active, Context context) {
		int listId = ListMirakel.safeFirst(context).getId();
		database.beginTransaction();

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.NAME, name);
		values.put(WHERE_QUERY, whereQuery);
		values.put(ACTIVE, active);
		values.put(DEFAULT_LIST, listId);
		long insertId = database.insert(TABLE, null, values);
		Cursor cursor = database.query(TABLE, allColumns, "_id = " + insertId,
				null, null, null, null);
		cursor.moveToFirst();
		database.execSQL("update " + TABLE + " SET " + LFT + "=(SELECT MAX("
				+ RGT + ") from " + TABLE + ")+1, " + RGT + "=(SELECT MAX("
				+ RGT + ") from " + TABLE + ")+2 where " + DatabaseHelper.ID
				+ "=" + insertId);
		database.setTransactionSuccessful();
		database.endTransaction();
		SpecialList newSList = cursorToSList(cursor);
		cursor.close();
		return newSList;
	}

	/**
	 * Update the List in the Database
	 * 
	 * @param list
	 *            The List
	 */
	public void save() {
		database.beginTransaction();
		setSyncState(getSyncState() == SYNC_STATE.ADD
				|| getSyncState() == SYNC_STATE.IS_SYNCED ? getSyncState()
				: SYNC_STATE.NEED_SYNC);
		ContentValues values = getContentValues();
		database.update(TABLE, values,
				DatabaseHelper.ID + " = " + Math.abs(getId()), null);
		database.setTransactionSuccessful();
		database.endTransaction();
	}

	/**
	 * Delete a List from the Database
	 * 
	 * @param list
	 */
	public void destroy() {
		database.beginTransaction();
		long id = Math.abs(getId());

		if (getSyncState() != SYNC_STATE.ADD) {
			setSyncState(SYNC_STATE.DELETE);
			setActive(false);
			ContentValues values = new ContentValues();
			values.put(SyncAdapter.SYNC_STATE, getSyncState().toInt());
			database.update(TABLE, values, DatabaseHelper.ID + "=" + id, null);
		} else {
			database.delete(TABLE, DatabaseHelper.ID + "=" + id, null);
		}
		database.rawQuery("UPDATE " + TABLE + " SET " + LFT + "=" + LFT
				+ "-2 WHERE " + LFT + ">" + getLft() + "; UPDATE " + TABLE
				+ " SET " + RGT + "=" + RGT + "-2 WHERE " + RGT + ">"
				+ getRgt() + ";", null);
		database.setTransactionSuccessful();
		database.endTransaction();

	}

	public ContentValues getContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.NAME, getName());
		cv.put(SORT_BY, getSortBy());
		cv.put(SyncAdapter.SYNC_STATE, getSyncState().toInt());
		cv.put(ACTIVE, isActive() ? 1 : 0);
		cv.put(WHERE_QUERY, getWhereQuery(false));
		cv.put(DEFAULT_LIST, defaultList == null ? null : defaultList.getId());
		cv.put(DEFAULT_DUE, defaultDate);
		cv.put(COLOR, getColor());
		cv.put(LFT, getLft());
		cv.put(RGT, getRgt());
		return cv;
	}

	/**
	 * Get all SpecialLists
	 * 
	 * @return
	 */
	public static List<SpecialList> allSpecial() {
		return allSpecial(false);
	}

	/**
	 * Get all SpecialLists
	 * 
	 * @return
	 */
	public static List<SpecialList> allSpecial(boolean showAll) {
		List<SpecialList> slists = new ArrayList<SpecialList>();
		Cursor c = database.query(TABLE, allColumns, showAll ? "" : ACTIVE
				+ "=1", null, null, null, LFT + " ASC");
		c.moveToFirst();
		while (!c.isAfterLast()) {
			slists.add(cursorToSList(c));
			c.moveToNext();
		}
		c.close();
		return slists;
	}

	/**
	 * Get a List by id selectionArgs
	 * 
	 * @param listId
	 *            List–ID
	 * @return List
	 */
	public static SpecialList getSpecialList(int listId) {
		Cursor cursor = database.query(SpecialList.TABLE, allColumns,
				DatabaseHelper.ID + "=" + listId, null, null, null, null);
		cursor.moveToFirst();
		if (cursor.getCount() != 0) {
			SpecialList t = cursorToSList(cursor);
			cursor.close();
			return t;
		}
		cursor.close();
		return firstSpecial();
	}

	/**
	 * Get the first List
	 * 
	 * @return List
	 */
	public static SpecialList firstSpecial() {
		Cursor cursor = database.query(SpecialList.TABLE, allColumns, "not "
				+ SyncAdapter.SYNC_STATE + "=" + SYNC_STATE.DELETE, null, null,
				null, LFT + " ASC");
		SpecialList list = null;
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			list = cursorToSList(cursor);
			cursor.moveToNext();
		}
		cursor.close();
		return list;
	}

	public static SpecialList firstSpecialSafe(Context ctx) {
		SpecialList s = SpecialList.firstSpecial();
		if (s == null) {
			s = SpecialList.newSpecialList(ctx.getString(R.string.list_all),
					"", true, ctx);
			if (ListMirakel.count() == 0)
				ListMirakel.safeFirst(ctx);
			s.save(false);
		}
		return s;
	}

	/**
	 * Create a List from a Cursor
	 * 
	 * @param cursor
	 * @return
	 */
	private static SpecialList cursorToSList(Cursor cursor) {
		int i = 0;
		Integer defDate = cursor.getInt(5);
		if (cursor.isNull(5))
			defDate = null;
		SpecialList slist = new SpecialList(cursor.getInt(i++),
				cursor.getString(i++), cursor.getString(i++),
				cursor.getInt(i++) == 1,
				ListMirakel.getList(cursor.getInt(i++)), defDate,
				(short) cursor.getInt(++i), SYNC_STATE.parseInt(cursor
						.getInt(++i)), cursor.getInt(++i), cursor.getInt(++i),
				cursor.getInt(++i));
		return slist;
	}

	public static int getSpecialListCount() {
		Cursor c = Mirakel.getReadableDatabase().rawQuery(
				"Select count(" + DatabaseHelper.ID + ") from " + TABLE, null);
		c.moveToFirst();
		int r = 0;
		if (c.getCount() > 0) {
			r = c.getInt(0);
		}
		c.close();
		return r;
	}

}
