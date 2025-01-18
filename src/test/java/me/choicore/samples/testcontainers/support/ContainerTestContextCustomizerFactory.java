package me.choicore.samples.testcontainers.support;

import jakarta.annotation.Nonnull;
import me.choicore.samples.testcontainers.TestcontainersConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

final class ContainerTestContextCustomizerFactory implements ContextCustomizerFactory {
    private static final Logger log = LoggerFactory.getLogger(ContainerTestContextCustomizerFactory.class);

    @Override
    public ContextCustomizer createContextCustomizer(
            @Nonnull Class<?> testClass,
            @Nonnull List<ContextConfigurationAttributes> configAttributes
    ) {
        ContainerTest annotation = testClass.getAnnotation(ContainerTest.class);
        if (annotation != null) {
            log.debug("Found @ContainerTest annotation on test class: {}", testClass.getSimpleName());
            return new ContainerTestContextCustomizer(annotation);
        }

        return null;
    }

    private static final class ContainerTestContextCustomizer implements ContextCustomizer {
        private static final Logger log = LoggerFactory.getLogger(ContainerTestContextCustomizer.class);

        private final ContainerTest annotation;

        public ContainerTestContextCustomizer(ContainerTest annotation) {
            this.annotation = annotation;
        }

        @Override
        public void customizeContext(
                ConfigurableApplicationContext context,
                MergedContextConfiguration mergedConfig
        ) {
            String testClass = mergedConfig.getTestClass().getSimpleName();
            log.info("[{}] Starting Testcontainers configuration", testClass);

            ContainerType[] containers = annotation.containers();
            if (containers.length == 0) {
                containers = ContainerType.values();
                log.info("[{}] Using all available containers: {}",
                        testClass, Arrays.toString(containers));
            } else {
                log.info("[{}] Using specified containers: {}",
                        testClass, Arrays.toString(containers));
            }

            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context;
            AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(registry);

            for (ContainerType container : containers) {
                Class<?> configClass = switch (container) {
                    case MARIADB -> TestcontainersConfiguration.MariaDB.class;
                    case REDIS -> TestcontainersConfiguration.Redis.class;
                };

                log.debug("[{}] Registering configuration: {}",
                        testClass, configClass.getSimpleName());

                reader.register(configClass);
            }

            log.info("[{}] Testcontainers configuration completed", testClass);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ContainerTestContextCustomizer that)) return false;
            return Objects.equals(annotation, that.annotation);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(annotation);
        }
    }
}
