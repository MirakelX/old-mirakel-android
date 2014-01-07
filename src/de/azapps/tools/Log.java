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
package de.azapps.tools;

import de.azapps.mirakel.helper.BuildHelper;


public class Log {
	public static void d(String tag, String msg) {
		if (tag == null || msg == null)
			return;
		if (BuildHelper.isDebug()) {
			android.util.Log.d(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (tag == null || msg == null)
			return;
		android.util.Log.e(tag, msg);
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (tag == null || msg == null)
			return;
		android.util.Log.e(tag, msg, tr);
	}

	public static String getStackTraceString(Throwable tr) {
		return android.util.Log.getStackTraceString(tr);
	}

	public static void i(String tag, String msg) {
		if (tag == null || msg == null)
			return;
		if (BuildHelper.isDebug()) {
			android.util.Log.i(tag, msg);
		}
	}

	public static void v(String tag, String msg) {
		if (tag == null || msg == null)
			return;
		if (BuildHelper.isDebug()) {
			android.util.Log.v(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (tag == null || msg == null)
			return;
		if (BuildHelper.isDebug()) {
			android.util.Log.w(tag, msg);
		}
	}

	public static void wtf(String tag, String msg) {
		if (tag == null || msg == null)
			return;
		android.util.Log.wtf(tag, msg);
	}
}
