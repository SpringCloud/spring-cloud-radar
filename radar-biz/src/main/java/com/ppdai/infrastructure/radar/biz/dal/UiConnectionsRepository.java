package com.ppdai.infrastructure.radar.biz.dal;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UiConnectionsRepository {

    public Map maxConnectionsCount();
    public Map connectionsCount();


}