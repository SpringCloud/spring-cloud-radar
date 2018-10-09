package com.ppdai.infrastructure.radar.biz.dto.base;

import java.util.Date;
import java.util.Map;

//@Data
public class InstanceDto {
	private long id;
	// private long appId;
	// private String appName;
	// private String candAppId;
	// private long appClusterId;
	// private String appClusterName;
	private String candInstanceId;
	private String ip;
	private int port;
	private int pubStatus;
	private int instanceStatus;
	private int supperStatus;
	private int heartStatus;
	private Date heartTime;
	private boolean status;
	private int weight;
	private String servName;
	private Map<String, String> tag;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCandInstanceId() {
		return candInstanceId;
	}

	public void setCandInstanceId(String candInstanceId) {
		this.candInstanceId = candInstanceId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPubStatus() {
		return pubStatus;
	}

	public void setPubStatus(int pubStatus) {
		this.pubStatus = pubStatus;
	}

	public int getInstanceStatus() {
		return instanceStatus;
	}

	public void setInstanceStatus(int instanceStatus) {
		this.instanceStatus = instanceStatus;
	}

	public int getSupperStatus() {
		return supperStatus;
	}

	public void setSupperStatus(int supperStatus) {
		this.supperStatus = supperStatus;
	}

	public int getHeartStatus() {
		return heartStatus;
	}

	public void setHeartStatus(int heartStatus) {
		this.heartStatus = heartStatus;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getServName() {
		return servName;
	}

	public void setServName(String servName) {
		this.servName = servName;
	}

	public Map<String, String> getTag() {
		return tag;
	}

	public void setTag(Map<String, String> tag) {
		this.tag = tag;
	}

	public Date getHeartTime() {
		return heartTime;
	}

	public void setHeartTime(Date heartTime) {
		this.heartTime = heartTime;
	}
}
