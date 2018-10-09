package com.ppdai.infrastructure.radar.client;

import java.io.IOException;

import org.junit.Test;

import com.ppdai.infrastructure.radar.biz.common.util.PropUtil;

/**
 * Created by zhangyicong on 17-12-14.
 */
public class PropUtilTest {

    @Test
    public void testPropUtil() throws IOException {        
        String version = PropUtil.getSdkVersion();
        System.out.println("version: " + version);
    }
}
