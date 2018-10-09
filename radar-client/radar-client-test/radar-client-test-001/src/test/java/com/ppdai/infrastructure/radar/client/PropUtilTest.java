package com.ppdai.infrastructure.radar.client;

import com.ppdai.infrastructure.radar.client.utils.PropUtil;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by zhangyicong on 17-12-14.
 */
public class PropUtilTest {

    @Test
    public void testPropUtil() throws IOException {
        Properties props = PropUtil.loadProps();

        String lan = (String) props.get("lan");
        String version = (String) props.get("version");

        System.out.println("lan: " + lan);
        System.out.println("version: " + version);
    }
}
