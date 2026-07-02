package com.walkit.walkit.global.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class KnowledgeDatasourceConfig {

    // DataSource 빈을 직접 선언하면 DataSourceAutoConfiguration이 비활성화되므로
    // 기존 MySQL도 여기서 @Primary로 재선언. @Value로 직접 바인딩해 jdbcUrl 문제 방지.
    @Bean
    @Primary
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.driver-class-name}") String driverClassName) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName(driverClassName);
        return ds;
    }

    @Bean("knowledgeDataSource")
    public DataSource knowledgeDataSource(
            @Value("${knowledge.datasource.url}") String url,
            @Value("${knowledge.datasource.username}") String username,
            @Value("${knowledge.datasource.password}") String password,
            @Value("${knowledge.datasource.driver-class-name}") String driverClassName) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName(driverClassName);
        return ds;
    }

    @Bean("knowledgeJdbcTemplate")
    public JdbcTemplate knowledgeJdbcTemplate(
            @Qualifier("knowledgeDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
