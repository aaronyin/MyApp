package com.buaair.carsmart.entity;

import java.io.Serializable;

public class CarHistory implements Serializable {
	
	
	/**  
	 * serialVersionUID  
	 */  
	
	private static final long serialVersionUID = 1L;
	
	public double lat;
	public double lng;
	public double speed;
	public double course;
	public long gpsTime;
}
