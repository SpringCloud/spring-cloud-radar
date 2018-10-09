package com.ppdai.infrastructure.radar.biz.dto.pub;

import java.util.List;

import com.ppdai.infrastructure.radar.biz.dto.BaseRequest;

public class AdjustRequest  extends BaseRequest {
	//外部关联id
	private List<String> candInstanceIds;	
	//数据库主键id
	private List<Long> ids;
//	private int slot;//0，表示发布，1,表示服务启动停止
	//拉入拉出状态，true 表示拉入， false 表示拉出 
	private boolean up;
	
	public List<String> getCandInstanceIds() {
		return candInstanceIds;
	}
	public void setCandInstanceIds(List<String> candInstanceIds) {
		this.candInstanceIds = candInstanceIds;
	}
	public List<Long> getIds() {
		return ids;
	}
	public void setIds(List<Long> ids) {
		this.ids = ids;
	}
//	public int getSlot() {
//		return slot;
//	}
//	public void setSlot(int slot) {
//		this.slot = slot;
//	}
	public boolean isUp() {
		return up;
	}
	public void setUp(boolean up) {
		this.up = up;
	}
}
