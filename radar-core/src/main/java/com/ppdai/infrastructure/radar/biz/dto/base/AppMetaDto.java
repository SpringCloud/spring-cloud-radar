package com.ppdai.infrastructure.radar.biz.dto.base;

import java.util.Date;

//@Data
public class AppMetaDto {
	private long id;
	private String name;
	private String candAppId;
	private long version;

	// 数据库更新时间
	private Date updateTime;

	// 数据库更新时间
	private Date cacheTime;

	private String msg;
	private String domain;
	private int allowCross;

	public int getAllowCross() {
		return allowCross;
	}

	public void setAllowCross(int allowCross) {
		this.allowCross = allowCross;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCandAppId() {
		return candAppId;
	}

	public void setCandAppId(String candAppId) {
		this.candAppId = candAppId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Date getCacheTime() {
		return cacheTime;
	}

	public void setCacheTime(Date cacheTime) {
		this.cacheTime = cacheTime;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
