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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import de.azapps.mirakel.model.task.Task;
import de.azapps.mirakelandroid.R;
import de.azapps.tools.Log;

public class TaskDetailProgressBar extends TaskDetailSubListBase<Integer> {

	private static final String	TAG	= "TaskDetailProgressBar";
	private final SeekBar	progress;

	public TaskDetailProgressBar(Context ctx) {
		super(ctx);
		inflate(ctx, R.layout.task_progress, this);
		this.progress = (SeekBar) findViewById(R.id.task_progress_seekbar);
		this.progress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progressLocal, boolean fromUser) {}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (TaskDetailProgressBar.this.task != null) {
					TaskDetailProgressBar.this.task.setProgress(seekBar
							.getProgress());
					save();
				}

			}
		});
		this.progress.setMax(100);
	}

	public void setTask(Task t) {
		this.task = t;
	}


	@Override
	public void updatePart(Integer newValue) {
		Log.d(TAG, "update " + newValue);
		this.progress.setProgress(newValue);
	}

}
