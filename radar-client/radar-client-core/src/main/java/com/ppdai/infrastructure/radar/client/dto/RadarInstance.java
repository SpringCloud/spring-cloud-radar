package com.ppdai.infrastructure.radar.client.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by zhangyicong on 17-12-11.
 */
public class RadarInstance {
  //唯一外键
  private String candInstanceId;
  private String host;
  private int port;
  private String candAppId;
  private String appName;
  private String clusterName;
  private boolean up;
  private int weight;
  private Map<String, String> tags;

  private RadarInstance() {
  }

  public String getCandInstanceId() {
    return candInstanceId;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getCandAppId() {
    return candAppId;
  }

  public String getAppName() {
    return appName;
  }

  public String getClusterName() {
    return clusterName;
  }

  public boolean isUp() {
    return up;
  }

  public int getWeight() {
    return weight;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public static Builder getBuilder() {
    return new Builder();
  }

  public static class Builder {
    private RadarInstance instanceRadar;

    public Builder() {
      instanceRadar = new RadarInstance();
    }

    public Builder withCandInstanceId(String candInstanceId) {
      instanceRadar.candInstanceId = candInstanceId;
      return this;
    }

    public Builder withHost(String host) {
      instanceRadar.host = host;
      return this;
    }

    public Builder withPort(int port) {
      instanceRadar.port = port;
      return this;
    }

    public Builder withCandAppId(String appId) {
      instanceRadar.candAppId = appId;
      return this;
    }

    public Builder withAppName(String appName) {
      instanceRadar.appName = appName;
      return this;
    }

    public Builder withClusterName(String clusterName) {
      instanceRadar.clusterName = clusterName;
      return this;
    }
    //注册的时候不用设置此值
    public Builder withUp(boolean up) {
      instanceRadar.up = up;
      return this;
    }
    //注册的时候不用设置此值
    public Builder withWeight(int weight) {
      instanceRadar.weight = weight;
      return this;
    }

    public Builder withTags(Map<String, String> tags) {
      if (tags == null) {
        tags = new HashMap<>();
      }
      instanceRadar.tags = Collections.unmodifiableMap(tags);
      return this;
    }

    public RadarInstance build() {
      return instanceRadar;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RadarInstance that = (RadarInstance) o;
    return port == that.port &&
            up == that.up &&
            weight == that.weight &&
            Objects.equals(candInstanceId, that.candInstanceId) &&
            Objects.equals(host, that.host) &&
            Objects.equals(candAppId, that.candAppId) &&
            Objects.equals(appName, that.appName) &&
            Objects.equals(clusterName, that.clusterName) &&
            Objects.equals(tags, that.tags);
  }

  @Override
  public int hashCode() {

    return Objects.hash(candInstanceId, host, port, candAppId, appName, clusterName, up, weight, tags);
  }
}
