package com.ppdai.infrastructure.radar.biz.dto.pub;

//发布系统获取实例对象

public class GetStatusInstanceDto {
	private long id;	
	private String candInstanceId;
	private String ip;
	private int port;
	private int heartStatus;
	private int pubStatus;
	private int instanceStatus;	
	private int supperStatus;	
	private int status;
	
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
	public int getHeartStatus() {
		return heartStatus;
	}
	public void setHeartStatus(int heartStatus) {
		this.heartStatus = heartStatus;
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
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
}
