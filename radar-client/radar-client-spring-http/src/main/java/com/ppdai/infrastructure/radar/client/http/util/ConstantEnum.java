package com.ppdai.infrastructure.radar.client.http.util;

public enum ConstantEnum {
	/**
	 *  常量枚举值
	 */
	ConnectTimeout("soa.connect.timeout.global", 1000),
	SocketTimeout("soa.socket.timeout.global",	2000), 
	RequestTimeout("soa.request.timeout.global", 20000),
	MaxConnection("soa.max.connections",500), 
	MaxRouteConnection("soa.max.route.connections", 30),
	ReadTimeout("soa.read.timeout.global",20000);
	private String name;

	private int value;

	private ConstantEnum(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}
}
