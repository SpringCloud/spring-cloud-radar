
package com.ppdai.infrastructure.radar.biz.entity;

/// <summary>
///  
/// </summary>
public class TaskEntity extends BaseEntity {
	/// <summary>
	///
	/// </summary>
	private long appId;
	/// <summary>
	///
	/// </summary>
	private String msg;

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
	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg1) {
		msg = msg1;
	}
}
