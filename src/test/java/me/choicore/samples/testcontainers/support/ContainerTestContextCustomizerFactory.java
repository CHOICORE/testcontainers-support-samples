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

@Deprecated(forRemoval = true)
final class ContainerTestContextCustomizerFactory implements ContextCustomizerFactory {
    private static final Logger log = LoggerFactory.getLogger(ContainerTestContextCustomizerFactory.class);

    @Override
    public ContextCustomizer createContextCustomizer(
            @Nonnull Class<?> testClass,
            @Nonnull List<ContextConfigurationAttributes> configAttributes
    ) {
        ContainerTest annotation = testClass.getAnnotation(ContainerTest.class);
        if (annotation == null) {
            return null;
        }

        log.info("Found @ContainerTest on {} with containers {}",
                testClass.getName(), Arrays.toString(annotation.containers()));

        return new ContainerTestContextCustomizer(annotation);
    }

    static final class ContainerTestContextCustomizer implements ContextCustomizer {
        private static final Logger log = LoggerFactory.getLogger(ContainerTestContextCustomizer.class);

        private final ContainerTest annotation;

        public ContainerTestContextCustomizer(ContainerTest annotation) {
            this.annotation = annotation;
        }

        @Override
        public void customizeContext(
                @Nonnull ConfigurableApplicationContext context,
                @Nonnull MergedContextConfiguration mergedConfig
        ) {
            String testClass = mergedConfig.getTestClass().getSimpleName();
            log.info("Customizing context for test class [{}]", testClass);

            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context;
            AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(registry);

            ContainerType[] containers = annotation.containers();
            if (containers.length == 0) {
                containers = ContainerType.values();
                log.info("No containers specified, using all available containers: {}", Arrays.toString(containers));
            } else {
                log.info("Found specified containers, using containers: {}", Arrays.toString(containers));
                for (ContainerType container : containers) {
                    Class<?> configClass = switch (container) {
                        case MARIADB -> TestcontainersConfiguration.MariaDB.class;
                        case REDIS -> TestcontainersConfiguration.Redis.class;
                    };

                    log.trace("Registering configuration {} for {}",
                            configClass.getSimpleName(), testClass);
                    reader.register(configClass);
                }
            }

            log.info("Completed context customization for test class [{}]", testClass);
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
