SET MODE MYSQL;

DROP TABLE IF EXISTS app;
DROP TABLE IF EXISTS app_client;
DROP TABLE IF EXISTS app_cluster;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS dic;
DROP TABLE IF EXISTS instance;
DROP TABLE IF EXISTS soa_lock;
DROP TABLE IF EXISTS task;


CREATE TABLE `app` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cand_app_id` varchar(200) DEFAULT NULL COMMENT '表示有运维统一分配的app_id\n',
  `app_name` varchar(200) DEFAULT NULL,
  `department_id` varchar(200) DEFAULT NULL,
  `department_name` varchar(200) DEFAULT NULL,
  `owner_name` varchar(2000) DEFAULT NULL,
  `owner_id` varchar(2000) DEFAULT NULL COMMENT '逗号隔开',
  `owner_email` varchar(2000) DEFAULT NULL,
  `member_id` varchar(2000) DEFAULT NULL,
  `member_name` varchar(2000) DEFAULT NULL,
  `member_email` varchar(2000) DEFAULT NULL,
  `allow_cross` int(11) DEFAULT '0' COMMENT '是否允许跨集群',
  `domain` varchar(200) DEFAULT NULL COMMENT '域名',
  `version` bigint(20) DEFAULT '0',
  `insert_by` varchar(100) DEFAULT NULL COMMENT '操作人',
  `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(100) DEFAULT NULL COMMENT '操作人',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
  `is_active` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `app_id_UNIQUE` (`cand_app_id`),
  UNIQUE KEY `app_name_UNIQUE` (`app_name`),
  KEY `app_id_idx` (`cand_app_id`)
) ENGINE=InnoDB AUTO_INCREMENT=303 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `app_client` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `provider_cand_app_id` varchar(200) DEFAULT NULL COMMENT '服务提供端的app1_id',
  `consumer_cand_app_id` varchar(200) DEFAULT NULL COMMENT '当前消费端的appid',
  `consumer_cluster_name` varchar(200) NOT NULL COMMENT '子环境名称',
  `insert_by` varchar(100) DEFAULT NULL COMMENT '操作人',
  `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(100) DEFAULT NULL COMMENT '操作人',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
  `is_active` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq` (`provider_cand_app_id`,`consumer_cand_app_id`,`consumer_cluster_name`),
  KEY `consumer_cand_appid_idx` (`consumer_cand_app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `app_cluster` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) DEFAULT NULL,
  `cand_app_id` varchar(200) DEFAULT NULL,
  `app_name` varchar(200) DEFAULT NULL,
  `cluster_name` varchar(200) DEFAULT NULL,
  `black_list` varchar(2000) DEFAULT NULL,
  `white_list` varchar(1000) DEFAULT NULL,
  `limit_qps` int(11) DEFAULT NULL COMMENT '0 表示不开启限流',
  `gateway_visual` varchar(2000) DEFAULT NULL COMMENT '多种类型逗号隔开',
  `enable_self` int(11) DEFAULT NULL COMMENT '是否开启自保护',
  `insert_by` varchar(100) DEFAULT NULL COMMENT '操作人',
  `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(100) DEFAULT NULL COMMENT '操作人',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
  `is_active` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `app_cluster_name_uq` (`cluster_name`,`cand_app_id`),
  KEY `update_idx` (`update_time`),
  KEY `app_name_idx` (`cand_app_id`,`cluster_name`)
) ENGINE=InnoDB AUTO_INCREMENT=303 DEFAULT CHARSET=utf8;

CREATE TABLE `audit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tb_name` varchar(100) NOT NULL COMMENT '名称',
  `ref_id` bigint(20) NOT NULL COMMENT '外键id',
  `content` varchar(2000) NOT NULL COMMENT '内容',
  `insert_by` varchar(100) DEFAULT NULL COMMENT '操作人',
  `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(100) DEFAULT NULL COMMENT '操作人',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
  `is_active` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ;

CREATE TABLE `dic` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `key1` varchar(50) DEFAULT NULL COMMENT '键',
  `value` varchar(1000) NOT NULL COMMENT '值',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `insert_by` varchar(100) DEFAULT NULL COMMENT '操作人',
  `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(100) DEFAULT NULL COMMENT '操作人',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
  `is_active` tinyint(4) NOT NULL DEFAULT '0' ,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_index` (`key1`),
  KEY `key_idx` (`key1`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `instance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) DEFAULT NULL,
  `app_name` varchar(200) DEFAULT NULL,
  `cand_app_id` varchar(200) DEFAULT NULL,
  `app_cluster_id` bigint(20) DEFAULT NULL,
  `app_cluster_name` varchar(200) DEFAULT NULL,
  `cand_instance_id` varchar(200) DEFAULT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `port` int(11) DEFAULT NULL,
  `lan` varchar(45) DEFAULT NULL,
  `sdk_version` varchar(45) DEFAULT NULL,
  `pub_status` int(11) DEFAULT NULL COMMENT '发布槽位0,1',
  `instance_status` int(11) DEFAULT NULL COMMENT '应用槽位0,1',
  `supper_status` int(11) DEFAULT NULL COMMENT '超级槽位-1,0,1,-1 表示强制关闭，0表示忽略，1表示强制开启',
  `extend_status1` int(11) DEFAULT NULL COMMENT '预留扩展槽位',
  `extend_status2` int(11) DEFAULT NULL COMMENT '预留扩展槽位',
  `heart_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `heart_status` tinyint(4) DEFAULT '0' COMMENT '因为心跳字段是一个很特殊的字段，后面需要做异常检查的，所以需要单独拿出来。',
  `weight` int(11) DEFAULT NULL COMMENT '权重',
  `serv_name` text COMMENT '实例服务名列表',
  `tag` text COMMENT '程序自动上传的tag，存的key value的json',
  `tag1` text COMMENT '用户界面操作的tag，为了区别程序自动上报的tag，用两个字段表示同时存的key value的json',
  `insert_by` varchar(100) DEFAULT NULL COMMENT '操作人',
  `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(100) DEFAULT NULL COMMENT '操作人',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
  `is_active` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `cand_instance_id_uq` (`cand_instance_id`),
  KEY `cand_instance_id_index` (`cand_instance_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3016 DEFAULT CHARSET=utf8;

CREATE TABLE `soa_lock` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(45) DEFAULT NULL,
  `key1` varchar(45) DEFAULT NULL COMMENT '需要加锁的key',
  `heart_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `insert_by` varchar(100) DEFAULT NULL COMMENT '操作人',
  `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(100) DEFAULT NULL COMMENT '操作人',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
  `is_active` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_UNIQUE` (`key1`)
) ENGINE=InnoDB AUTO_INCREMENT=223 DEFAULT CHARSET=utf8;

CREATE TABLE `task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) DEFAULT NULL,
  `msg` text,
  `insert_by` varchar(100) DEFAULT NULL COMMENT '操作人',
  `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(100) DEFAULT NULL COMMENT '操作人',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
  `is_active` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_appid` (`app_id`)
) ENGINE=InnoDB AUTO_INCREMENT=445866 DEFAULT CHARSET=utf8;
