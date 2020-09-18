package org.jumpmind.pos.persist.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix="openpos.augmenterconfigs")
public class AugmenterConfigs {
    private List<AugmenterConfig> configs;

    public List<String> getGroups() {
        return Optional.ofNullable(configs).orElse(Collections.emptyList())
                .stream()
                .map(AugmenterConfig::getGroup)
                .collect(Collectors.toList());
    }

    public List<AugmenterConfig> getConfigsByGroups(String... groups) {
        return Optional.ofNullable(configs).orElse(Collections.emptyList())
                .stream()
                .filter(group -> Arrays.stream(groups).anyMatch(g -> g.equals(group.getGroup())))
                .collect(Collectors.toList());
    }

    public AugmenterConfig getConfig(String group) {
        return Optional.ofNullable(configs).orElse(Collections.emptyList())
                .stream()
                .filter(config -> Objects.equals(config.getGroup(), group))
                .findFirst()
                .orElse(null);
    }
}
