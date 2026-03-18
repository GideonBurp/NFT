package cn.gideon.nft.turbo.base.statemachine;

/**
 * @author Gideon
 */
public interface StateMachine<STATE, EVENT> {

    /**
     * 状态机转移
     *
     * @param state
     * @param event
     * @return
     */
    public STATE transition(STATE state, EVENT event);
}

