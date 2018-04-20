package org.jumpmind.pos.app.state;

import org.jumpmind.pos.core.flow.IState;
import org.jumpmind.pos.core.flow.IStateManager;
import org.jumpmind.pos.core.flow.In;
import org.jumpmind.pos.core.flow.ScopeType;
import org.springframework.beans.factory.annotation.Autowired;

abstract public class AbstractState implements IState {

    @In(scope=ScopeType.Node)
    protected IStateManager stateManager;
    
    protected String getStoreId() {        
        String nodeId = stateManager.getNodeId();
        String[] parts = nodeId.split("-");
        return parts[0];
    }
    
    protected String getWorstationId() {        
        String nodeId = stateManager.getNodeId();
        String[] parts = nodeId.split("-");
        return parts[1];
    }

    
}
