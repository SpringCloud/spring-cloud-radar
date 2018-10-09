package com.ppdai.infrastructure.radar.biz.common.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by zhangyicong on 17-12-14.
 */
public class PropUtil {
	private static volatile String sdkVersion = null;
	public static String getSdkVersion() {
		if (sdkVersion == null) {
			synchronized (PropUtil.class) {
				if (sdkVersion == null) {
					try {
						String path = "/version.properties";
						InputStream stream = PropUtil.class.getResourceAsStream(path);
						Properties props = new Properties();
						props.load(stream);
						stream.close();
						sdkVersion = props.getProperty("version");
						return sdkVersion;
					} catch (Exception e) {
						throw new RuntimeException("获取skd version 异常", e);
					}
				}
			}
		}
		return sdkVersion;
	}

}
