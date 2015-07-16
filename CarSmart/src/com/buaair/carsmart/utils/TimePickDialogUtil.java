package com.buaair.carsmart.utils;

import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.buaair.carsmart.R;

@SuppressLint({ "InflateParams", "DefaultLocale" })
public class TimePickDialogUtil implements OnDateChangedListener, OnTimeChangedListener{

	private Activity activity;
	private DatePicker datePicker;
	private TimePicker timePickerBegin,timePickerEnd;
	
	private AlertDialog alertDialog;
	
	private OnDateTimeChangedListener onDateTimeChangedListener;
	
	
	public TimePickDialogUtil(Activity activity, OnDateTimeChangedListener onDateTimeChangedListener) {
		this.onDateTimeChangedListener = onDateTimeChangedListener;
		this.activity = activity;
	}
	
	public interface OnDateTimeChangedListener{
		void onDateTimeChanged(Date beginDate, Date endDate);
	}
	
	/**
	 * 弹出日期时间选择框方法
	 * 
	 * @param inputDate
	 *            :为需要设置的日期时间文本编辑框
	 * @return
	 */
	public AlertDialog showTimePicKDialog(Date beginDate, Date endDate) {
		
		LinearLayout dateTimeLayout = (LinearLayout) activity
				.getLayoutInflater().inflate(R.layout.dialog_time_picker, null);
		
		alertDialog = new AlertDialog.Builder(activity)
		.setTitle("选择回放的时间段")
		.setView(dateTimeLayout)
		.setPositiveButton("设置", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				int year = datePicker.getYear();
				int monthOfYear = datePicker.getMonth();
				int dayOfMonth = datePicker.getDayOfMonth();
				int currentHourBegin = timePickerBegin.getCurrentHour();
				int currentMinuteBegin = timePickerBegin.getCurrentMinute();
				int currentHourEnd = timePickerEnd.getCurrentHour();
				int currentMinuteEnd = timePickerEnd.getCurrentMinute();
				Calendar calendar = Calendar.getInstance();
				calendar.set(year, monthOfYear, dayOfMonth, currentHourBegin, currentMinuteBegin);
				Date beginDate = calendar.getTime();
				calendar.set(year, monthOfYear, dayOfMonth, currentHourEnd, currentMinuteEnd);
				Date endDate = calendar.getTime();
				onDateTimeChangedListener.onDateTimeChanged(beginDate, endDate);
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		}).show();
		
		
		datePicker = (DatePicker) dateTimeLayout.findViewById(R.id.date_picker);
		timePickerBegin = (TimePicker) dateTimeLayout.findViewById(R.id.time_picker_begin);
		timePickerEnd = (TimePicker) dateTimeLayout.findViewById(R.id.time_picker_end);
		timePickerBegin.setIs24HourView(true);
		timePickerBegin.setOnTimeChangedListener(this);
		timePickerEnd.setIs24HourView(true);
		timePickerEnd.setOnTimeChangedListener(this);
		
		Calendar begin = DateUtil.date2Calendar(beginDate);
		Calendar end = DateUtil.date2Calendar(endDate);

		
		datePicker.init(begin.get(Calendar.YEAR), begin.get(Calendar.MONTH), begin.get(Calendar.DAY_OF_MONTH), this);

		timePickerBegin.setCurrentHour(begin.get(Calendar.HOUR_OF_DAY));
		timePickerBegin.setCurrentMinute(begin.get(Calendar.MINUTE));
		
		timePickerEnd.setCurrentHour(end.get(Calendar.HOUR_OF_DAY));
		timePickerEnd.setCurrentMinute(end.get(Calendar.MINUTE));
		
		
		return alertDialog;
	}
	
	

	@Override
	public void onDateChanged(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		setTitle();
	}

	@Override
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		
		switch (view.getId()) {
		case R.id.time_picker_begin:
			setTitle();
			break;
		case R.id.time_picker_end:
			setTitle();
			break;
		default:
			break;
		}
		
	}
	
	private void setTitle(){
		int year = datePicker.getYear();
		int monthOfYear = datePicker.getMonth();
		int dayOfMonth = datePicker.getDayOfMonth();
		int currentHourBegin = timePickerBegin.getCurrentHour();
		int currentMinuteBegin = timePickerBegin.getCurrentMinute();
		int currentHourEnd = timePickerEnd.getCurrentHour();
		int currentMinuteEnd = timePickerEnd.getCurrentMinute();
		StringBuilder sb = new StringBuilder();
		sb.append(year).append("年").append(monthOfYear+1).append("月").append(format(dayOfMonth)).append("日   ");
		sb.append(format(currentHourBegin)).append(":").append(format(currentMinuteBegin));
		sb.append("至");
		sb.append(format(currentHourEnd)).append(":").append(format(currentMinuteEnd));
		if(alertDialog != null){
			alertDialog.setTitle(sb.toString());
		}
	}
	
	private String format(Integer integer){
		return String.format("%02d", integer);
	}
}
