package org.jumpmind.pos.config.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jumpmind.pos.config.model.ConfigModel;
import org.jumpmind.pos.config.model.ConfigRepository;
import org.jumpmind.pos.service.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@Transactional
public class GetConfigEndpoint {
    
    @Autowired
    private ConfigRepository configRepository;
    
    @Endpoint("/getConfig")
    public ConfigResult getConfig(
            @RequestParam(value="region", defaultValue="*") String region,
            @RequestParam(value="country", defaultValue="*") String country,
            @RequestParam(value="state", defaultValue="*") String state,
            @RequestParam(value="store", defaultValue="*") String store,
            @RequestParam(value="nodeId", defaultValue="*") String nodeId,
            @RequestParam(value="deviceType", defaultValue="*") String deviceType,
            @RequestParam(value="storeType", defaultValue="*") String storeType,
            @RequestParam(value="departmentId", defaultValue="*") String departmentId,
            @RequestParam(value="brandId", defaultValue="*") String brandId,
            @RequestParam(value="configName", defaultValue="") String configName) {    
        
        // TODO get the date from the TimeService.
        
        Map<String, String> tags = new HashMap<>();
        tags.put(ConfigRepository.TAG_REGION, region);
        tags.put(ConfigRepository.TAG_COUNTRY, country);
        tags.put(ConfigRepository.TAG_STATE, state);
        tags.put(ConfigRepository.TAG_STORE, store);
        tags.put(ConfigRepository.TAG_NODE_ID, nodeId);
        tags.put(ConfigRepository.TAG_DEVICE_TYPE, deviceType);
        tags.put(ConfigRepository.TAG_STORE_TYPE, storeType);
        tags.put(ConfigRepository.TAG_DEPARTMENT_ID, departmentId);
        tags.put(ConfigRepository.TAG_BRAND_ID, brandId);
        
        ConfigModel model = configRepository.findConfigValue(new Date(), tags, configName);
        ConfigResult result = new ConfigResult();
        if (model != null) {
            result.setConfigName(configName);
            result.setConfigValue(model.getConfigValue());
            result.setConfig(model);
        } else {
            result.setResultStatus("NOT_FOUND");
            result.setResultMessage("No config found for " + configName);
        }
        
        return result;
        
    }

}
