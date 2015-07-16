package com.buaair.carsmart.fragment;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.InfoWindowAdapter;
import com.amap.api.maps2d.AMap.OnInfoWindowClickListener;
import com.amap.api.maps2d.AMap.OnMarkerClickListener;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.buaair.carsmart.CarListActivity;
import com.buaair.carsmart.CarMapActivity;
import com.buaair.carsmart.MainActivity;
import com.buaair.carsmart.R;
import com.buaair.carsmart.entity.Car;
import com.buaair.carsmart.utils.HttpClientUtil;
import com.buaair.carsmart.utils.JsonUtil;
import com.buaair.carsmart.utils.Util;
import com.buaair.carsmart.vo.CarMapResponseVO;
import com.special.ResideMenu.ResideMenu;

public class CarMapFragment extends Fragment implements OnMarkerClickListener,
		OnInfoWindowClickListener, InfoWindowAdapter, OnClickListener {

	private Context mContext;

	private View parentView;

	private ImageView mapBtnLeft, mapBtnRight, mapBtnType;

	private ResideMenu resideMenu;
	
	private View progressView;

	private MapView mapView;
	private AMap aMap;

	private int carWidth;
	
	private GetDataTask getDataTask;

	/**
	 * 当前显示的车辆信息下标
	 */
	private int position;

	// map init
	private void mapInit() {
		if (aMap == null) {
			aMap = mapView.getMap();
		}

		aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
		aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
		aMap.setOnMarkerClickListener(this);
	}
	
	private void setUpViews() {
		CarMapActivity parentActivity = (CarMapActivity) getActivity();
		resideMenu = parentActivity.getResideMenu();

		// add gesture operation's ignored views
		mapView = (MapView) parentView.findViewById(R.id.map);
		resideMenu.addIgnoredView(mapView);

		mapBtnLeft = (ImageView) parentView.findViewById(R.id.map_btn_left);
		mapBtnLeft.setOnClickListener(this);
		mapBtnRight = (ImageView) parentView.findViewById(R.id.map_btn_rifht);
		mapBtnRight.setOnClickListener(this);
		mapBtnType = (ImageView) parentView.findViewById(R.id.map_btn_type);
		mapBtnType.setVisibility(View.VISIBLE);
		mapBtnType.setOnClickListener(this);
		progressView = parentView.findViewById(R.id.progress_view);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		parentView = inflater.inflate(R.layout.fragment_car_map, container,
				false);
		setUpViews();
		return parentView;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mContext = this.getActivity().getApplicationContext();
		carWidth = (int) this.getResources().getDimension(
				R.dimen.car_icon_width);

		mapView.onCreate(savedInstanceState);

		mapInit();
//		getData();
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
		if(getDataTask != null){
			getDataTask.cancel(true);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
		
		if(markerList == null || markerList.size() == 0){
			getData();
		}
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.car_map, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_car_list:
			Intent intent = new Intent(mContext, CarListActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_refresh:
			getData();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.map_btn_left:
			position -= 1;
			showInfoWindow(position);
			break;
		case R.id.map_btn_rifht:
			position += 1;
			showInfoWindow(position);
			break;
		case R.id.map_btn_close:
			hideInfoWindow();
			break;
		case R.id.map_btn_carinfo:
			startMainActivity(MainActivity.FRAGMENT_FLAG_CARINFO);
			break;
		case R.id.map_btn_history:
			startMainActivity(MainActivity.FRAGMENT_FLAG_HISTORY);
			break;
		case R.id.map_btn_tracking:
			startMainActivity(MainActivity.FRAGMENT_FLAG_TRACKING);
			break;
		case R.id.map_btn_type:
			int mapType =aMap.getMapType();
			if(AMap.MAP_TYPE_NORMAL == mapType){
				aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
			}else{
				aMap.setMapType(AMap.MAP_TYPE_NORMAL);
			}
		default:
			break;
		}
	}
	
	private void startMainActivity(int fragmentFlag){
		Intent intent = new Intent(mContext, MainActivity.class);
		Car car = carList.get(position);
		intent.putExtra("imei", car.imei);
		intent.putExtra("fragment_flag", fragmentFlag);
		startActivity(intent);
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		CameraUpdate update = CameraUpdateFactory.changeLatLng(new LatLng(
				marker.getPosition().latitude, marker.getPosition().longitude));
		aMap.moveCamera(update);
		return false;
	}

	@Override
	public void onInfoWindowClick(Marker marker) {

		// CameraUpdate update = CameraUpdateFactory.newCameraPosition(new
		// CameraPosition(
		// new LatLng(marker.getPosition().latitude,
		// marker.getPosition().longitude), 18, 0, 0));
		// aMap.moveCamera(update);
	}

	@Override
	public View getInfoContents(Marker marker) {
		return null;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getInfoWindow(Marker marker) {
		View infoWindow = getActivity().getLayoutInflater().inflate(
				R.layout.custom_info_window, null);
		DisplayMetrics dm = getResources().getDisplayMetrics();

		int width = (int) (dm.widthPixels * 0.55); // 屏幕宽（像素，如：480px）

		TextView equipmentNameTV = (TextView) infoWindow.findViewById(R.id.equipmentName);
		TextView gpsTimeTV = (TextView) infoWindow.findViewById(R.id.gpsTime);
		TextView gpsPosTV = (TextView) infoWindow.findViewById(R.id.gpsPos);
		TextView statusTV = (TextView) infoWindow.findViewById(R.id.status);
		
		infoWindow.findViewById(R.id.map_btn_close).setOnClickListener(this);
		infoWindow.findViewById(R.id.map_btn_carinfo).setOnClickListener(this);
		infoWindow.findViewById(R.id.map_btn_history).setOnClickListener(this);
		infoWindow.findViewById(R.id.map_btn_tracking).setOnClickListener(this);

		String snippetString = marker.getSnippet();
		if (TextUtils.isEmpty(snippetString)) {
			snippetString = "no snippet";
		}
		

		int i = Integer.parseInt(snippetString);
		position = i;
		Car car = carList.get(position);
		String nickName = car.equipmentNickName;
		String name = car.equipmentName;
		if(nickName == null || nickName.isEmpty()){
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

	private void showInfoWindow(int position) {
		if (position < 0) {
			position = carList.size() - 1;
		} else if (position >= carList.size()) {
			position = 0;
		}
		Car car = carList.get(position);
		CameraUpdate update = CameraUpdateFactory.changeLatLng(new LatLng(
				car.lat, car.lng));
		aMap.moveCamera(update);
		markerList.get(position).showInfoWindow();
	}

	private void hideInfoWindow() {
		markerList.get(position).hideInfoWindow();
	}

	private MarkerOptions createCarMarkerOptions(Car car, int i) {
		LatLng latLng = new LatLng(car.lat, car.lng);
		MarkerOptions markerOptions = new MarkerOptions();
		ImageView imageView = new ImageView(mContext);
		imageView.setImageResource(car.equipmentStatueFlag == 0 ? R.drawable.car_red : R.drawable.car_yellow);
//		imageView.setRotation((float) car.course);
		imageView.setLayoutParams(new LayoutParams(carWidth, carWidth));
		BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromView(imageView);

		// 设置Marker的图标样式
		markerOptions.icon(bitmapDescriptor);
		// 设置Marker点击之后显示的标题
		markerOptions.title(car.gpsPos);
		markerOptions.snippet(i + "");
		// 设置Marker的坐标，为我们点击地图的经纬度坐标
		markerOptions.position(latLng);
		// 设置Marker的可见性
		markerOptions.visible(true);
		// 设置Marker是否可以被拖拽，这里先设置为false，之后会演示Marker的拖拽功能
		markerOptions.draggable(false);
		
		markerOptions.anchor(0.5f, 0.5f);
		return markerOptions;
	}

	private void getData() {
		aMap.clear();
		markerList.clear();
		
		mapBtnLeft.setVisibility(View.GONE);
		mapBtnRight.setVisibility(View.GONE);
		progressView.setVisibility(View.VISIBLE);

		if(getDataTask != null){
			getDataTask.cancel(true);
		}
		getDataTask = new GetDataTask();
		getDataTask.execute();
	}

	public final static String fileName = "carList.json";
	private ArrayList<Car> carList = null;
	private ArrayList<Marker> markerList = new ArrayList<Marker>();

	private class GetDataTask extends AsyncTask<String, Void, CarMapResponseVO> {

		@Override
		protected CarMapResponseVO doInBackground(String... params) {

			// String jsonStr = JSONHelper.getJson(getBaseContext(), fileName);
			String jsonStr = HttpClientUtil.getCarMapData(null);
			CarMapResponseVO responseVO = JsonUtil.parseObject(jsonStr,
					CarMapResponseVO.class);
			System.out.println(JsonUtil.toJSON(responseVO));
			return responseVO;
		}

		@Override
		protected void onPostExecute(CarMapResponseVO result) {
			if(progressView != null){
				progressView.setVisibility(View.GONE);
			}
			
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
			
			carList = result.rows;
			MarkerOptions markerOptions;
			Marker marker;
			Car car;
			for (int i = 0; i < carList.size(); i++) {
				car = carList.get(i);
				markerOptions = createCarMarkerOptions(car, i);
				if(aMap != null){
					marker = aMap.addMarker(markerOptions);
					marker.setRotateAngle(360 - (float) car.course%360);
					markerList.add(marker);
					if (i == 0) {
						position = i;
						showInfoWindow(position);
					}
				}
			}
			

			if(mapBtnLeft != null){
				mapBtnLeft.setVisibility(View.VISIBLE);
			}
			if(mapBtnRight != null){
				mapBtnRight.setVisibility(View.VISIBLE);
			}

			super.onPostExecute(result);
		}
	}
}
