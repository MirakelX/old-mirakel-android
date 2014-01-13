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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.TextView;
import de.azapps.mirakel.helper.MirakelPreferences;
import de.azapps.mirakel.model.file.FileMirakel;
import de.azapps.mirakelandroid.R;

public class TaskDetailFilePart extends TaskDetailSubListBase<FileMirakel> {

	private final Context	ctx;
	private final ImageView	fileImage;
	private final TextView	fileName;
	private final TextView	filePath;

	public TaskDetailFilePart(Context context) {
		super(context);
		this.ctx=context;
		inflate(context, R.layout.files_row, this);
		this.fileImage = (ImageView) findViewById(R.id.file_image);
		this.fileName = (TextView) findViewById(R.id.file_name);
		this.filePath = (TextView) findViewById(R.id.file_path);
	}


	@Override
	public void updatePart(final FileMirakel file) {
		new Thread(new Runnable() {
			private Bitmap	preview;

			@Override
			public void run() {
				this.preview = file.getPreview();
				if (file.getPath().endsWith(".mp3")) {
					int resource_id = MirakelPreferences.isDark() ? R.drawable.ic_action_play_dark
							: R.drawable.ic_action_play;
					this.preview = BitmapFactory.decodeResource(
							TaskDetailFilePart.this.ctx.getResources(),
							resource_id);
				}
				if (this.preview != null) {
					TaskDetailFilePart.this.fileImage.post(new Runnable() {
						@Override
						public void run() {
							TaskDetailFilePart.this.fileImage
							.setImageBitmap(preview);

						}
					});
				} else {
					TaskDetailFilePart.this.fileImage.post(new Runnable() {
						@Override
						public void run() {
							LayoutParams params = (LayoutParams) TaskDetailFilePart.this.fileImage
									.getLayoutParams();
							params.height = 0;
							TaskDetailFilePart.this.fileImage
							.setLayoutParams(params);

						}
					});
				}
			}
		}).start();
		if (file.getPath().endsWith(".mp3")
				&& file.getName().startsWith("AUD_")) {
			this.fileName.setText(R.string.audio_record_file);
		} else if (file.getName().endsWith(".jpg")) {
			this.fileName.setText(R.string.image_file);
		} else {
			this.fileName.setText(file.getName());
		}
		this.filePath.setText(file.getPath());
		if (!file.getFile().exists()) {
			this.filePath.setText(R.string.file_vanished);
		} else {
			this.filePath.setText(file.getPath());
		}
	}

}
