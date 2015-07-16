package com.buaair.carsmart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.buaair.carsmart.entity.Car;
import com.buaair.carsmart.utils.HttpClientUtil;
import com.buaair.carsmart.utils.JsonUtil;
import com.buaair.carsmart.utils.LogUtil;
import com.buaair.carsmart.utils.Util;
import com.buaair.carsmart.vo.CarListResponseVO;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshBase.State;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.extras.SoundPullEventListener;

public class CarListActivity extends ListActivity implements
		OnLastItemVisibleListener, OnRefreshListener2<ListView>,
		AdapterView.OnItemClickListener {
	
	private EditText searchText;
	private PullToRefreshListView mPullRefreshListView;
	private SimpleAdapter mAdapter;
	
	private GetDataTask getDataTask;

	private LinkedList<HashMap<String, Object>> listItem = new LinkedList<HashMap<String, Object>>();
	private ArrayList<Car> carList = new ArrayList<Car>();
	
	/**
	 *  页大小
	 */
	private int rows = 10;
	/**
	 * 当前页
	 */
	private int page = 0;
	/**
	 * 总页数
	 */
	private int total = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_car_list);

		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
		searchText = (EditText) findViewById(R.id.searchText);
		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView.setOnRefreshListener(this);

		// Add an end-of-list listener
		mPullRefreshListView.setOnLastItemVisibleListener(this);
		
		mPullRefreshListView.setMode(Mode.BOTH);
		
		
		searchText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				switch (v.getId()) {  
		            case R.id.searchText:  
		                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
		                	mPullRefreshListView.setRefreshing(false);
		                	try {
		                		Activity context = CarListActivity.this;
		                        InputMethodManager inputMethodManager = (InputMethodManager) context  
		                                .getSystemService(Context.INPUT_METHOD_SERVICE);  
		                        inputMethodManager.hideSoftInputFromWindow(context                                .getCurrentFocus().getWindowToken(),  
		                                InputMethodManager.HIDE_NOT_ALWAYS);  
		                    } catch (Exception e) {  
		                        Log.d("", "关闭输入法异常");  
		                    }
		                }
		                break;  
		            default:  
		                break;  
		        }  
		        return false;
			}
		});

		getActionBar().setDisplayHomeAsUpEnabled(true);

		/**
		 * Add Sound Event Listener
		 */
		SoundPullEventListener<ListView> soundListener = new SoundPullEventListener<ListView>(
				this);
		soundListener.addSoundEvent(State.PULL_TO_REFRESH, R.raw.pull_event);
		soundListener.addSoundEvent(State.RESET, R.raw.reset_sound);
		soundListener.addSoundEvent(State.REFRESHING, R.raw.refreshing_sound);
		// mPullRefreshListView.setOnPullEventListener(soundListener);

		ListView actualListView = mPullRefreshListView.getRefreshableView();

		// Need to use the Actual ListView when registering for Context Menu
		registerForContextMenu(actualListView);

		mAdapter = new SimpleAdapter(
				this,
				listItem,// 需要绑定的数据
				R.layout.list_item,// 每一行的布局//动态数组中的数据源的键对应到定义布局的View中
				new String[] { "car_icon","car_name", "car_num", "terminal_num",
						"update_time", "address" },
				new int[] { R.id.car_icon, R.id.list_item_car_name, R.id.list_item_car_num,
						R.id.list_item_terminal_num,
						R.id.list_item_data_update_time, R.id.list_item_address });

		// You can also just use setListAdapter(mAdapter) or
		// mPullRefreshListView.setAdapter(mAdapter)
		actualListView.setAdapter(mAdapter);

		actualListView.setOnItemClickListener(this);

		listItem.clear();
		mPullRefreshListView.setRefreshing(false);
//		getData(TaskType.UP);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(getDataTask != null){
			getDataTask.cancel(true);
		}
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		String label = DateUtils.formatDateTime(getApplicationContext(),
				System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_ABBREV_ALL);
		// Update the LastUpdatedLabel
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

		// Do work to refresh the list here.
		getData(TaskType.DOWN);
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		String label = DateUtils.formatDateTime(getApplicationContext(),
				System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_ABBREV_ALL);

		// Update the LastUpdatedLabel
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

		// Do work to refresh the list here.
		getData(TaskType.UP);
	}

	@Override
	public void onLastItemVisible() {
//		Toast.makeText(CarListActivity.this, "End of List!", Toast.LENGTH_SHORT)
//				.show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
//		Toast.makeText(this,"position : " + position, Toast.LENGTH_SHORT).show();
		String imei = this.carList.get(position-1).imei;
		Intent intent = new Intent(CarListActivity.this, MainActivity.class);
		intent.putExtra("imei", imei);
		intent.putExtra("fragment_flag", MainActivity.FRAGMENT_FLAG_CARINFO);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.car_list, menu);
