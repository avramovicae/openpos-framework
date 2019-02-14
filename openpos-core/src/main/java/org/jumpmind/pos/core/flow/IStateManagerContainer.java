package org.jumpmind.pos.core.flow;

import java.util.Map;

public interface IStateManagerContainer {

    IStateManager create(String appId, String deviceId, Map<String, Object> queryParams);
    
    IStateManager retrieve(String appId, String deviceId);
    
    void remove(String appId, String deviceId);
    
    void removeSessionIdVariables(String sessionId);
    
}
