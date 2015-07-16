package com.buaair.carsmart.vo;

import java.util.ArrayList;

import com.buaair.carsmart.entity.Car;

public class CarMapResponseVO extends BaseResponseVO{

	/**
	 * 总数
	 */
	public int records;

	/**
	 * 数据列表
	 */
	public ArrayList<Car> rows;

}