//		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
//		searchView.setQueryHint(this.getResources().getString(R.string.search_hint));
	    return true; 
	}

//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v,
//			ContextMenuInfo menuInfo) {
//		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
//
//		menu.setHeaderTitle("Item: "
//				+ getListView().getItemAtPosition(info.position));
//		menu.add("Item 1");
//		menu.add("Item 2");
//		menu.add("Item 3");
//		menu.add("Item 4");
//
//		super.onCreateContextMenu(menu, v, menuInfo);
//	}
	
	

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		mPullRefreshListView.setRefreshing(false);
		
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// MenuItem disableItem = menu.findItem(MENU_DISABLE_SCROLL);
		// disableItem
		// .setTitle(mPullRefreshListView
		// .isScrollingWhileRefreshingEnabled() ?
		// "Disable Scrolling while Refreshing"
		// : "Enable Scrolling while Refreshing");
		//
		// MenuItem setModeItem = menu.findItem(MENU_SET_MODE);
		// setModeItem
		// .setTitle(mPullRefreshListView.getMode() == Mode.BOTH ?
		// "Change to MODE_FROM_START"
		// : "Change to MODE_PULL_BOTH");

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			Intent upIntent = NavUtils.getParentActivityIntent(this);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				TaskStackBuilder.create(this)
						.addNextIntentWithParentStack(upIntent)
						.startActivities();
			} else {
				upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				NavUtils.navigateUpTo(this, upIntent);
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	private void getData(TaskType taskType){
		if(getDataTask != null){
			getDataTask.cancel(true);
		}
		getDataTask = new GetDataTask();
		getDataTask.execute(taskType);
	}


	private class GetDataTask extends
			AsyncTask<TaskType, Void, CarListResponseVO> {
		
		private TaskType taskType;

		@Override
		protected CarListResponseVO doInBackground(
				TaskType... params) {
			// Simulates a background job.
			CarListResponseVO responseVO = null;
			String jsonStr = null;
			taskType = params[0];
			String searchString = searchText.getText().toString();
			switch (taskType) {
			case UP:
				if(page < total){
					page += 1;
					jsonStr = HttpClientUtil.getCarList(null, searchString, 2, page, rows);
					responseVO = JsonUtil.parseObject(jsonStr,	CarListResponseVO.class);
				}else{
					return null;
				}
				break;
			case DOWN:
				page = 1;
				jsonStr = HttpClientUtil.getCarList(null, searchString, 2, page, rows);
				responseVO = JsonUtil.parseObject(jsonStr,	CarListResponseVO.class);
				break;
			default:
				break;
			}
			
			
			return responseVO;
		}

		@Override
		protected void onPostExecute(CarListResponseVO result) {
			if(result == null){
				Util.showToast(CarListActivity.this, "没有更多数据");
				return;
			}
			
			if(result.ret != 0){
				Util.showToast(CarListActivity.this, result.msg);
				return;
			}
			
			if(TaskType.DOWN.equals(taskType)){
				listItem.clear();
				carList.clear();
			}
			
			ArrayList<Car> resultList = result.rows;
			total = result.total;
			page = result.page;

			LogUtil.d("total:" + total + "    page:"+page);
			listItem.addAll(listItem.size(), converData(resultList));
			carList.addAll(CarListActivity.this.carList.size(), resultList);
			
			
			mAdapter.notifyDataSetChanged();

			// Call onRefreshComplete when the list has been refreshed.
			mPullRefreshListView.onRefreshComplete();
			
			Mode mode = mPullRefreshListView.getMode();
			if(page >= total && mode != Mode.PULL_FROM_START){
				mPullRefreshListView.setMode(Mode.PULL_FROM_START);
//				Util.showToast(CarListActivity.this, "没有跟多数据");
			}else if(mode != Mode.BOTH){
				mPullRefreshListView.setMode(Mode.BOTH);
			}

			super.onPostExecute(result);
		}
		
		private LinkedList<HashMap<String, Object>> converData(ArrayList<Car> carList) {
			LinkedList<HashMap<String, Object>> listItem2 = new LinkedList<HashMap<String, Object>>();
			for (int i = 0; i < carList.size(); i++) {
				HashMap<String, Object> item = new HashMap<String, Object>();
				item.put("car_icon", carList.get(i).equipmentStatueFlag == 0 ? R.drawable.car_icon_red:R.drawable.car_icon_blue);
				item.put("car_name", carList.get(i).equipmentNickName);
				item.put("car_num", carList.get(i).equipmentName );
				item.put("terminal_num", carList.get(i).imei);
				item.put("update_time", carList.get(i).gpsTime);
				item.put("address", carList.get(i).gpsPos);
				listItem2.add(item);
			}
			return listItem2;
		}
	}
	

	private enum TaskType {
		/**
		 * 上拉刷新
		 */
		UP,
		/**
		 * 下拉刷新
		 */
		DOWN;
	}

}
