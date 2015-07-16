package com.buaair.carsmart.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import antistatic.spinnerwheel.WheelVerticalView;
import antistatic.spinnerwheel.adapters.ArrayWheelAdapter;
import antistatic.spinnerwheel.adapters.NumericWheelAdapter;

import com.buaair.carsmart.R;
import com.buaair.carsmart.entity.Command;
import com.buaair.carsmart.utils.HttpClientUtil;
import com.buaair.carsmart.utils.JsonUtil;
import com.buaair.carsmart.utils.Util;
import com.buaair.carsmart.vo.CommandResponseVO;
import com.kyleduo.switchbutton.Configuration;
import com.kyleduo.switchbutton.SwitchButton;

@SuppressLint("InflateParams")
public class CommandFragment extends Fragment implements OnClickListener,OnCheckedChangeListener{

	private Context mContext;

	private GetDataTask getDataTask;
	
	private View parentView;
	private View progressView;
	
	private TextView tvImei, tvOperateStatue,tvOperateTime,tvOperateUser,tvSendDetail,tvWorkModel,
					 tvTimer,tvPositionCount,tvIntervalTime;
	
	private SwitchButton sbWorkStatus;
	
	private View lbPositionCount,lbIntervalTime,lbIntervalTimeLine; 
	
	/**
	 * 设备imei号
	 */
	private String imei = null;
	
	/**
	 * 激活状态  0-不设置 3-已激活
	 */
	private int workStatue = 0;
	
	/**
	 * 1:标准；2：精准，3：追车 默认为2
	 */
	private int workModel = 2;
	
	/**
	 * （0-24）对应（1：00-00：00），0为不设置此值 默认为0
	 */
	private int timer = 0;
	
	/**
	 * 定位次数(标准、精准模式下有效) 默认为1
	 */
	private int postionCount = 1; 
	
	/**
	 * 追车间隔时间单位为秒（追车模式下有效） 默认为600
	 */
	private int intervalTime = 6;

	private String[] workModels = {"标准","精确","追车"}; 
	private String[] timers = {"不设置","01:00","02:00","03:00","04:00","05:00","06:00","07:00","08:00","09:00","10:00","11:00","12:00",
									  "13:00","14:00","15:00","16:00","17:00","18:00","19:00","20:00","21:00","22:00","23:00","24:00"};
	
	private void setUpView(){
		tvImei = (TextView) parentView.findViewById(R.id.tv_imei);
		tvOperateStatue = (TextView) parentView.findViewById(R.id.tv_operate_statue);
		tvOperateTime = (TextView) parentView.findViewById(R.id.tv_operate_time);
		tvOperateUser = (TextView) parentView.findViewById(R.id.tv_operate_user);
		tvSendDetail = (TextView) parentView.findViewById(R.id.tv_send_detail);
		tvWorkModel = (TextView) parentView.findViewById(R.id.tv_work_model);
		tvTimer = (TextView) parentView.findViewById(R.id.tv_timer);
		tvPositionCount = (TextView) parentView.findViewById(R.id.tv_postion_count);
		tvIntervalTime = (TextView) parentView.findViewById(R.id.tv_interval_time);
		
		sbWorkStatus = (SwitchButton) parentView.findViewById(R.id.sb_work_status);
		sbWorkStatus.setOnCheckedChangeListener(this);
		Configuration conf = Configuration.getDefault(getResources().getDisplayMetrics().density);
		conf.setThumbMargin(2);
		conf.setVelocity(8);
		conf.setThumbWidthAndHeight(26, 16);
		conf.setRadius(6);
		conf.setMeasureFactor(2f);
		sbWorkStatus.setConfiguration(conf);
		
		
		progressView = parentView.findViewById(R.id.progress_view);
		
		parentView.findViewById(R.id.lb_work_model).setOnClickListener(this);
		parentView.findViewById(R.id.lb_timer).setOnClickListener(this);
		
		lbPositionCount = parentView.findViewById(R.id.lb_position_count);
		lbPositionCount.setOnClickListener(this);
		lbIntervalTimeLine = parentView.findViewById(R.id.lb_interval_time_line);
		lbIntervalTime = parentView.findViewById(R.id.lb_interval_time);
		lbIntervalTime.setOnClickListener(this);
		
		tvWorkModel.setText(workModels[workModel-1]);
		tvPositionCount.setText(postionCount+"");
		tvIntervalTime.setText(intervalTime+"");
		tvTimer.setText(timers[timer]);
		
		setViewVisible();
	}
	
