package com.ppdai.infrastructure.radar.biz.common.plugin;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.common.trace.Tracer;
import com.ppdai.infrastructure.radar.biz.common.trace.spi.Transaction;
import com.ppdai.infrastructure.radar.biz.common.util.SpringUtil;

@Intercepts({ @Signature(args = { MappedStatement.class, Object.class }, method = "update", type = Executor.class),
		@Signature(args = { MappedStatement.class, Object.class, RowBounds.class,
				ResultHandler.class }, method = "query", type = Executor.class) })

public class CatMybatisPlugin implements Interceptor {

	private Logger log = LoggerFactory.getLogger(CatMybatisPlugin.class);
	private String dbUrl;
	private SoaConfig soaConfig;

	private SoaConfig getSoaConfig() {
		if (soaConfig == null) {
			soaConfig = SpringUtil.getBean(SoaConfig.class);
		}
		return soaConfig;
	}

	private boolean isFullLog() {
		getSoaConfig();
		if (soaConfig == null) {
			return false;
		}
		return soaConfig.isFullLog();
	}

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		String jdbcUrl = "Notsupported Url";
		String method = "Notsupported Method";
		String sql = "Notsupported SQL";
		String classMethod = "Notsupported Class Method";

		try {
			MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];

			DataSource ds = mappedStatement.getConfiguration().getEnvironment().getDataSource();

			if (ds instanceof com.mchange.v2.c3p0.ComboPooledDataSource) {
				com.mchange.v2.c3p0.ComboPooledDataSource c3p0Ds = (com.mchange.v2.c3p0.ComboPooledDataSource) ds;
				jdbcUrl = c3p0Ds.getJdbcUrl();
			} else if (ds instanceof org.apache.tomcat.jdbc.pool.DataSource) {
				org.apache.tomcat.jdbc.pool.DataSource tDs = (org.apache.tomcat.jdbc.pool.DataSource) ds;
				jdbcUrl = tDs.getUrl();
			} else if (ds instanceof com.alibaba.druid.pool.DruidDataSource) {
				com.alibaba.druid.pool.DruidDataSource dDs = (com.alibaba.druid.pool.DruidDataSource) ds;
				jdbcUrl = dDs.getUrl();
			} else {
				jdbcUrl = dbUrl;
			}

			// 得到 类名-方法
			String[] strArr = mappedStatement.getId().split("\\.");
			classMethod = strArr[strArr.length - 2] + "." + strArr[strArr.length - 1];
			// 得到sql语句
			Object parameter = null;
			if (invocation.getArgs().length > 1) {
				parameter = invocation.getArgs()[1];
			}

			BoundSql boundSql = mappedStatement.getBoundSql(parameter);
			Configuration configuration = mappedStatement.getConfiguration();
			sql = showSql(configuration, boundSql);

		} catch (Exception ex) {

		}

		if (isFullLog() && sql.toLowerCase().indexOf("select") == -1 && sql.toLowerCase().indexOf("instance") != -1
				&& sql.toLowerCase().indexOf("update instance set heart_time=now()") == -1) {
			log.info("sql5_is_{}", sql);
		}
		Transaction t = Tracer.newTransaction("SQL", classMethod);
		method = sql.substring(0, sql.indexOf(" "));

		Tracer.logEvent("SQL.Method", method);
		Tracer.logEvent("SQL.Database", jdbcUrl);
		Tracer.logEvent("SQL.Statement", method, Transaction.SUCCESS,
				sql.length() > 1000 ? sql.substring(0, 1000) : sql);

		Object returnObj = null;
		try {
			returnObj = invocation.proceed();
			t.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			if (sql.indexOf("soa_lock") != -1 && sql.indexOf("insert") != -1) {
				t.setStatus(Transaction.SUCCESS);
			} else {
				t.setStatus(e);
				Tracer.logError(e);
				throw e;
			}			
		} finally {
			t.complete();
		}
		return returnObj;
	}

	public String showSql(Configuration configuration, BoundSql boundSql) {
		Object parameterObject = boundSql.getParameterObject();

		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

		String sql = boundSql.getSql().replaceAll("[\\s]+", " ");

		if (parameterMappings.size() > 0 && parameterObject != null) {

			TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();

			if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {

				sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));

			} else {
				MetaObject metaObject = configuration.newMetaObject(parameterObject);

				for (ParameterMapping parameterMapping : parameterMappings) {

					String propertyName = parameterMapping.getProperty();

					if (metaObject.hasGetter(propertyName)) {

						Object obj = metaObject.getValue(propertyName);
						sql = sql.replaceFirst("\\?", getParameterValue(obj));

					} else if (boundSql.hasAdditionalParameter(propertyName)) {

						Object obj = boundSql.getAdditionalParameter(propertyName);
						sql = sql.replaceFirst("\\?", getParameterValue(obj));
					}
				}
			}
		}
		return sql;
	}

	private String getParameterValue(Object obj) {
		String value = null;
		if (obj instanceof String) {
			value = "'" + obj.toString() + "'";
		} else if (obj instanceof Date) {
			DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
			value = "'" + formatter.format(obj) + "'";
		} else {
			if (obj != null) {
				value = obj.toString();
			} else {
				value = "";
			}

		}
		return value;
	}

	@Override
	public Object plugin(Object target) {
		if (target instanceof Executor){
			return Plugin.wrap(target, this);
		}
		return target;
	}

	@Override
	public void setProperties(Properties properties) {
		dbUrl = properties.getProperty("DBUrl");
	}
}
