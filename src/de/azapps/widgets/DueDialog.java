package de.azapps.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.NumberPicker;
import de.azapps.mirakelandroid.R;

public class DueDialog extends AlertDialog {
	public enum VALUE {
		DAY, HOUR, MINUTE, MONTH, YEAR;

		public int getInt() {
			switch (this) {
				case DAY:
					return 0;
				case MONTH:
					return 1;
				case YEAR:
					return 2;
				case MINUTE:
					return 3;
				case HOUR:
					return 4;
				default:
					break;
			}
			return 0;
		}
	}
	private int			count;
	private final Context		ctx;
	private VALUE		dayYear	= VALUE.DAY;
	private final View		dialogView;

	private final String[]	s;

	public DueDialog(Context context, final boolean minuteHour) {
		super(context);
		this.ctx = context;
		this.s = new String[100];
		for (int i = 0; i < this.s.length; i++) {
			this.s[i] = (i > 10 ? "+" : "") + (i - 10) + "";
		}

		this.dialogView = getNumericPicker();
		final NumberPicker pickerDay = (NumberPicker) this.dialogView
				.findViewById(R.id.due_day_year);
		NumberPicker pickerValue = (NumberPicker) this.dialogView
				.findViewById(R.id.due_val);
		String dayYearValues[] = getDayYearValues(0, minuteHour);

		pickerDay.setDisplayedValues(dayYearValues);
		pickerDay.setMaxValue(dayYearValues.length - 1);
		pickerValue.setMaxValue(this.s.length - 1);
		pickerValue.setValue(10);
		pickerValue.setMinValue(0);
		pickerValue.setDisplayedValues(this.s);
		pickerValue
		.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		pickerDay
		.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		pickerValue.setWrapSelectorWheel(false);
		pickerValue
		.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				pickerDay.setDisplayedValues(getDayYearValues(
						newVal - 10, minuteHour));
				DueDialog.this.count = newVal - 10;
			}
		});
		pickerDay
		.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				switch (newVal) {
					case 0:
						DueDialog.this.dayYear = VALUE.DAY;
						break;
					case 1:
						DueDialog.this.dayYear = VALUE.MONTH;
						break;
					case 2:
						DueDialog.this.dayYear = VALUE.YEAR;
						break;
					default:
						break;
				}

			}
		});

		setView(this.dialogView);

	}

	public VALUE getDayYear() {
		return this.dayYear;
	}

	protected String[] getDayYearValues(int newVal, boolean minutesHour) {
		int size = minutesHour ? 5 : 3;
		int i = 0;
		String[] ret = new String[size];
		if (minutesHour) {
			ret[i++] = this.ctx.getResources().getQuantityString(
					R.plurals.due_minute, newVal);
			ret[i++] = this.ctx.getResources().getQuantityString(R.plurals.due_hour,
					newVal);
		}
		ret[i++] = this.ctx.getResources().getQuantityString(R.plurals.due_day,
				newVal);
		ret[i++] = this.ctx.getResources().getQuantityString(R.plurals.due_month,
				newVal);
		ret[i] = this.ctx.getResources().getQuantityString(R.plurals.due_year,
				newVal);

		return ret;
	}

	protected View getNumericPicker() {
		return getLayoutInflater().inflate(R.layout.due_dialog, null);
	}

	public int getValue() {
		return this.count;
	}

	public void setNegativeButton(int textId, OnClickListener onCancel) {
		setButton(BUTTON_NEGATIVE, this.ctx.getString(textId), onCancel);

	}

	public void setNeutralButton(int textId, OnClickListener onCancel) {
		setButton(BUTTON_NEUTRAL, this.ctx.getString(textId), onCancel);

	}

	public void setPositiveButton(int textId, OnClickListener onCancel) {
		setButton(BUTTON_POSITIVE, this.ctx.getString(textId), onCancel);

	}

	public void setValue(int val, VALUE day) {
		int _day = this.dayYear.getInt();
		((NumberPicker) this.dialogView.findViewById(R.id.due_day_year))
		.setValue(_day);;
		((NumberPicker) this.dialogView.findViewById(R.id.due_val))
		.setValue(val + 10);
	}

	protected String updateDayYear() {
		switch (this.dayYear) {
			case MINUTE:
				return this.ctx.getResources().getQuantityString(
						R.plurals.due_minute, this.count);
			case HOUR:
				return this.ctx.getResources().getQuantityString(R.plurals.due_hour,
						this.count);
			case DAY:
				return this.ctx.getResources().getQuantityString(R.plurals.due_day,
						this.count);
			case MONTH:
				return this.ctx.getResources().getQuantityString(
						R.plurals.due_month, this.count);
			case YEAR:
				return this.ctx.getResources().getQuantityString(R.plurals.due_year,
						this.count);
			default:
				break;
		}
		return "";

	}

}
