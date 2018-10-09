package com.ppdai.infrastructure.radar.biz;

import javax.sql.DataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.alibaba.druid.pool.DruidDataSource;
import com.ppdai.infrastructure.radar.biz.common.plugin.CatMybatisPlugin;

/**
 * Created by zhangyicong on 2017/5/3.
 */
@Configuration
@MapperScan(basePackages = MySqlDatasourceConfiguration.BASE_PACKAGES,
        sqlSessionTemplateRef = "mysqlSessionTemplate")
public class MySqlDatasourceConfiguration {	
	
    /** 接口类文件所在包 */
    public static final String BASE_PACKAGES = "com.ppdai.infrastructure.radar.biz.dal";
    /** XML 文件所在目录 */
    public static final String MAPPER_XML_PATH = "classpath:mapper/mysql/*.xml";

    @Bean(name = "mysqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("mysqlDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setPlugins(new Interceptor[] {new CatMybatisPlugin()});
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(MAPPER_XML_PATH));
        bean.getObject().getConfiguration().setMapUnderscoreToCamelCase(true);
        return bean.getObject();
    }

    @Bean(name = "mysqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource mysqlDataSource() {
    	DruidDataSource druidDataSource= new DruidDataSource();
    	return druidDataSource;
    }

    @Bean(name = "mysqlTransactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier("mysqlDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "mysqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(
            @Qualifier("mysqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
