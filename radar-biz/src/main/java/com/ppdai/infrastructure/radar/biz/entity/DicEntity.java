package com.ppdai.infrastructure.radar.biz.entity;

/// <summary>
///  
/// </summary>
public class DicEntity extends BaseEntity {
	/// <summary>
	/// 键
	/// </summary>
	private String key1;
	/// <summary>
	/// 值
	/// </summary>
	private String value;
	/// <summary>
	/// 备注
	/// </summary>
	private String remark;

	/// <summary>
	/// 键
	/// </summary>
	public String getKey1() {
		return key1;
	}

	public void setKey1(String key11) {
		key1 = key11;
	}

	/// <summary>
	/// 值
	/// </summary>
	public String getValue() {
		return value;
	}

	public void setValue(String value1) {
		value = value1;
	}

	/// <summary>
	/// 备注
	/// </summary>
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark1) {
		remark = remark1;
	}
}
