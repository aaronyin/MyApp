package com.buaair.carsmart.entity;

public class Command {

	/**
	 * imei号
	 */
	public String imei;
	/**
	 * 命令详情
	 */
	public String sendDetail;
	/**
	 * 命令状态，1:等待发送中; 2:命令已下发; 3:命令执行成功; 4:命令执行失败;5:命令已失效;
	 */
	public String operateStatue;
	/**
	 * 操作人
	 */
	public String operateUser;
	/**
	 * 操作时间
	 */
	public String operateTime;
	/**
	 * 执行时间
	 */
	public String successOperateTime;

	/**
	 * 态命令设置结果，0设置失败，1设置成功
	 */
	public String status;
}
