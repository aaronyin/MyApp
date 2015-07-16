package com.buaair.carsmart.entity;

import java.io.Serializable;

public class Car implements Serializable {

	public static final long serialVersionUID = 1L;

	/**
	 * 设备IMEI号
	 */
	public String imei;

	/**
	 * 设备名称
	 */
	public String equipmentNickName;

	/**
	 * 车牌号
	 */
	public String equipmentName;

	/**
	 * 车辆识别号
	 */
	public String equipmentNum;

	/**
	 * 0:正常数据 1:异常数据
	 */
	public int equipmentInfo;
	
	/**
	 * 状态
	 */
	public int equipmentStatueFlag;
	
	/**
	 * 状态信息
	 */
	public String equipmentStatueInfo;

	/**
	 * SIM卡号
	 */
	public String sim;// SIM卡号
	/**
	 * 车主姓名
	 */
	public String ownerName;// 车主姓名
	/**
	 * 车主联系电话
	 */
	public String ownerPhone;// 车主联系电话
	/**
	 * 车主住址
	 */
	public String ownerAddress;// 车主住址
	/**
	 * 终端类型
	 */
	public String terminalType;// 终端类型
	/**
	 * 终端型号
	 */
	public String terminalModel;// 终端型号
	/**
	 * 开通时间
	 */
	public String in_time;// uint 开通时间
	/**
	 * 到期时间
	 */
	public String out_time;// uint 到期时间

	/**
	 * 定位时间 GPS定位时间 UTC秒数(如果设备过期，值为0)
	 */
	public String gpsTime;

	/**
	 * Gps数据的系统时间 UTC秒数(如果设备过期，值为0)
	 */
	public String sysTime;

	/**
	 * 当前服务器时间 UTC秒数(如果设备过期，值为0)
	 */
	public long serverTime;

	/**
	 * 经度 (如果设备过期，值为0)
	 */
	public double lng;

	/**
	 * 纬度 (如果设备过期，值为0)
	 */
	public double lat;

	/**
	 * 定位位置
	 */
	public String gpsPos;

	/**
	 * 航向(正北方向为0度，顺时针方向增大。最大值360度) (如果设备过期，值为0)
	 */
	public double course;

	/**
	 * 速度 (单位:km/h) (如果设备过期，值为0)
	 */
	public double speed;

	//
	// /**
	// * 车辆名称
	// */
	// public String name;
	// /**
	// * 车牌号
	// */
	// public String num;
	// /**
	// * 终端编号
	// */
	// public String terminalNum;
	// /**
	// * 当前位置
	// */
	// public String address;

	//	/**
	//	 * 数据更新时间
	//	 */
	//	public String updateTime;
}
