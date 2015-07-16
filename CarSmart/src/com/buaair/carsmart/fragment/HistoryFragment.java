package com.buaair.carsmart.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;
import com.buaair.carsmart.R;
import com.buaair.carsmart.entity.CarHistory;
import com.buaair.carsmart.utils.DateUtil;
import com.buaair.carsmart.utils.HttpClientUtil;
import com.buaair.carsmart.utils.JsonUtil;
import com.buaair.carsmart.utils.TimePickDialogUtil;
import com.buaair.carsmart.utils.TimePickDialogUtil.OnDateTimeChangedListener;
import com.buaair.carsmart.utils.Util;
import com.buaair.carsmart.vo.HistoryResponseVO;

@SuppressLint("HandlerLeak")
public class HistoryFragment extends Fragment implements OnDateTimeChangedListener{

	private Context mContext;

	private View parentView;
	private View progressView;

	private MapView mapView;
	private AMap aMap;

	private GetDataTask getDataTask;
	
	private String imei;

	private Date beginDate, endDate;
	
	private Marker marker=null;//当前轨迹点图案
//	public  Handler timer=new Handler();//定时器
	
	private long speed = 300;
	
	private int carWidth;
	
	private ArrayList<CarHistory> carList = new ArrayList<CarHistory>() ;
	private int carIndex = 0;

	// map init
	private void mapInit() {
		if (aMap == null) {
			aMap = mapView.getMap();
		}
	}
	
	private void dateInit(){
		Date now = new Date();
		beginDate = DateUtil.setTimeFirst(now);
		endDate = DateUtil.setTimeLast(now);
	}

	private void setUpViews() {
		// add gesture operation's ignored views
		mapView = (MapView) parentView.findViewById(R.id.map);
		progressView = parentView.findViewById(R.id.progress_view);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.fragment_history, container,
				false);
		setUpViews();
		return parentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = this.getActivity();
		carWidth = (int) this.getResources().getDimension(
				R.dimen.car_icon_width);
		mapView.onCreate(savedInstanceState);

		mapInit();
		dateInit();
		
