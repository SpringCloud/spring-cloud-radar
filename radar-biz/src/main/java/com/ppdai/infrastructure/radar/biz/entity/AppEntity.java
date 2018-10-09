package com.ppdai.infrastructure.radar.biz.entity;

/// <summary>
///  
/// </summary>
public class AppEntity extends BaseEntity {
	/// <summary>
	/// 表示有运维统一分配的app_id
	/// </summary>
	private String candAppId;
	/// <summary>
	///
	/// </summary>
	private String appName;
	/// <summary>
	///
	/// </summary>
	private String departmentId;
	/// <summary>
	///
	/// </summary>
	private String departmentName;
	/// <summary>
	///
	/// </summary>
	private String ownerName;
	/// <summary>
	/// 逗号隔开
	/// </summary>
	private String ownerId;
	/// <summary>
	///
	/// </summary>
	private String ownerEmail;
	/// <summary>
	///
	/// </summary>
	private String memberId;
	/// <summary>
	///
	/// </summary>
	private String memberName;
	/// <summary>
	///
	/// </summary>
	private String memberEmail;
	/// <summary>
	/// 是否允许跨集群
	/// </summary>
	private int allowCross;
	
	private long version;
	
	/// <summary>
	/// 应用域名
	/// </summary>
	private String domain;
	/*
	 * 是否开启告警
	 * */
	private int alarm;
	
	public int getAlarm() {
		return alarm;
	}

	public void setAlarm(int alarm) {
		this.alarm = alarm;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	/// <summary>
	/// 表示有运维统一分配的app_id
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
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName1) {
		appName = appName1;
	}

	/// <summary>
	///
	/// </summary>
	public String getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(String departmentId1) {
		departmentId = departmentId1;
	}

	/// <summary>
	///
	/// </summary>
	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName1) {
		departmentName = departmentName1;
	}

	/// <summary>
	///
	/// </summary>
	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName1) {
		ownerName = ownerName1;
	}

	/// <summary>
	/// 逗号隔开
	/// </summary>
	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId1) {
		ownerId = ownerId1;
	}

	/// <summary>
	///
	/// </summary>
	public String getOwnerEmail() {
		return ownerEmail;
	}

	public void setOwnerEmail(String ownerEmail1) {
		ownerEmail = ownerEmail1;
	}

	/// <summary>
	///
	/// </summary>
	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId1) {
		memberId = memberId1;
	}

	/// <summary>
	///
	/// </summary>
	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName1) {
		memberName = memberName1;
	}

	/// <summary>
	///
	/// </summary>
	public String getMemberEmail() {
		return memberEmail;
	}

	public void setMemberEmail(String memberEmail1) {
		memberEmail = memberEmail1;
	}

	/// <summary>
	/// 是否允许跨集群
	/// </summary>
	public int getAllowCross() {
		return allowCross;
	}

	public void setAllowCross(int allowCross1) {
		allowCross = allowCross1;
	}
}
