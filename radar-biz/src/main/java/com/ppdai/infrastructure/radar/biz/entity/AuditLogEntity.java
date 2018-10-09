package com.ppdai.infrastructure.radar.biz.entity;

/// <summary>
///  审计日志
/// </summary>
public class AuditLogEntity extends BaseEntity {
	/// <summary>
	/// 名称
	/// </summary>
	private String tbName;
	/// <summary>
	/// 外键id
	/// </summary>
	private long refId;
	/// <summary>
	/// 内容
	/// </summary>
	private String content;

	/// <summary>
	/// 名称
	/// </summary>
	public String getTbName() {
		return tbName;
	}

	public void setTbName(String tbName1) {
		tbName = tbName1;
	}

	/// <summary>
	/// 外键id
	/// </summary>
	public long getRefId() {
		return refId;
	}

	public void setRefId(long refId1) {
		refId = refId1;
	}

	/// <summary>
	/// 内容
	/// </summary>
	public String getContent() {
		return content;
	}

	public void setContent(String content1) {
		content = content1;
	}
}
