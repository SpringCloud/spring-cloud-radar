package com.ppdai.infrastructure.radar.biz.common.trace.spi;


public interface MessageProducerManager {
  /**
   * @return the message producer
   */
  MessageProducer getProducer();
}
