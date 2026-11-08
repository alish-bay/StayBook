package org.akusher.staybook.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * Отдельная конфигурация для миграций Liquibase.
 * Изолирует запуск миграций по соответствующим базам данных,
 * оставляя основные DatabaseConfig чистыми (только DataSource + JPA).
 */
@Configuration
public class LiquibaseConfig {

    private final Environment env;

    public LiquibaseConfig(Environment env) {
        this.env = env;
    }

    @Bean(name = "userLiquibase")
    public SpringLiquibase userLiquibase(@Qualifier("userDataSource") DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-user.xml");
        liquibase.setShouldRun(env.getProperty("staybook.liquibase.enabled", Boolean.class, true));
        return liquibase;
    }

    @Bean(name = "roomLiquibase")
    public SpringLiquibase roomLiquibase(@Qualifier("roomDataSource") DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-room.xml");
        liquibase.setShouldRun(env.getProperty("staybook.liquibase.enabled", Boolean.class, true));
        return liquibase;
    }

    @Bean(name = "bookingLiquibase")
    public SpringLiquibase bookingLiquibase(@Qualifier("bookingDataSource") DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-booking.xml");
        liquibase.setShouldRun(env.getProperty("staybook.liquibase.enabled", Boolean.class, true));
        return liquibase;
    }
}
