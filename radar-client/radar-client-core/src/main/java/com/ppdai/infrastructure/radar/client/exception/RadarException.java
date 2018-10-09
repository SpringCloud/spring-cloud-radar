package com.ppdai.infrastructure.radar.client.exception;

/**
 * Created by zhangyicong on 17-12-19.
 */
@SuppressWarnings("serial")
public class RadarException extends RuntimeException {

    public RadarException(String msg) {
        super(msg);
    }

    public RadarException(String msg, Throwable t) {
        super(msg, t);
    }
}
