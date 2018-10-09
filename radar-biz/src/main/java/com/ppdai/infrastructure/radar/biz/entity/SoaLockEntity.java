
package com.ppdai.infrastructure.radar.biz.entity;

import java.util.Date;

/// <summary>
///  
/// </summary>
public class SoaLockEntity extends BaseEntity {
	/// <summary>
	///
	/// </summary>
	private String ip;
	/// <summary>
	/// 需要加锁的key
	/// </summary>
	private String key1;
	/// <summary>
	///
	/// </summary>
	private Date heartTime;
	/// <summary>
	///注意此字段不是数据库字段
	/// </summary>
	private Date dbNow;
	
	public Date getDbNow() {
		return dbNow;
	}

	public void setDbNow(Date dbNow) {
		this.dbNow = dbNow;
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
	/// 需要加锁的key
	/// </summary>
	public String getKey1() {
		return key1;
	}

	public void setKey1(String key11) {
		key1 = key11;
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
}