	private void setViewVisible(){
		lbIntervalTime.setVisibility(workModel == 3 ? View.VISIBLE : View.GONE);
		lbIntervalTimeLine.setVisibility(workModel == 3 ? View.VISIBLE : View.GONE);
		lbPositionCount.setVisibility(workModel == 3 ? View.GONE : View.VISIBLE);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = this.getActivity().getApplicationContext();
		parentView = inflater.inflate(R.layout.fragment_command1, container, false);
		setUpView();
		return parentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		

		imei = getArguments().getString("imei");
		
		getData(CommandType.GETLAST);
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.command, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_send_command:
			getData(CommandType.SEND);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lb_work_model:
//			String workModelStr = tvWorkModel.getText().toString();
//			int workModelIndex = 0;
//			for (int i = 0; i < workModels.length; i++) {
//				if(workModels[i].equals(workModelStr)){
//					workModelIndex = i;
//					break;
//				}
//			}
			showWorkModelAlertDialog(workModel-1);
			break;
		case R.id.lb_timer:
			showTimerAlertDialog(timer);
			break;
		case R.id.lb_position_count:
			showPositionCountAlertDialog(postionCount-1);
			break;
		case R.id.lb_interval_time:
			showIntervalTimeAlertDialog(intervalTime-1);
			break;
		default:
			break;
		}
	}
	
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(workStatue == 0){
			workStatue = 3;
		}else{
			workStatue = 0;
		}
	}

	private void showWorkModelAlertDialog(int index){
		new AlertDialog.Builder(this.getActivity())  
	    .setTitle(R.string.command_work_model)  
	    .setSingleChoiceItems(workModels, index,   
	      new DialogInterface.OnClickListener() {  
	         public void onClick(DialogInterface dialog, int which) {
	        	workModel = which + 1;
	        	tvWorkModel.setText(workModels[workModel-1]);
	        	setViewVisible();
	            dialog.dismiss();
	         }  
	      }  
	    ).setNegativeButton(R.string.cancel, null)
	    .show();
	}
	 
	private void showTimerAlertDialog(int index){
		final WheelVerticalView wheelVerticalView = getWheelVerticalView();
		wheelVerticalView.setVisibleItems(5);
        ArrayWheelAdapter<String> adapter = new ArrayWheelAdapter<String>(mContext, timers);
//		NumericWheelAdapter adapter = new NumericWheelAdapter(mContext, 1, 24, "%02d:00");
		adapter.setItemResource(R.layout.wheel_text_centered);
        adapter.setItemTextResource(R.id.text);
        adapter.setTextSize(22);
        wheelVerticalView.setViewAdapter(adapter);
        wheelVerticalView.setCurrentItem(index);
//        timerWheel.setCyclic(true);
    	new AlertDialog.Builder(this.getActivity())
    	.setView(wheelVerticalView)
    	.setTitle(R.string.command_timer)
	    .setNegativeButton(R.string.cancel, null)	
		.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				timer = wheelVerticalView.getCurrentItem();
				tvTimer.setText(timers[timer]);
				dialog.cancel();
			}
		})
        .show();
	}
	
	private void showPositionCountAlertDialog(int index){
		final WheelVerticalView wheelVerticalView = getWheelVerticalView();
		wheelVerticalView.setVisibleItems(5);
        NumericWheelAdapter adapter = new NumericWheelAdapter(mContext, 1, 24);
        adapter.setItemResource(R.layout.wheel_text_centered);
        adapter.setItemTextResource(R.id.text);
        adapter.setTextSize(22);
        wheelVerticalView.setViewAdapter(adapter);
        wheelVerticalView.setCurrentItem(index);
    	new AlertDialog.Builder(this.getActivity())
    	.setView(wheelVerticalView)
    	.setTitle(R.string.command_postion_count)
	    .setNegativeButton(R.string.cancel, null)
		.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int currentItem = wheelVerticalView.getCurrentItem();
				postionCount = currentItem+1;
				tvPositionCount.setText(postionCount+"");
				dialog.cancel();
			}
		})
        .show();
	}
	
	private void showIntervalTimeAlertDialog(int index){
		final WheelVerticalView wheelVerticalView = getWheelVerticalView();
		wheelVerticalView.setVisibleItems(5);
        NumericWheelAdapter adapter = new NumericWheelAdapter(mContext, 1, 60);
        adapter.setItemResource(R.layout.wheel_text_centered);
        adapter.setItemTextResource(R.id.text);
        adapter.setTextSize(22);
        wheelVerticalView.setViewAdapter(adapter);
        wheelVerticalView.setCurrentItem(index);
    	new AlertDialog.Builder(this.getActivity())
    	.setView(wheelVerticalView)
    	.setTitle(R.string.command_interval_time)
	    .setNegativeButton(R.string.cancel, null)
		.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int currentItem = wheelVerticalView.getCurrentItem();
				intervalTime = currentItem +1;
				tvIntervalTime.setText(intervalTime+"");
				dialog.cancel();
			}
		})
        .show();
	}
	
	private WheelVerticalView getWheelVerticalView(){
		WheelVerticalView wheelVerticalView = (WheelVerticalView)this.getActivity().getLayoutInflater().inflate(R.layout.wheel_vertical_view, null);
		return wheelVerticalView;
	}

	private void getData(CommandType commandType) {
		progressView.setVisibility(View.VISIBLE);
		if (getDataTask != null) {
			getDataTask.cancel(true);
		}
		getDataTask = new GetDataTask();
		getDataTask.execute(commandType);
	}

	private class GetDataTask extends
			AsyncTask<CommandType, Void, CommandResponseVO> {

		private CommandType commandType;
		
		@Override
		protected CommandResponseVO doInBackground(CommandType... params) {
			commandType = params[0];
			String jsonStr = null;
			switch (commandType) {
			case GETLAST:
				jsonStr = HttpClientUtil.getCommandLast(imei);
				break;
			case SEND:
				jsonStr = HttpClientUtil.sendCommand(imei, workStatue, workModel, timer, postionCount, intervalTime*60);
				break;
			}
			CommandResponseVO responseVO = JsonUtil.parseObject(jsonStr, CommandResponseVO.class);
			return responseVO;
		}

		@Override
		protected void onPostExecute(CommandResponseVO result) {
			progressView.setVisibility(View.GONE);
			if(result == null){
				Util.showToast(mContext, "获取数据失败");
				super.onPostExecute(result);
				return;
			}
			
			if(result.ret != 0){
				Util.showToast(mContext, result.msg);
				super.onPostExecute(result);
				return;
			}
			
			Command command = result.rows;
			if(command != null){
				switch (commandType) {
				case GETLAST:
					tvImei.setText(command.imei);
					tvOperateStatue.setText(command.operateStatue);
					tvOperateTime.setText(command.operateTime);
					tvOperateUser.setText(command.operateUser);
					String sd = command.sendDetail;
					if(sd == null){
						sd = "";
					}
					tvSendDetail.setText(sd.replace("，", "\n"));
					break;
				case SEND:
					if("1".equals(command.status)){
						Util.showToast(mContext, "指令设置成功！" + result.msg);
						getData(CommandType.GETLAST);
					}else{
						Util.showToast(mContext, "指令设置失败！" + result.msg);
					}
					break;
				}
			}else{
				Util.showToast(mContext, "请求异常");
			}
			
			super.onPostExecute(result);
		}
	}

	private enum CommandType{
		GETLAST,
		SEND;
	}

}
