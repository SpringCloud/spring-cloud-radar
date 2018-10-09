package com.ppdai.infrastructure.radar.biz.common.trace.internals;

import com.ppdai.infrastructure.radar.biz.common.trace.spi.MessageProducer;
import com.ppdai.infrastructure.radar.biz.common.trace.spi.MessageProducerManager;


public class NullMessageProducerManager implements MessageProducerManager {
  private static final MessageProducer PRODUCER = new NullMessageProducer();

  @Override
  public MessageProducer getProducer() {
    return PRODUCER;
  }
}
