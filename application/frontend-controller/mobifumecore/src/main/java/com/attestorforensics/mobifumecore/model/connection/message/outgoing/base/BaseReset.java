package com.attestorforensics.mobifumecore.model.connection.message.outgoing.base;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;

public class BaseReset implements OutgoingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/cmd/";

  private final String deviceId;

  private BaseReset(String deviceId) {
    this.deviceId = deviceId;
  }

  public static BaseReset create(String deviceId) {
    return new BaseReset(deviceId);
  }

  @Override
  public String topic() {
    return TOPIC_PREFIX + deviceId;
  }

  @Override
  public String payload() {
    return "R;1";
  }
}
