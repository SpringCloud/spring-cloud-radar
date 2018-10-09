package com.ppdai.infrastructure.radar.biz.dto.ui;

import java.util.Date;

public class CombinedInstanceDto {
    private Long id;

    private Long appId;

    private String appName;

    private String candAppId;

    private Long appClusterId;

    private String appClusterName;

    private String candInstanceId;

    private String ip;

    private Integer port;

    private String lan;

    private String sdkVersion;

    private Integer pubStatus;

    private Integer instanceStatus;

    private Integer supperStatus;

    private Integer extendStatus1;

    private Integer extendStatus2;

    private Date heartTime;

    private Byte heartStatus;

    private Integer weight;

    private String insertBy;

    private Date insertTime;

    private String updateBy;

    private Date updateTime;

    private Byte isActive;

    private String ownerName;

    private String ownerId;

    private String servName;

    private int role;//用户角色,0代表管理员,1代表owner,2代表非管理员

    private String member;//组员

    private String memberId;

    private int finalStatus;//最终状态

    private String departmentName;

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public int getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(int finalStatus) {
        this.finalStatus = finalStatus;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getServName() {
        return servName;
    }

    public void setServName(String servName) {
        this.servName = servName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName == null ? null : appName.trim();
    }

    public String getCandAppId() {
        return candAppId;
    }

    public void setCandAppId(String candAppId) {
        this.candAppId = candAppId == null ? null : candAppId.trim();
    }

    public Long getAppClusterId() {
        return appClusterId;
    }

    public void setAppClusterId(Long appClusterId) {
        this.appClusterId = appClusterId;
    }

    public String getAppClusterName() {
        return appClusterName;
    }

    public void setAppClusterName(String appClusterName) {
        this.appClusterName = appClusterName == null ? null : appClusterName.trim();
    }

    public String getCandInstanceId() {
        return candInstanceId;
    }

    public void setCandInstanceId(String candInstanceId) {
        this.candInstanceId = candInstanceId == null ? null : candInstanceId.trim();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip == null ? null : ip.trim();
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getLan() {
        return lan;
    }

    public void setLan(String lan) {
        this.lan = lan == null ? null : lan.trim();
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion == null ? null : sdkVersion.trim();
    }

    public Integer getPubStatus() {
        return pubStatus;
    }

    public void setPubStatus(Integer pubStatus) {
        this.pubStatus = pubStatus;
    }

    public Integer getInstanceStatus() {
        return instanceStatus;
    }

    public void setInstanceStatus(Integer instanceStatus) {
        this.instanceStatus = instanceStatus;
    }

    public Integer getSupperStatus() {
        return supperStatus;
    }

    public void setSupperStatus(Integer supperStatus) {
        this.supperStatus = supperStatus;
    }

    public Integer getExtendStatus1() {
        return extendStatus1;
    }

    public void setExtendStatus1(Integer extendStatus1) {
        this.extendStatus1 = extendStatus1;
    }

    public Integer getExtendStatus2() {
        return extendStatus2;
    }

    public void setExtendStatus2(Integer extendStatus2) {
        this.extendStatus2 = extendStatus2;
    }

    public Date getHeartTime() {
        return heartTime;
    }

    public void setHeartTime(Date heartTime) {
        this.heartTime = heartTime;
    }

    public Byte getHeartStatus() {
        return heartStatus;
    }

    public void setHeartStatus(Byte heartStatus) {
        this.heartStatus = heartStatus;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getInsertBy() {
        return insertBy;
    }

    public void setInsertBy(String insertBy) {
        this.insertBy = insertBy == null ? null : insertBy.trim();
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy == null ? null : updateBy.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Byte getIsActive() {
        return isActive;
    }

    public void setIsActive(Byte isActive) {
        this.isActive = isActive;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }



}