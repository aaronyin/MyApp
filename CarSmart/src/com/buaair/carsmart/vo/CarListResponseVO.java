package com.buaair.carsmart.vo;

import java.util.ArrayList;

import com.buaair.carsmart.entity.Car;

public class CarListResponseVO extends BaseResponseVO{

	
	/**
	 * 总页数
	 */
	public int total;
	/**
	 * 当前页
	 */
	public int page;
	/**
	 * 总数
	 */
	public int records;

	/**
	 * 数据列表
	 */
	public ArrayList<Car> rows;

}
