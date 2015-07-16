package com.buaair.carsmart.fragment;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.InfoWindowAdapter;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;
import com.buaair.carsmart.R;
import com.buaair.carsmart.entity.Car;
import com.buaair.carsmart.utils.HttpClientUtil;
import com.buaair.carsmart.utils.JsonUtil;
import com.buaair.carsmart.utils.LogUtil;
import com.buaair.carsmart.utils.Util;
import com.buaair.carsmart.vo.CarDetailResponseVO;

@SuppressLint("InflateParams")
@SuppressWarnings("deprecation")
public class TrackingFragment extends Fragment implements LocationSource,
		AMapLocationListener, SensorEventListener, InfoWindowAdapter, OnClickListener {

	private long refrefreshTime = 1000*10;//数据刷新时间
	
	private Handler handler = new Handler();
	
	private Car car;
	
	private Context mContext;
	
	private View parentView;
	
	private ImageView mapBtnType,mapBtnSwitch;

	private MapView mapView;
	private AMap aMap;

	private Marker mGPSMarker;
	private Marker mCarMarker;
	private Polyline polyline;

	private OnLocationChangedListener mListener;
	private LocationManagerProxy mAMapLocationManager;

	private SensorManager mSensorManager;
	private Sensor mSensor;

	private long lastTime = 0;
	private final int TIME_SENSOR = 100;
	private float mAngle;

	private int carWidth;
	
	private boolean showInfoWindows = true;
	
	private boolean firstLocation = true;
	
	private boolean isMoveToCar = true;
	
	private GetDataTask getDataTask;

	// map init
	private void mapInit() {
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
		// 初始化传感器
		mSensorManager = (SensorManager) this.getActivity().getSystemService(
				Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		
	}

	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		mGPSMarker = aMap.addMarker(new MarkerOptions().icon(
				BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),
								R.drawable.location_marker))).anchor((float) 0.5, (float) 0.5));

		aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
	}
	
	private void setUpViews() {
		mapView = (MapView) parentView.findViewById(R.id.map);
		mapBtnType = (ImageView) parentView.findViewById(R.id.map_btn_type);
		mapBtnType.setVisibility(View.VISIBLE);
		mapBtnType.setOnClickListener(this);
		mapBtnSwitch = (ImageView) parentView.findViewById(R.id.map_btn_switch);
		mapBtnSwitch.setVisibility(View.VISIBLE);
		mapBtnSwitch.setOnClickListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.fragment_tracking, container, false); 
		setUpViews();
		return parentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = this.getActivity().getApplicationContext();

		carWidth = (int) this.getResources().getDimension(
				R.dimen.car_icon_width);

		// ((TextView) getView().findViewById(R.id.tvTop))
		// .setText(R.string.carchase);
		
		mapView.onCreate(savedInstanceState);

		mapInit();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		deactivate();
		if(getDataTask != null){
			getDataTask.cancel(true);
		}
		handler.removeCallbacks(runnable);
	}

	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
		getData();
		registerSensorListener();
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
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.map_btn_close:
			showInfoWindows = false;
			mCarMarker.hideInfoWindow();
			break;
		case R.id.map_btn_type:
			int mapType =aMap.getMapType();
			if(AMap.MAP_TYPE_NORMAL == mapType){
				aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
			}else{
				aMap.setMapType(AMap.MAP_TYPE_NORMAL);
			}
			break;
		case R.id.map_btn_switch:
			switchMarker();
			break;
		default:
			break;
		}
		
	}

	@Override
	public View getInfoContents(Marker marker) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		showInfoWindows = true;
		
		View infoWindow = getActivity().getLayoutInflater().inflate(
				R.layout.custom_info_window, null);
		DisplayMetrics dm = getResources().getDisplayMetrics();

		int width = (int) (dm.widthPixels * 0.55); // 屏幕宽（像素，如：480px）

		TextView equipmentNameTV = (TextView) infoWindow.findViewById(R.id.equipmentName);
		TextView gpsTimeTV = (TextView) infoWindow.findViewById(R.id.gpsTime);
		TextView gpsPosTV = (TextView) infoWindow.findViewById(R.id.gpsPos);
		TextView statusTV = (TextView) infoWindow.findViewById(R.id.status);
		
		infoWindow.findViewById(R.id.map_btn_close).setOnClickListener(this);
		infoWindow.findViewById(R.id.line).setVisibility(View.GONE);
		infoWindow.findViewById(R.id.btn_lb).setVisibility(View.GONE);
