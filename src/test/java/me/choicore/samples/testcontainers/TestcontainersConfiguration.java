package me.choicore.samples.testcontainers;

import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {
    @TestConfiguration(proxyBeanMethods = false)
    public static class MariaDB {
        static final MariaDBContainer<?> MARIA_DB_CONTAINER = new MariaDBContainer<>(DockerImageName.parse("mariadb:latest"));

        @Bean
        @ServiceConnection
        MariaDBContainer<?> mariaDbContainer() {
            return MARIA_DB_CONTAINER;
        }

        @Bean
        DynamicPropertyRegistrar mariaDbDynamicPropertyRegistrar(
                @Nonnull final MariaDBContainer<?> mariaDbContainer
        ) {
            return registry -> {
                // example of adding dynamic properties
                registry.add("spring.datasource.url", mariaDbContainer::getJdbcUrl);
                registry.add("spring.datasource.username", mariaDbContainer::getUsername);
                registry.add("spring.datasource.password", mariaDbContainer::getPassword);
                registry.add("spring.datasource.driver-class-name", mariaDbContainer::getDriverClassName);
                registry.add("spring.datasource.hikari.maximum-pool-size", Runtime.getRuntime()::availableProcessors);
                registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
            };
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class Redis {
        public static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:latest"))
                .withExposedPorts(6379);

        @Bean
        @ServiceConnection(name = "redis")
        GenericContainer<?> redisContainer() {
            return REDIS_CONTAINER;
        }

        @Bean
        DynamicPropertyRegistrar redisDynamicPropertyRegistrar(
                @Nonnull final @Qualifier("redisContainer") GenericContainer<?> redisContainer
        ) {
            return registry -> {
                // example of adding dynamic properties
                registry.add("spring.data.redis.host", redisContainer::getHost);
                registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
            };
        }
    }
}

