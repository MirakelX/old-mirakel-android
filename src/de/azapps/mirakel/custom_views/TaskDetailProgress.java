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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import de.azapps.mirakel.model.task.Task;
import de.azapps.mirakelandroid.R;

public class TaskDetailProgress extends TaskDetailSubtitleView<Integer, TaskDetailProgressBar> {

	public TaskDetailProgress(Context ctx) {
		super(ctx);
		this.title.setText(R.string.task_fragment_progress);
		this.audioButton.setVisibility(GONE);
		this.cameraButton.setVisibility(GONE);
		this.button.setVisibility(GONE);
	}

	@Override
	TaskDetailProgressBar newElement() {
		TaskDetailProgressBar t = new TaskDetailProgressBar(this.context);
		t.setTask(this.task);
		return t;
	}

	@Override
	void update(Task t) {
		this.task=t;
		List<Integer> l=new ArrayList<Integer>();
		l.add(t.getProgress());
		updateSubviews(l);

	}

}
