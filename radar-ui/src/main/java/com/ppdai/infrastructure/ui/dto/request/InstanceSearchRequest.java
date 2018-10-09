package com.ppdai.infrastructure.ui.dto.request;

import com.ppdai.infrastructure.ui.dto.BaseUiRequst;

public class InstanceSearchRequest extends BaseUiRequst{
    private String statusSelect;

    private String id;

    private String clusterName;

    private String appId;

    private String appName;

    private String ip;

    private String departmentName;

    private String sdkVersion;

    private String heartStatus;

    public String getHeartStatus() {
        return heartStatus;
    }

    public void setHeartStatus(String heartStatus) {
        this.heartStatus = heartStatus;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getStatusSelect() {
        return statusSelect;
    }

    public void setStatusSelect(String statusSelect) {
        this.statusSelect = statusSelect;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
}
