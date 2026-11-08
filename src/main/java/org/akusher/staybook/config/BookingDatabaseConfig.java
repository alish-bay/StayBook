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
        basePackages = "org.akusher.staybook.booking.repository",
        entityManagerFactoryRef = "bookingEntityManagerFactory",
        transactionManagerRef = "bookingTransactionManager"
)
public class BookingDatabaseConfig {

    private final Environment env;

    public BookingDatabaseConfig(Environment env) {
        this.env = env;
    }

    @Bean(name = "bookingDataSource")
    public DataSource bookingDataSource() {
        return DataSourceBuilder.create()
                .url(env.getProperty("spring.datasource.booking.url"))
                .username(env.getProperty("spring.datasource.booking.username"))
                .password(env.getProperty("spring.datasource.booking.password"))
                .driverClassName(env.getProperty("spring.datasource.booking.driver-class-name", "org.postgresql.Driver"))
                .build();
    }

    @DependsOn("bookingLiquibase")
    @Bean(name = "bookingEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean bookingEntityManagerFactory(
            @Qualifier("bookingDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("org.akusher.staybook.booking.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto", "validate"));
        properties.put("hibernate.dialect", env.getProperty("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"));
        properties.put("hibernate.show_sql", env.getProperty("spring.jpa.show-sql", "true"));
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean(name = "bookingTransactionManager")
    public PlatformTransactionManager bookingTransactionManager(
            @Qualifier("bookingEntityManagerFactory") EntityManagerFactory bookingEntityManagerFactory) {
        return new JpaTransactionManager(bookingEntityManagerFactory);
    }
}
