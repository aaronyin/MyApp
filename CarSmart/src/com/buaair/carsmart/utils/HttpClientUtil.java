package com.buaair.carsmart.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.buaair.carsmart.entity.Login;

public class HttpClientUtil {

	private static boolean DEBUG = true;

	/**
	 * 执行一个HTTP GET请求，返回请求响应的HTML
	 * 
	 * @param url
	 *            请求的URL地址
	 * @param queryString
	 *            请求的查询参数,可以为null
	 * @return 返回请求响应的HTML
	 */
	public static String doGet(String url, Map<String, String> paramsMap) {
		if(DEBUG) LogUtil.d("doGet url : " + url);
		HttpGet httpRequest = new HttpGet(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		Iterator<String> its = paramsMap.keySet().iterator();
		while (its.hasNext()) {
			String key = (String) its.next();
			String value = paramsMap.get(key);
			params.add(new BasicNameValuePair(key, value));
		}

		try {

			// 取得默认的HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			// 取得HttpResponse
			HttpResponse httpResponse = httpclient.execute(httpRequest);

			// HttpStatus.SC_OK表示连接成功
//			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 取得返回的字符串
				String strResult = EntityUtils.toString(httpResponse
						.getEntity());
				if(DEBUG) LogUtil.d("result : " + strResult);
				return strResult;
//			} else {
//				return "请求错误!";
//			}
		} catch (ClientProtocolException e) {
			LogUtil.e("请求错误", e);
		} catch (IOException e) {
			LogUtil.e("请求错误", e);
		} catch (Exception e) {
			LogUtil.e("请求错误", e);
		}
		return "请求错误";
	}

	/**
	 * 执行一个HTTP POST请求，返回请求响应的HTML
	 * 
	 * @param url
	 *            请求的URL地址
	 * @param params
	 *            请求的查询参数,可以为null
	 * @param charset
	 *            字符集
	 * @param pretty
	 *            是否美化
	 * @return 返回请求响应的HTML
	 */
	public static String doPost(String url, Map<String, String> paramsMap,
			String charset, boolean pretty) {
		if(DEBUG) LogUtil.d("doPost url : " + url);
		HttpPost httpRequest = new HttpPost(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		Iterator<String> its = paramsMap.keySet().iterator();
		while (its.hasNext()) {
			String key = (String) its.next();
			String value = paramsMap.get(key);
			params.add(new BasicNameValuePair(key, value));
		}

		try {
			if (charset == null) {
				charset = "gb2312";
			}
			// 设置字符集
			HttpEntity httpentity = new UrlEncodedFormEntity(params, charset);

			// 请求httpRequest
			httpRequest.setEntity(httpentity);

			// 取得默认的HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			// 取得HttpResponse
			HttpResponse httpResponse = httpclient.execute(httpRequest);

			// HttpStatus.SC_OK表示连接成功
//			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 取得返回的字符串
				String strResult = EntityUtils.toString(httpResponse
						.getEntity());
				if(DEBUG) LogUtil.d("result : " + strResult);
				return strResult;
//			} else {
//				return "请求错误!";
//			}
		} catch (ClientProtocolException e) {
			LogUtil.e("请求错误", e);
		} catch (IOException e) {
			LogUtil.e("请求错误", e);
		} catch (Exception e) {
			LogUtil.e("请求错误", e);
		}
		return "请求错误";
	}

	private static String URL = "http://api.bdzhdw.com";
	/**
	 * 登录链接
	 */
	public static String URL_LOGIN = "/api/Account/Login";
	
	/**
	 * 获取一个当前账户名下所有车辆运行及位置数据
	 */
	public static String URL_CAR_MAP_DATA = "/api/ApiReceiveData/GetReceiveDataMap";
	
	/**
	 * 车辆列表链接
	 */
	public static String URL_CAR_LIST = "/api/ApiReceiveData/GetEquipmentDataLasts";
	
	/**
	 * 追车模式
	 */
	public static String URL_TRACKING = "/api/ApiReceiveData/GetSingleEquipmentDataLast";
	
	/**
	 * 历史轨迹
	 */
	public static String URL_HISTORY = "/api/ApiReceiveData/GetSingleEquipmentDataHistory";
	
	/**
	 * 设备详细信息
	 */
	public static String URL_CAR_DETAIL =   "/api/ApiEquipment/EquipmentDetail";
	
	/**
	 * 获取设备最后一条指令
	 */
	public static String URL_COMMAND_LAST = "/api/ApiTerminalEquipmentCommand/GetSingletCommandLast";
	
	/**
	 * 发送指令
	 */
	public static String URL_SEND_COMMAND = "/api/ApiTerminalEquipmentCommand/SendSingleCommand";
	
	public static Login loginInfo;
	
	public static String getParam(){
		String url = "&access_token=" + loginInfo.access_token
				   + "&account=" + loginInfo.account
				   + "&time=" + System.currentTimeMillis()/1000;
	  
	  return url;
	}
	
	public static String login(String userName, String password) {
		String url = URL+URL_LOGIN + "?userName=" + userName
								   + "&password=" + password;
		Map<String, String> paramsMap = new HashMap<String, String>();
		String result = HttpClientUtil.doPost(url, paramsMap, "UTF-8", true);
		
		return result;
	}
	
	
	public static String getCarMapData(String map_type){
		if(loginInfo == null){
			return null;
		}
		
		String url = URL+URL_CAR_MAP_DATA + "?" + getParam();
		
		if(map_type != null){
			url += "&map_type=" + map_type;
		}

		Map<String, String> paramsMap = new HashMap<String, String>();
		String result = HttpClientUtil.doGet(url, paramsMap);
		return result;
	}
	
	/**
	 * 获取一个当前账户名下所有车辆运行及位置数据
	 * @param map_type	如果要显示在百度地图上，map_type=BAIDU此时返回的经纬度将经过baidu校准方式校准
						如果要显示在google地图上，map_type=GOOGLE，此时返回的经纬度将经过google校准方式校准
						map_type如果不填，则返回原始经纬度
	 * @param searchString	搜索内容，按照车牌号，设备名称检索
	 * @param isOnline	在线状态，2全部，0离线，1在线	默认为2
	 * @param page	当前页 默认1
	 * @param rows	每页显示记录数 默认10
	 * @return
	 */
	public static String getCarList(String map_type, String searchString,int isOnline, int page,int rows){
		if(loginInfo == null){
			return null;
		}
		
		String url = URL+URL_CAR_LIST + "?" + getParam()
									  + "&isOnline=" + isOnline
									  + "&page=" + page
									  + "&rows=" + rows;
		if(map_type != null){
			url += "&map_type=" + map_type;
		}
		if(searchString != null){
			url += "&searchString=" + searchString;
		}

		Map<String, String> paramsMap = new HashMap<String, String>();
		String result = HttpClientUtil.doGet(url, paramsMap);
		return result;
	}
	
	/**
	 * 追车模式 - 获取单个车辆/终端的最新位置信息
	 * @param imei
	 * @param map_type	如果要显示在百度地图上，map_type=BAIDU此时返回的经纬度将经过baidu校准方式校准
						如果要显示在google地图上，map_type=GOOGLE，此时返回的经纬度将经过google校准方式校准
						map_type如果不填，则返回原始经纬度
	 * @return
	 */
	public static String getCarTracking(String imei, String map_type){
		if(loginInfo == null){
			return null;
		}
		String url = URL+URL_TRACKING +"?"+getParam()
									  +"&imei=" + imei;
		
		if(map_type != null){
			url += "&map_type=" + map_type;
		}

		Map<String, String> paramsMap = new HashMap<String, String>();
		String result = HttpClientUtil.doGet(url, paramsMap);
		return result;
	}
	
	/**
	 * 历史轨迹 - 获取设备历史轨迹位置信息
	 * @param imei
	 * @param map_type 		如果要显示在百度地图上，map_type=BAIDU此时返回的经纬度将经过baidu校准方式校准
							如果要显示在google地图上，map_type=GOOGLE，此时返回的经纬度将经过google校准方式校准
							map_type如果不填，则返回原始经纬度
	 * @param begin_time	开始时间(UTC) 秒数
	 * @param end_time		结束时间(UTC) 秒数
	 * @param limit			每次请求数据数量(一次最大1000条)
	 * @return
	 */
	public static String getCarHistory(String imei, String map_type, long begin_time, long end_time, int limit){
		if(loginInfo == null){
			return null;
		}
		String url = URL+URL_HISTORY +"?"+getParam()
									  +"&imei=" + imei
									  +"&begin_time=" + begin_time
									  +"&end_time=" + end_time
									  +"&limit=" + limit;
		
		if(map_type != null){
			url += "&map_type=" + map_type;
		}

		Map<String, String> paramsMap = new HashMap<String, String>();
		String result = HttpClientUtil.doGet(url, paramsMap);
		return result;
	}

	/**
	 * 获取设备信息 - 查询IMEI设备的详细信息
	 * @param imei
	 * @return
	 */
	public static String getCarDetail(String imei){
		if(loginInfo == null){
			return null;
		}
		String url = URL+URL_CAR_DETAIL 
									  + "?"+getParam()
									  + "&imei=" + imei;

		Map<String, String> paramsMap = new HashMap<String, String>();
		String result = HttpClientUtil.doGet(url, paramsMap);
		return result;
	}
	
	/**
	 * 查询单台设备最后一条指令信息
	 * @param imei
	 * @return
	 */
	public static String getCommandLast(String imei){
		if(loginInfo == null){
			return null;
		}
		String url = URL+URL_COMMAND_LAST 
									  + "?"+getParam()
									  + "&imei=" + imei;

		Map<String, String> paramsMap = new HashMap<String, String>();
		String result = HttpClientUtil.doGet(url, paramsMap);
		return result;
	}
	
	/**
	 * 发送指令
	 * @param imei
	 * @param workStatue 激活状态  0-不设置 3-已激活
	 * @param workModel	1:标准；2：精准，3：追车   默认为2
	 * @param timer	定时启动时间，（0-24）对应（1：00-00：00），0为不设置此值  默认为0
	 * @param postionCount	定位次数(标准、精准模式下有效) 默认为1
	 * @param intervalTime	追车间隔时间单位为秒（追车模式下有效） 默认为600
	 * @return
	 */
	public static String sendCommand(String imei,int workStatue, int workModel, int timer, int postionCount, int intervalTime){
		if(loginInfo == null){
			return null;
		}
		
		String url = URL+URL_SEND_COMMAND 
									  + "?"+getParam()
									  + "&imei=" + imei
									  + "&workStatue=" + workStatue
									  + "&workModel=" + workModel
									  + "&timer=" + timer 
									  + "&postionCount=" + postionCount
									  + "&intervalTime=" + intervalTime;
		Map<String, String> paramsMap = new HashMap<String, String>();
		String result = HttpClientUtil.doPost(url, paramsMap, "UTF-8", true);
		return result;
	}
}
