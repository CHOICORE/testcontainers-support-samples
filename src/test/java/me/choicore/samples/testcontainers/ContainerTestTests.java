package me.choicore.samples.testcontainers;

import me.choicore.samples.testcontainers.support.ContainerTest;
import me.choicore.samples.testcontainers.support.ContainerType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;

import static org.assertj.core.api.Assertions.assertThat;

public class ContainerTestTests {
    @Nested
    @ContainerTest
    class AvailableContainerTests {
        @Autowired
        private ApplicationContext context;

        @Test
        @DisplayName("MariaDB 컨테이너가 컨텍스트에 등록되고 실행 중이어야 한다")
        void mariaDBContainer() {
            MariaDBContainer<?> container = context.getBean("mariaDbContainer", MariaDBContainer.class);
            assertThat(container.isRunning()).isTrue();
        }

        @Test
        @DisplayName("Redis 컨테이너가 컨텍스트에 등록되고 실행 중이어야 한다")
        void redisContainer() {
            GenericContainer<?> container = context.getBean("redisContainer", GenericContainer.class);
            assertThat(container.isRunning()).isTrue();
        }
    }

    @Nested
    @ContainerTest(containers = ContainerType.MARIADB)
    class MariaDBContainerTests {
        @Autowired
        private ApplicationContext context;

        @Test
        @DisplayName("MariaDB 컨테이너는 컨텍스트에 등록되어야 하고 실행 중이어야 한다")
        void established() {
            MariaDBContainer<?> container = context.getBean("mariaDbContainer", MariaDBContainer.class);
            assertThat(container.isRunning()).isTrue();
        }

        @Test
        @DisplayName("Redis 컨테이너는 컨텍스트에 등록되지 않고, 빈을 찾을 수 없어야 한다")
        void notEstablished() {
            Assertions.assertThatThrownBy(() -> context.getBean("redisContainer", GenericContainer.class))
                    .isInstanceOf(org.springframework.beans.factory.NoSuchBeanDefinitionException.class);
        }
    }

    @Nested
    @ContainerTest(containers = ContainerType.REDIS)
    class RedisContainerTests {
        @Autowired
        private ApplicationContext context;

        @Test
        @DisplayName("Redis 컨테이너는 컨텍스트에 등록되어야 하고 실행 중이어야 한다")
        void established() {
            GenericContainer<?> container = context.getBean("redisContainer", GenericContainer.class);
            assertThat(container.isRunning()).isTrue();
        }

        @Test
        @DisplayName("MariaDB 컨테이너는 컨텍스트에 등록되지 않고, 빈을 찾을 수 없어야 한다")
        void notEstablished() {
            Assertions.assertThatThrownBy(() -> context.getBean("mariaDbContainer", MariaDBContainer.class))
                    .isInstanceOf(org.springframework.beans.factory.NoSuchBeanDefinitionException.class);
        }
    }
}
