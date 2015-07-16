package com.buaair.carsmart.fragment;

import java.util.HashMap;
import java.util.LinkedList;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.buaair.carsmart.R;
import com.buaair.carsmart.entity.Car;
import com.buaair.carsmart.utils.HttpClientUtil;
import com.buaair.carsmart.utils.JsonUtil;
import com.buaair.carsmart.utils.LogUtil;
import com.buaair.carsmart.utils.Util;
import com.buaair.carsmart.vo.CarDetailResponseVO;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class CarInfoFragment extends ListFragment implements
		OnRefreshListener<ListView> {

	private View parentView;

	private Context mContext;

	private PullToRefreshListView mPullRefreshListView;

	private GetDataTask getDataTask;

	private SimpleAdapter mAdapter;
	private LinkedList<HashMap<String, Object>> listItem = new LinkedList<HashMap<String, Object>>();

	private void setUpView() {
		mPullRefreshListView = (PullToRefreshListView) parentView
				.findViewById(R.id.pull_refresh_list);

		mPullRefreshListView.setOnRefreshListener(this);

		mPullRefreshListView.setMode(Mode.PULL_FROM_START);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.fragment_carinfo, container,
				false);
		setUpView();
		return parentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mContext = getActivity().getApplicationContext();

		mAdapter = new SimpleAdapter(mContext, listItem,// 需要绑定的数据
				R.layout.list_item_carinfo,// 每一行的布局//动态数组中的数据源的键对应到定义布局的View中
				// android.R.layout.simple_list_item_2,
				new String[] { "name", "value" }, 
				new int[] { R.id.name, R.id.value });
		mPullRefreshListView.setAdapter(mAdapter);

		mPullRefreshListView.setMode(Mode.MANUAL_REFRESH_ONLY);
		// getData();
		mPullRefreshListView.setRefreshing(false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (getDataTask != null) {
			getDataTask.cancel(true);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		getData();
	}

	private void getData() {
		String imeiText = getArguments().getString("imei");
		getDataTask = new GetDataTask();
		getDataTask.execute(imeiText);
	}

	private class GetDataTask extends
			AsyncTask<String, Void, CarDetailResponseVO> {

		private HashMap<String, Object> getItem(int resId, String value) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("name", mContext.getString(resId));
			map.put("value", value);
			return map;
		}

		@Override
		protected CarDetailResponseVO doInBackground(String... params) {
			String imei = params[0];
			System.out.println("imei :"+imei);
			String jsonStr = HttpClientUtil.getCarDetail(imei);
			CarDetailResponseVO responseVO = JsonUtil.parseObject(jsonStr,
					CarDetailResponseVO.class);
			
			LogUtil.d(JsonUtil.toJSON(responseVO));
			return responseVO;
		}

		@Override
		protected void onPostExecute(CarDetailResponseVO result) {
			if (result == null) {
				Util.showToast(mContext, "获取车辆信息失败！");
				return;
			}
			if(result.ret != 0){
				Util.showToast(mContext, result.msg);
				return;
			}

			Car car = result.rows;
			if(car != null){
				listItem.clear();
				listItem.add(getItem(R.string.carinfo_equipment_nick_name,	car.equipmentNickName));
				listItem.add(getItem(R.string.carinfo_equipment_num,		car.equipmentNum));
				listItem.add(getItem(R.string.carinfo_equipment_name,		car.equipmentName));
				listItem.add(getItem(R.string.carinfo_imei, 				car.imei));
				listItem.add(getItem(R.string.carinfo_terminal_type,		car.terminalType));
				listItem.add(getItem(R.string.carinfo_terminal_model,		car.terminalModel));
				listItem.add(getItem(R.string.carinfo_sim, 					car.sim));
				listItem.add(getItem(R.string.carinfo_owner_name, 			car.ownerName));
				listItem.add(getItem(R.string.carinfo_owner_phone,			car.ownerPhone));
				listItem.add(getItem(R.string.carinfo_owner_address,		car.ownerAddress));
				listItem.add(getItem(R.string.carinfo_in_time, 				car.in_time));
				listItem.add(getItem(R.string.carinfo_out_time, 			car.out_time));
			}

			mAdapter.notifyDataSetChanged();

			// Call onRefreshComplete when the list has been refreshed.
			mPullRefreshListView.onRefreshComplete();

			super.onPostExecute(result);
		}
	}
}