		imei = getArguments().getString("imei");
		
	}
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
		if (getDataTask != null) {
			getDataTask.cancel(true);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
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
	public boolean onOptionsItemSelected(MenuItem item) {
		item.setChecked(true);
		Date now = new Date();
		switch (item.getItemId()) {
		case R.id.menu_time_today:
			beginDate = DateUtil.setTimeFirst(now);
			endDate = now;
			getData();
			break;
		case R.id.menu_time_yesterday:
			Date yesterday = new Date(now.getTime() - 1*24*60*60*1000l);
			beginDate = DateUtil.setTimeFirst(yesterday);
			endDate = DateUtil.setTimeLast(yesterday);
			getData();
			break;
		case R.id.menu_time_before_yesterday:
			Date byesterday = new Date(now.getTime() - 2*24*60*60*1000l);
			beginDate = DateUtil.setTimeFirst(byesterday);
			endDate = DateUtil.setTimeLast(byesterday);
			getData();
			break;
		case R.id.menu_time_a_hour_before:
			beginDate = new Date(now.getTime() - 365*24*60*60*1000l);
			endDate = now;
			getData();
			break;
		case R.id.menu_time_custom:
			showTimerAlertDialog();
			break;
		case R.id.menu_speed_1:
			speed = 500;
			break;
		case R.id.menu_speed_2:
			speed = 400;
			break;
		case R.id.menu_speed_3:
			speed = 300;
			break;
		case R.id.menu_speed_4:
			speed = 200;
			break;
		case R.id.menu_speed_5:
			speed = 100;
			break;
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.history, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	
	@Override
	public void onDateTimeChanged(Date beginDate, Date endDate) {
		this.beginDate = beginDate;
		this.endDate = endDate;
		getData();
	}

	private Polyline polyline;
	private List<LatLng> points = new ArrayList<LatLng>();
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if(carIndex >= carList.size()){
					break;
				}
				CarHistory car = carList.get(carIndex);
				
				if(marker == null){
					MarkerOptions markerOptions = createCarMarkerOptions(car);
					marker = aMap.addMarker(markerOptions);
				}
				
				marker.setRotateAngle(360 - (float)car.course%360);
				marker.setPosition(new LatLng(car.lat, car.lng));
				if(polyline == null){
					PolylineOptions polylineOptions = new PolylineOptions();
					polylineOptions.color(Color.RED).width(5);
					polyline = aMap.addPolyline(polylineOptions);
				}
				points.add(new LatLng(car.lat, car.lng));
				polyline.setPoints(points);
//				polylineOptions.add(new LatLng(car.lat, car.lng));
				
				if(carIndex == 0){
					
					//画起点终点
					aMap.addMarker(new MarkerOptions()
					.position(marker.getPosition())//.title("起点")
					.icon(BitmapDescriptorFactory
							.fromBitmap(BitmapFactory
									.decodeResource(getResources(), R.drawable.nav_route_result_start_point))));
					
					CameraUpdate update = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15);
					aMap.moveCamera(update);
					
				}
				
				if(carIndex == carList.size() - 1){
					aMap.addMarker(new MarkerOptions()
					.position(marker.getPosition())//.title("终点")
					.icon(BitmapDescriptorFactory
							.fromBitmap(BitmapFactory
									.decodeResource(getResources(), R.drawable.nav_route_result_end_point))));
				}
				aMap.invalidate();
				carIndex ++;
				
				if(carIndex < carList.size()){
					handler.postDelayed(runnable, speed);
				}
				break;

			default:
				break;
			}
		}
	};
	
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			handler.sendEmptyMessage(1);
		}
	};
	
	private MarkerOptions createCarMarkerOptions(CarHistory car) {
		LatLng latLng = new LatLng(car.lat, car.lng);
		MarkerOptions markerOptions = new MarkerOptions();
		ImageView imageView = new ImageView(mContext);
		imageView.setImageResource(R.drawable.car_red);
//		imageView.setRotation((float) car.course);
		imageView.setLayoutParams(new LayoutParams(carWidth, carWidth));
		BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromView(imageView);

		// 设置Marker的图标样式
		markerOptions.icon(bitmapDescriptor);
		// 设置Marker点击之后显示的标题
//		markerOptions.title(car.speed+"");
		// 设置Marker的坐标，为我们点击地图的经纬度坐标
		markerOptions.position(latLng);
		// 设置Marker的可见性
		markerOptions.visible(true);
		// 设置Marker是否可以被拖拽，这里先设置为false，之后会演示Marker的拖拽功能
		markerOptions.draggable(false);
		markerOptions.anchor(0.5f, 0.5f);
		return markerOptions;
	}
	
	private void showTimerAlertDialog(){
		
		TimePickDialogUtil dialogUtil = new TimePickDialogUtil(this.getActivity(), this);
		dialogUtil.showTimePicKDialog(beginDate, endDate);
		
	}
	

	private void getData() {
		if(marker != null){
			marker.remove();
			marker = null;
		}
		if(polyline != null){
			polyline.remove();
			polyline = null;
			points.clear();
		}
		carIndex = 0 ;
		carList.clear();
		aMap.clear();
		handler.removeCallbacks(runnable);
		
		progressView.setVisibility(View.VISIBLE);
		if (getDataTask != null) {
			getDataTask.cancel(true);
		}
		
		getDataTask = new GetDataTask();
		getDataTask.execute();
	}
	
	private int limit = 1000;

	private class GetDataTask extends
			AsyncTask<String, Void, HistoryResponseVO> {

		@Override
		protected HistoryResponseVO doInBackground(String... params) {
			long beginTime = beginDate.getTime()/1000;
			long endTime = endDate.getTime()/1000;
			String jsonStr = HttpClientUtil.getCarHistory(imei, null, beginTime, endTime, limit);
			HistoryResponseVO responseVO = JsonUtil.parseObject(jsonStr, HistoryResponseVO.class);
			return responseVO;
		}

		@Override
		protected void onPostExecute(HistoryResponseVO result) {
			progressView.setVisibility(View.GONE);
			if (result != null && result.ret == 0) {
				List<CarHistory> cars = result.rows;
				if((cars == null || cars.size() == 0) && carList.size() == 0){
					Util.showToast(mContext, "该时间段内没有行驶记录");
				}else{
					carList.addAll(cars);
					if(carList.size() <= limit){
						handler.post(runnable);
					}
					if(cars.size() == limit){
						CarHistory carHistory = cars.get(limit -1);
						long gpstime = carHistory.gpsTime*1000;
						System.out.println(gpstime);
						beginDate = new Date(gpstime);
						System.out.println(beginDate.getTime());
						getDataTask = new GetDataTask();
						getDataTask.execute();
					}
				}
			} else {
				Toast.makeText(mContext, "获取车辆信息失败！" + result.msg,
						Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(result);
		}
	}
}
