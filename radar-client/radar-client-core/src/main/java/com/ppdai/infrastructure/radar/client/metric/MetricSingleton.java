package com.ppdai.infrastructure.radar.client.metric;

import com.codahale.metrics.MetricRegistry;

/**
 * Created by zhangyicong on 18-1-17.
 */
public class MetricSingleton {

    private MetricRegistry metricRegistry = new MetricRegistry();

    private MetricSingleton() {

    }

    /**
     * 懒加载单例帮助类
     */
    private static class SingletonHelper {
        private static final MetricSingleton INSTANCE = new MetricSingleton();
    }
    
    public static MetricRegistry getMetricRegistry() {
        return MetricSingleton.SingletonHelper.INSTANCE.metricRegistry;
    }
}
