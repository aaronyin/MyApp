package com.buaair.carsmart.entity;

public class Login {

	/**
	 * 后续接口访问的访问令牌
	 */
	public String access_token;
	/**
	 * access token的有效期，以秒为单位
	 */
	public long expires_in;
	
	public String account;

}