//		infoWindow.findViewById(R.id.map_btn_carinfo).setOnClickListener(this);
//		infoWindow.findViewById(R.id.map_btn_history).setOnClickListener(this);
//		infoWindow.findViewById(R.id.map_btn_tracking).setOnClickListener(this);

		String nickName = car.equipmentNickName;
		String name = car.equipmentName;
		if(nickName == null){
			nickName = "";
		}
		if(name == null || name.isEmpty()){
			name = "";
		}else{
			name = "(" + name +")";
		}
		equipmentNameTV.setText(nickName + name);
		gpsTimeTV.setText(car.gpsTime);
		gpsPosTV.setText(car.gpsPos);
		statusTV.setText(car.equipmentStatueInfo);
		gpsPosTV.setMaxWidth(width);

		return infoWindow;
	}

	@Override
	public void onLocationChanged(Location location) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	/**
	 * 定位成功后回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation aLocation) {
		if (mListener != null && aLocation != null) {

			if (polyline != null) {
				polyline.remove();
				polyline = null;
			}
			LatLng location = new LatLng(aLocation.getLatitude(),
					aLocation.getLongitude());
			ArrayList<LatLng> points = new ArrayList<LatLng>();
			points.add(location);
			points.add(mCarMarker.getPosition());

			PolylineOptions polylineOptions = new PolylineOptions();
			polylineOptions.addAll(points);
			polylineOptions.color(Color.RED).width(5);

			polyline = aMap.addPolyline(polylineOptions);
//			mListener.onLocationChanged(aLocation);// 显示系统小蓝点
			mGPSMarker.setPosition(location);

			if(isMoveToCar){
				CameraUpdate update = CameraUpdateFactory.changeLatLng(location);
				aMap.moveCamera(update);
			}
		}
	}

	/**
	 * 激活定位
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(mContext);
			/*
			 * mAMapLocManager.setGpsEnable(false);
			 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
			 * API定位采用GPS和网络混合定位方式
			 * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
			 */
			mAMapLocationManager.requestLocationUpdates(
					LocationProviderProxy.AMapNetwork, 2000, 10, this);
		}
	}

	/**
	 * 停止定位
	 */
	@Override
	public void deactivate() {
		mListener = null;
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destory();
		}
		mAMapLocationManager = null;
		unRegisterSensorListener();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (System.currentTimeMillis() - lastTime < TIME_SENSOR) {
			return;
		}
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ORIENTATION: {
			float x = event.values[0];

			x += getScreenRotationOnPhone(mContext);
			x %= 360.0F;
			if (x > 180.0F)
				x -= 360.0F;
			else if (x < -180.0F)
				x += 360.0F;
			if (Math.abs(mAngle - 90 + x) < 3.0f) {
				break;
			}
			mAngle = x;
			if (mGPSMarker != null) {
				mGPSMarker.setRotateAngle(-mAngle);
				aMap.invalidate();
			}
			lastTime = System.currentTimeMillis();
		}
		}

	}

	/**
	 * 获取当前屏幕旋转角度
	 * 
	 * @param activity
	 * @return 0表示是竖屏; 90表示是左横屏; 180表示是反向竖屏; 270表示是右横屏
	 */
	private int getScreenRotationOnPhone(Context context) {
		final Display display = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		switch (display.getRotation()) {
		case Surface.ROTATION_0:
			return 0;

		case Surface.ROTATION_90:
			return 90;

		case Surface.ROTATION_180:
			return 180;

		case Surface.ROTATION_270:
			return -90;
		}
		return 0;
	}

	private void registerSensorListener() {
		mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	private void unRegisterSensorListener() {
		mSensorManager.unregisterListener(this, mSensor);
	}

	private void switchMarker(){
		LatLng latLng = null;
		if(isMoveToCar){
			if(mCarMarker != null){
				isMoveToCar = false;
				latLng = mCarMarker.getPosition();
			}
		}else{
			if(mGPSMarker != null){
				isMoveToCar = true;
				latLng = mGPSMarker.getPosition();
			}
		}
		
		if(latLng == null){
			return;
		}
		
		mapBtnSwitch.setImageResource(isMoveToCar?R.drawable.map_button_switch_car_selector:R.drawable.map_button_switch_gps_selector);
		CameraUpdate update = CameraUpdateFactory.changeLatLng(latLng);
		aMap.moveCamera(update);
	}
	
	private MarkerOptions createCarMarkerOptions(Car car) {
		LatLng latLng = new LatLng(car.lat, car.lng);
		MarkerOptions markerOptions = new MarkerOptions();
		ImageView imageView = new ImageView(mContext);
		imageView.setImageResource(car.equipmentStatueFlag == 0 ? R.drawable.car_red : R.drawable.car_yellow);
//		imageView.setRotation((float) car.course);
		imageView.setLayoutParams(new LayoutParams(carWidth, carWidth));
		BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory
				.fromView(imageView);

		// 设置Marker的图标样式
		markerOptions.icon(bitmapDescriptor);
		// 设置Marker点击之后显示的标题
		markerOptions.title(car.equipmentName);
		// 设置Marker的坐标，为我们点击地图的经纬度坐标
		markerOptions.position(latLng);
		// 设置Marker的可见性
		markerOptions.visible(true);
		// 设置Marker是否可以被拖拽，这里先设置为false，之后会演示Marker的拖拽功能
		markerOptions.draggable(false);
		markerOptions.anchor(0.5f, 0.5f);
		return markerOptions;
	}
	
	private Runnable runnable =new Runnable() {  
	    @Override  
	    public void run() {  
	    	getData();
	    }  
	};
	
	private void getData(){
		if(getDataTask != null){
			getDataTask.cancel(true);
		}
		getDataTask = new GetDataTask();
		
		String imeiText = getArguments().getString("imei");
		getDataTask.execute(imeiText);
	}

	private class GetDataTask extends
			AsyncTask<String, Void, CarDetailResponseVO> {

		@Override
		protected CarDetailResponseVO doInBackground(String... params) {
			String imei = params[0];
			String jsonStr = HttpClientUtil.getCarTracking(imei, null);
			CarDetailResponseVO responseVO = JsonUtil.parseObject(jsonStr,
					CarDetailResponseVO.class);
			return responseVO;
		}

		@Override
		protected void onPostExecute(CarDetailResponseVO result) {
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
			
			car = result.rows;
			if(car != null){
				if(mCarMarker == null){
//					mCarMarker.hideInfoWindow();
//					mCarMarker.destroy();
					MarkerOptions markerOptions = createCarMarkerOptions(car);
					mCarMarker = aMap.addMarker(markerOptions);
				}
				mCarMarker.setRotateAngle(360 - (float) car.course%360);
				LatLng location = new LatLng(car.lat, car.lng); 
				mCarMarker.setPosition(location);
				if(!isMoveToCar){
					CameraUpdate update = CameraUpdateFactory.changeLatLng(location);
					aMap.moveCamera(update);
				}
				if(firstLocation){
					switchMarker();
					firstLocation = false;
				}
				if(showInfoWindows){
				mCarMarker.showInfoWindow();
				}
			}else{
				LogUtil.d("获取车辆追踪信息失败");
			}
			//定时刷新数据
			handler.postDelayed(runnable, refrefreshTime);
			super.onPostExecute(result);
		}
	}
}
