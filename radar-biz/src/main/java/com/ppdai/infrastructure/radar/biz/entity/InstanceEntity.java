
package com.ppdai.infrastructure.radar.biz.entity;

import java.util.Date;

/// <summary>
///  
/// </summary>
public class InstanceEntity extends BaseEntity {
	/// <summary>
	///
	/// </summary>
	private long appId;
	/// <summary>
	///
	/// </summary>
	private String appName;
	/// <summary>
	///
	/// </summary>
	private String candAppId;
	/// <summary>
	///
	/// </summary>
	private long appClusterId;
	/// <summary>
	///
	/// </summary>
	private String appClusterName;
	/// <summary>
	///
	/// </summary>
	private String candInstanceId;
	/// <summary>
	///
	/// </summary>
	private String ip;
	/// <summary>
	///
	/// </summary>
	private int port;
	/// <summary>
	///
	/// </summary>
	private String lan;
	/// <summary>
	///
	/// </summary>
	private String sdkVersion;
	/// <summary>
	/// 发布槽位0,1
	/// </summary>
	private int pubStatus;
	/// <summary>
	/// 应用槽位0,1
	/// </summary>
	private int instanceStatus;
	/// <summary>
	/// 超级槽位-1,0,1,-1 表示强制关闭，0表示忽略，1表示强制开启
	/// </summary>
	private int supperStatus;
	/// <summary>
	/// 预留扩展槽位
	/// </summary>
	private int extendStatus1;
	/// <summary>
	/// 预留扩展槽位
	/// </summary>
	private int extendStatus2;
	/// <summary>
	///
	/// </summary>
	private Date heartTime;
	/// <summary>
	/// 因为心跳字段是一个很特殊的字段，后面需要做异常检查的，所以需要单独拿出来。
	/// </summary>
	private int heartStatus;
	/// <summary>
	/// 权重
	/// </summary>
	private int weight;
	/// <summary>
	/// 实例服务名列表
	/// </summary>
	private String servName;
	/// <summary>
	/// 程序自动上传的tag，存的key value的json
	/// </summary>
	private String tag;
	/// <summary>
	/// 用户界面操作的tag，为了区别程序自动上报的tag，用两个字段表示同时存的key value的json
	/// </summary>
	private String tag1;

	/// <summary>
	///
	/// </summary>
	public long getAppId() {
		return appId;
	}

	public void setAppId(long appId1) {
		appId = appId1;
	}

	/// <summary>
	///
	/// </summary>
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName1) {
		appName = appName1;
	}

	/// <summary>
	///
	/// </summary>
	public String getCandAppId() {
		return candAppId;
	}

	public void setCandAppId(String candAppId1) {
		candAppId = candAppId1;
	}

	/// <summary>
	///
	/// </summary>
	public long getAppClusterId() {
		return appClusterId;
	}

	public void setAppClusterId(long appClusterId1) {
		appClusterId = appClusterId1;
	}

	/// <summary>
	///
	/// </summary>
	public String getAppClusterName() {
		return appClusterName;
	}

	public void setAppClusterName(String appClusterName1) {
		appClusterName = appClusterName1;
	}

	/// <summary>
	///
	/// </summary>
	public String getCandInstanceId() {
		return candInstanceId;
	}

	public void setCandInstanceId(String candInstanceId1) {
		candInstanceId = candInstanceId1;
	}

	/// <summary>
	///
	/// </summary>
	public String getIp() {
		return ip;
	}

	public void setIp(String ip1) {
		ip = ip1;
	}

	/// <summary>
	///
	/// </summary>
	public int getPort() {
		return port;
	}

	public void setPort(int port1) {
		port = port1;
	}

	/// <summary>
	///
	/// </summary>
	public String getLan() {
		return lan;
	}

	public void setLan(String lan1) {
		lan = lan1;
	}

	/// <summary>
	///
	/// </summary>
	public String getSdkVersion() {
		return sdkVersion;
	}

	public void setSdkVersion(String sdkVersion1) {
		sdkVersion = sdkVersion1;
	}

	/// <summary>
	/// 发布槽位0,1
	/// </summary>
	public int getPubStatus() {
		return pubStatus;
	}

	public void setPubStatus(int pubStatus1) {
		pubStatus = pubStatus1;
	}

	/// <summary>
	/// 应用槽位0,1
	/// </summary>
	public int getInstanceStatus() {
		return instanceStatus;
	}

	public void setInstanceStatus(int instanceStatus1) {
		instanceStatus = instanceStatus1;
	}

	/// <summary>
	/// 超级槽位-1,0,1,-1 表示强制关闭，0表示忽略，1表示强制开启
	/// </summary>
	public int getSupperStatus() {
		return supperStatus;
	}

	public void setSupperStatus(int supperStatus1) {
		supperStatus = supperStatus1;
	}

	/// <summary>
	/// 预留扩展槽位
	/// </summary>
	public int getExtendStatus1() {
		return extendStatus1;
	}

	public void setExtendStatus1(int extendStatus11) {
		extendStatus1 = extendStatus11;
	}

	/// <summary>
	/// 预留扩展槽位
	/// </summary>
	public int getExtendStatus2() {
		return extendStatus2;
	}

	public void setExtendStatus2(int extendStatus21) {
		extendStatus2 = extendStatus21;
	}

	/// <summary>
	///
	/// </summary>
	public Date getHeartTime() {
		return heartTime;
	}

	public void setHeartTime(Date heartTime1) {
		heartTime = heartTime1;
	}

	/// <summary>
	/// 因为心跳字段是一个很特殊的字段，后面需要做异常检查的，所以需要单独拿出来。
	/// </summary>
	public int getHeartStatus() {
		return heartStatus;
	}

	public void setHeartStatus(int heartStatus1) {
		heartStatus = heartStatus1;
	}

	/// <summary>
	/// 权重
	/// </summary>
	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight1) {
		weight = weight1;
	}

	/// <summary>
	/// 实例服务名列表
	/// </summary>
	public String getServName() {
		return servName;
	}

	public void setServName(String servName1) {
		servName = servName1;
	}

	/// <summary>
	/// 程序自动上传的tag，存的key value的json
	/// </summary>
	public String getTag() {
		return tag;
	}

	public void setTag(String tag1) {
		tag = tag1;
	}

	/// <summary>
	/// 用户界面操作的tag，为了区别程序自动上报的tag，用两个字段表示同时存的key value的json
	/// </summary>
	public String getTag1() {
		return tag1;
	}

	public void setTag1(String tag11) {
		tag1 = tag11;
	}
}
