package org.akusher.staybook.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "org.akusher.staybook.room.repository",
        entityManagerFactoryRef = "roomEntityManagerFactory",
        transactionManagerRef = "roomTransactionManager"
)
public class RoomDatabaseConfig {

    private final Environment env;

    public RoomDatabaseConfig(Environment env) {
        this.env = env;
    }

    @Bean(name = "roomDataSource")
    public DataSource roomDataSource() {
        return DataSourceBuilder.create()
                .url(env.getProperty("spring.datasource.room.url"))
                .username(env.getProperty("spring.datasource.room.username"))
                .password(env.getProperty("spring.datasource.room.password"))
                .driverClassName(env.getProperty("spring.datasource.room.driver-class-name", "org.postgresql.Driver"))
                .build();
    }

    @DependsOn("roomLiquibase")
    @Bean(name = "roomEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean roomEntityManagerFactory(
            @Qualifier("roomDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("org.akusher.staybook.room.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto", "validate"));
        properties.put("hibernate.dialect", env.getProperty("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"));
        properties.put("hibernate.show_sql", env.getProperty("spring.jpa.show-sql", "true"));
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean(name = "roomTransactionManager")
    public PlatformTransactionManager roomTransactionManager(
            @Qualifier("roomEntityManagerFactory") EntityManagerFactory roomEntityManagerFactory) {
        return new JpaTransactionManager(roomEntityManagerFactory);
    }
}
