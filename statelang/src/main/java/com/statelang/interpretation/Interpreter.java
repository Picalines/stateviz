package com.statelang.interpretation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.statelang.model.StateMachine;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

public final class Interpreter {

    @Getter
    private final StateMachine stateMachine;

    private final Iterator<InterpretationAction> initActions;

    private final Map<String, InterpretationAction> stateActions;

    private final InterpretationContext context;

    @Builder
    public Interpreter(
        StateMachine stateMachine,
        @Singular List<InterpretationAction> initActions,
        Map<String, InterpretationAction> stateActions)
    {
        this.stateMachine = stateMachine;
        this.initActions = initActions.iterator();
        this.stateActions = Collections.unmodifiableMap(stateActions);

        context = new InterpretationContext(stateMachine);
    }

    public boolean tryStep() {
        if (context.stopped()) {
            return false;
        }

        if (initActions.hasNext()) {
            initActions.next().execute(context);
            return true;
        }

        var currentState = stateMachine.state();

        if (!stateActions.containsKey(currentState)) {
            return false;
        }

        stateActions.get(currentState).execute(context);
        return true;
    }

    public static final class InterpreterBuilder {
        public InterpreterBuilder() {
            stateActions = new HashMap<>();
        }

        public InterpreterBuilder stateAction(String state, InterpretationAction action) {
            stateActions.put(state, action);
            return this;
        }

        @SuppressWarnings("unused")
        private InterpreterBuilder stateActions(Map<String, InterpretationAction> stateActions) {
            this.stateActions = stateActions;
            return this;
        }
    }
}
