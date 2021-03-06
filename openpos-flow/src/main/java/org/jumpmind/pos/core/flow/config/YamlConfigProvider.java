package org.jumpmind.pos.core.flow.config;

import java.io.InputStream;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.core.flow.FlowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

@Slf4j
public class YamlConfigProvider implements IFlowConfigProvider {

    private YamlFlowConfigFileLoader flowConfigLoader = new YamlFlowConfigFileLoader();
    private YamlConfigConverter flowConfigConverter;

    protected Map<String, List<FlowConfig>> loadedFlowConfigs = new LinkedHashMap<String, List<FlowConfig>>();
    protected Map<String, List<YamlFlowConfig>> loadedYamlFlowConfigs = new HashMap<String, List<YamlFlowConfig>>();
    protected Map<String, String> appIdToStartFlowName = new HashMap<>();

    private List<TransitionStepConfig> transitionSteps;

    public YamlConfigProvider() {
        this(null);
    }

    public YamlConfigProvider(List<String> additionalPackages) {
        flowConfigConverter = new YamlConfigConverter(additionalPackages);
    }

    @Override
    public void load(String appId, String path, String startFlowName) {
        appIdToStartFlowName.put(appId, startFlowName);
        load(appId, path);
    }

    @Bean
    public List<String> loadedAppIds() {
        return new ArrayList<>(appIdToStartFlowName.keySet());
    }

    public void load(String appId, String path) {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());

            Resource[] resources = resolver.getResources("classpath*:/" + path + "/*-flow.yml");

            List<YamlFlowConfig> yamlFlowConfigs = new ArrayList<>();

            // Loop these backwards since most specific will be first in the class path an we want those
            // last so they win the override
            for (int i = resources.length - 1; i >= 0; --i) {
                Resource resource = resources[i];
                // first pass needs to load all raw YAML.
                yamlFlowConfigs.addAll(loadYamlResource(appId, path, resource));
            }

            // second pass here needed to convert
            loadYamlFlowConfigs(appId, yamlFlowConfigs);

            transitionSteps = flowConfigConverter.convertTransitionSteps(new YamlTransitionStepProvider().loadTransitionSteps(resolver, appId, path, flowConfigLoader), yamlFlowConfigs);

        } catch (Exception ex) {
            throw new FlowException(String.format("Failed to load YML flow config for appId '%s' and path '%s'", appId, path), ex);
        }
    }

    public void load(String appId, InputStream resource) {
        List<YamlFlowConfig> yamlFlowConfigs = new ArrayList<>();
        yamlFlowConfigs.addAll(loadYamlResource(appId, resource));
        // second pass here needs to then convert
        loadYamlFlowConfigs(appId, yamlFlowConfigs);
    }

    @Override
    public List<TransitionStepConfig> getTransitionStepConfig(String appId, String nodeId) {
        return transitionSteps;
    }

    @Override
    public FlowConfig getConfigByName(String appId, String deviceId, String name) {
        List<FlowConfig> flowConfigs = loadedFlowConfigs.get(appId);
        if (flowConfigs != null && !flowConfigs.isEmpty()) {
            return flowConfigs.stream().filter(flowConfig -> name.equals(flowConfig.getName())).findAny().orElse(null);
        } else {
            return null;
        }
    }

    @Override
    public FlowConfig getConfig(String appId, String deviceId) {
        if (appIdToStartFlowName.containsKey(appId)) {
            return getConfigByName(appId, deviceId, appIdToStartFlowName.get(appId));
        } else {
            List<FlowConfig> flowConfigs = loadedFlowConfigs.get(appId);
            if (flowConfigs != null && !flowConfigs.isEmpty()) {
                return flowConfigs.get(0);
            } else {
                return null;
            }
        }
    }

    protected void updateSubflowsWithParentConfigScope(FlowConfig flowConfig) {
        updateSubflowsWithParentConfigScope(flowConfig, flowConfig.getConfigScope());
    }

    protected void updateSubflowsWithParentConfigScope(FlowConfig flowConfig, Map<String, Object> inheritedConfigScope) {
        flowConfig.getStateConfigs().values().forEach(sc -> {
            updateSubflowsWithParentConfigScope(flowConfig, sc.getActionToSubStateMapping().values(), inheritedConfigScope);
        });
        updateSubflowsWithParentConfigScope(flowConfig, flowConfig.getActionToSubStateMapping().values(), inheritedConfigScope);
    }

    protected void updateSubflowsWithParentConfigScope(FlowConfig flowConfig, Collection<SubFlowConfig> configs, Map<String, Object> inheritedConfigScope) {
        configs.forEach(sf -> {
            Map<String, Object> currentScope = new HashMap<>(inheritedConfigScope);
            currentScope.putAll(sf.getSubFlowConfig().getConfigScope());
            sf.getSubFlowConfig().setConfigScope(currentScope);
            updateSubflowsWithParentConfigScope(sf.getSubFlowConfig(), currentScope);
        });
    }

    protected List<YamlFlowConfig> loadYamlResource(String appId, InputStream resource) {
        List<YamlFlowConfig> yamlFlowConfigs = flowConfigLoader.loadYamlFlowConfigs(resource);
        List<YamlFlowConfig> existingYamlFlowConfigs = loadedYamlFlowConfigs.get(appId);

        if (existingYamlFlowConfigs == null) {
            existingYamlFlowConfigs = new ArrayList<YamlFlowConfig>();
            loadedYamlFlowConfigs.put(appId, existingYamlFlowConfigs);
        }

        for (YamlFlowConfig flowConfig : yamlFlowConfigs) {
            YamlFlowConfig match = existingYamlFlowConfigs.stream().filter(flowConfig1 -> flowConfig.getFlowName().equals(flowConfig1.getFlowName())).findFirst().orElse(null);
            if (match == null) {
                existingYamlFlowConfigs.add(flowConfig);
            } else {
                match.merge(flowConfig);
            }
        }

        return existingYamlFlowConfigs;
    }

    protected List<YamlFlowConfig> loadYamlResource(String appId, String path, Resource resource) {
        log.debug("Loading flow config from {} for {}", resource.toString(), appId);
        try {
            return loadYamlResource(appId, resource.getInputStream());
        } catch (Exception ex) {
            throw new FlowException(String.format("Failed while loading resource %s", resource), ex);
        }
    }

    protected void loadYamlFlowConfigs(String appId, List<YamlFlowConfig> yamlFlowConfigs) {

        List<YamlFlowConfig> existingYamlFlowConfigs = loadedYamlFlowConfigs.get(appId);

        List<FlowConfig> existingFlowConfigs = loadedFlowConfigs.get(appId);
        if (existingFlowConfigs == null) {
            existingFlowConfigs = new ArrayList<FlowConfig>();
            loadedYamlFlowConfigs.put(appId, existingYamlFlowConfigs);
            loadedFlowConfigs.put(appId, existingFlowConfigs);
        }

        List<FlowConfig> flowConfigs = flowConfigConverter.convertFlowConfigs(existingYamlFlowConfigs, yamlFlowConfigs);
        existingFlowConfigs.addAll(flowConfigs);

        // inherit config scope down through child sub flows
        existingFlowConfigs.forEach(f -> updateSubflowsWithParentConfigScope(f));

    }

}
