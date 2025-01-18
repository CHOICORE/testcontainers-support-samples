package me.choicore.samples.testcontainers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {
    @TestConfiguration(proxyBeanMethods = false)
    public static class MariaDB {
        @Bean
        @ServiceConnection
        MariaDBContainer<?> mariaDbContainer() {
            return new MariaDBContainer<>(DockerImageName.parse("mariadb:latest"));
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class Redis {
        @Bean
        @ServiceConnection(name = "redis")
        GenericContainer<?> redisContainer() {
            return new GenericContainer<>(DockerImageName.parse("redis:latest")).withExposedPorts(6379);
        }
    }
}
