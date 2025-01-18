package me.choicore.samples.testcontainers.support;

import jakarta.annotation.Nonnull;
import me.choicore.samples.testcontainers.TestcontainersConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;


public class TestcontainersConfigurationImportSelector implements ImportSelector {
    private static final Logger log = LoggerFactory.getLogger(TestcontainersConfigurationImportSelector.class);

    @Nonnull
    @Override
    public String[] selectImports(@Nonnull AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = Optional
                .ofNullable(importingClassMetadata.getAnnotationAttributes(ContainerTest.class.getName()))
                .orElse(Collections.emptyMap());

        ContainerType[] containers = (ContainerType[]) attributes.get("containers");
        if (containers == null || containers.length == 0) {
            containers = ContainerType.values();
            log.info("No containers specified, using all available containers: {}", Arrays.toString(containers));
            return new String[]{TestcontainersConfiguration.class.getName()};
        }

        log.info("Containers specified, using containers: {}", Arrays.toString(containers));
        return Arrays.stream(containers)
                .map(container -> switch (container) {
                    case MARIADB -> TestcontainersConfiguration.MariaDB.class.getName();
                    case REDIS -> TestcontainersConfiguration.Redis.class.getName();
                })
                .toArray(String[]::new);
    }
}
