package com.ppdai.infrastructure.radar.biz.dal;

import com.ppdai.infrastructure.radar.biz.entity.AuditLogEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface UiAuditLogRepository {

    public Long auditLogCount(Map parameterMap);

    public List<AuditLogEntity> findByInstanceId(Map parameterMap);

    public void insertLog(Map parameterMap);

}