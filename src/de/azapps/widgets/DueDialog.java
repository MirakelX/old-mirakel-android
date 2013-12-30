package de.azapps.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.NumberPicker;
import de.azapps.mirakelandroid.R;

public class DueDialog extends AlertDialog {
	private Context		ctx;
	private VALUE		dayYear	= VALUE.DAY;
	private int			count;
	private View		dialogView;
	private String[]	s;

	public enum VALUE {
		MINUTE, HOUR, DAY, MONTH, YEAR;

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
			}
			return 0;
		}
	}

	public void setNegativeButton(int textId, OnClickListener onCancel) {
		setButton(BUTTON_NEGATIVE, ctx.getString(textId), onCancel);

	}

	public void setPositiveButton(int textId, OnClickListener onCancel) {
		setButton(BUTTON_POSITIVE, ctx.getString(textId), onCancel);

	}

	public void setNeutralButton(int textId, OnClickListener onCancel) {
		setButton(BUTTON_NEUTRAL, ctx.getString(textId), onCancel);

	}

	public DueDialog(Context context, final boolean minuteHour) {
		super(context);
		ctx = context;
		s = new String[100];
		for (int i = 0; i < s.length; i++) {
			s[i] = (i > 10 ? "+" : "") + (i - 10) + "";
		}

		dialogView = getNumericPicker();
		final NumberPicker pickerDay = ((NumberPicker) dialogView
				.findViewById(R.id.due_day_year));
		NumberPicker pickerValue = ((NumberPicker) dialogView
				.findViewById(R.id.due_val));
		String dayYearValues[] = getDayYearValues(0, minuteHour);

		pickerDay.setDisplayedValues(dayYearValues);
		pickerDay.setMaxValue(dayYearValues.length - 1);
		pickerValue.setMaxValue(s.length - 1);
		pickerValue.setValue(10);
		pickerValue.setMinValue(0);
		pickerValue.setDisplayedValues(s);
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
						count = newVal - 10;
					}
				});
		pickerDay
				.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

					@Override
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						switch (newVal) {
							case 0:
								dayYear = VALUE.DAY;
								break;
							case 1:
								dayYear = VALUE.MONTH;
								break;
							case 2:
								dayYear = VALUE.YEAR;
								break;
						}

					}
				});

		setView(dialogView);

	}

	protected String updateDayYear() {
		switch (dayYear) {
			case MINUTE:
				return ctx.getResources().getQuantityString(
						R.plurals.due_minute, count);
			case HOUR:
				return ctx.getResources().getQuantityString(R.plurals.due_hour,
						count);
			case DAY:
				return ctx.getResources().getQuantityString(R.plurals.due_day,
						count);
			case MONTH:
				return ctx.getResources().getQuantityString(
						R.plurals.due_month, count);
			case YEAR:
				return ctx.getResources().getQuantityString(R.plurals.due_year,
						count);
		}
		return "";

	}

	protected String[] getDayYearValues(int newVal, boolean minutesHour) {
		int size = minutesHour ? 5 : 3;
		int i = 0;
		String[] ret = new String[size];
		if (minutesHour) {
			ret[i++] = ctx.getResources().getQuantityString(
					R.plurals.due_minute, newVal);
			ret[i++] = ctx.getResources().getQuantityString(R.plurals.due_hour,
					newVal);
		}
		ret[i++] = ctx.getResources().getQuantityString(R.plurals.due_day,
				newVal);
		ret[i++] = ctx.getResources().getQuantityString(R.plurals.due_month,
				newVal);
		ret[i] = ctx.getResources().getQuantityString(R.plurals.due_year,
				newVal);

		return ret;
	}

	protected View getNumericPicker() {
		return getLayoutInflater().inflate(R.layout.due_dialog, null);
	}

	public void setValue(int val, VALUE day) {
		int _day = dayYear.getInt();
		((NumberPicker) dialogView.findViewById(R.id.due_day_year))
				.setValue(_day);;
		((NumberPicker) dialogView.findViewById(R.id.due_val))
				.setValue(val + 10);
	}

	public int getValue() {
		return count;
	}

	public VALUE getDayYear() {
		return dayYear;
	}

}
