/** 
 * Licensed to JumpMind Inc under one or more contributor
 * license agreements.  See the NOTICE file distributed
 * with this work for additional information regarding
 * copyright ownership.  JumpMind Inc licenses this file
 * to you under the GNU General Public License, version 3.0 (GPLv3)
 * (the "License"); you may not use this file except in compliance
 * with the License.
 *
 * You should have received a copy of the GNU General Public License,
 * version 3.0 (GPLv3) along with this library; if not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jumpmind.pos.core.flow;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.jumpmind.pos.core.flow.config.FlowConfig;
import org.jumpmind.pos.core.flow.config.StateConfig;
import org.jumpmind.pos.core.screen.Screen;

/**
 * Responsible for housing all true state data for a node. That is, it should be possible
 * to serialize this class and use it to reengage the application at exactly the same point 
 * it was in.
 */
public class ApplicationState {

    private Scope scope = new Scope();
    private LinkedList<StateContext> stateStack = new LinkedList<>();
    private StateContext currentContext;
    private Transition currentTransition;
    private int screenSequenceNumber = 0;
    private Map<String, Map<String, Screen>> lastScreenByAppIdByNodeId = new HashMap<>();
    private Map<String, Map<String, Screen>> lastDialogByAppIdByNodeId = new HashMap<>();
    
    public Scope getScope() {
        return scope;
    }
    public void setScope(Scope scope) {
        this.scope = scope;
    }
    public LinkedList<StateContext> getStateStack() {
        return stateStack;
    }
    public void setStateStack(LinkedList<StateContext> stateStack) {
        this.stateStack = stateStack;
    }
    public StateContext getCurrentContext() {
        return currentContext;
    }
    public void setCurrentContext(StateContext currentContext) {
        this.currentContext = currentContext;
    }
    public Transition getCurrentTransition() {
        return currentTransition;
    }
    public void setCurrentTransition(Transition currentTransition) {
        this.currentTransition = currentTransition;
    }
    public int getScreenSequenceNumber() {
        return screenSequenceNumber;
    }
    public void setScreenSequenceNumber(int screenSequenceNumber) {
        this.screenSequenceNumber = screenSequenceNumber;
    }
    public Map<String, Map<String, Screen>> getLastScreenByAppIdByNodeId() {
        return lastScreenByAppIdByNodeId;
    }
    public void setLastScreenByAppIdByNodeId(Map<String, Map<String, Screen>> lastScreenByAppIdByNodeId) {
        this.lastScreenByAppIdByNodeId = lastScreenByAppIdByNodeId;
    }
    public Map<String, Map<String, Screen>> getLastDialogByAppIdByNodeId() {
        return lastDialogByAppIdByNodeId;
    }
    public void setLastDialogByAppIdByNodeId(Map<String, Map<String, Screen>> lastDialogByAppIdByNodeId) {
        this.lastDialogByAppIdByNodeId = lastDialogByAppIdByNodeId;
    }
    public int incrementAndScreenSequenceNumber() {
        return ++screenSequenceNumber;
    }
    
    public Object getScopeValue(ScopeType scopeType, String name) {
        ScopeValue scopeValue = null;
        switch (scopeType) {
            case Node:
            case Session:
            case Conversation:
                scopeValue = getScope().getScopeValue(scopeType, name);
                break;
            case Flow:
                break;
            default:
                throw new FlowException("Invalid scope " + scopeType);
        }
        
        if (scopeValue != null) {
            return scopeValue.getValue();
        } else {
            return null;
        }
    }
    
    public <T> T getScopeValue(String name) {
        ScopeValue value = getScope().resolve(name);
        if (value != null) {
            return (T) value.getValue();
        } else {
            value = getCurrentContext().resolveScope(name);
            if (value != null) {
                return (T) value.getValue();    
            } else {
                return null;
            }
        }
    }
    
    public StateConfig findStateConfig(FlowConfig flowConfig) {
        StateConfig stateConfig = flowConfig.getStateConfig(getCurrentContext().getState());
        Iterator<StateContext> itr = getStateStack().iterator();
        while (stateConfig == null && itr.hasNext()) {
            StateContext context = itr.next();
            stateConfig = context.getFlowConfig().getStateConfig(getCurrentContext().getState());

        }
        return stateConfig;
    }    
    
    
}