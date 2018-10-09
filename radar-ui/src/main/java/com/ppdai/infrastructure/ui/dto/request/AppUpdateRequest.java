package com.ppdai.infrastructure.ui.dto.request;

public class AppUpdateRequest {
    private long id;

    private String candAppId;

    private String appName;

    private String domain;

    private String ownerId;

    private String memberId;

    private int allowCross;

    private String departmentId;

    private String departmentName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCandAppId() {
        return candAppId;
    }

    public void setCandAppId(String candAppId) {
        this.candAppId = candAppId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public int getAllowCross() {
        return allowCross;
    }

    public void setAllowCross(int allowCross) {
        this.allowCross = allowCross;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

}
