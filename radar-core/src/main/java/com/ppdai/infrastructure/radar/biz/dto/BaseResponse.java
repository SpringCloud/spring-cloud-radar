package com.ppdai.infrastructure.radar.biz.dto;

public class BaseResponse {
	private boolean isSuc=true;
	private String code=RadarConstanst.YES;
	private String msg;

	public boolean isSuc() {
		return isSuc;
	}

	public void setSuc(boolean isSuc) {
		this.isSuc = isSuc;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
