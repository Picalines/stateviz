package com.statelang.interpretation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.statelang.model.StateMachine;
import com.statelang.tokenization.SourceLocation;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

public final class Interpreter {

    public static final String EXIT_LABEL = "$exit$";

    public static final String SELECT_BRANCH_LABEL = "$select_branch$";

    @Getter
    private final StateMachine stateMachine;

    private final List<InterpretationAction> stateActions;

    private final Map<String, Integer> labels;

    private final InterpretationContext context;

    private int currentAction = 0;

    private InterpreterExitReason exitReason = null;

    @Builder
    public Interpreter(
        StateMachine stateMachine,
        @Singular List<InterpretationAction> stateActions)
    {
        this.stateMachine = stateMachine;
        this.stateActions = Collections.unmodifiableList(stateActions);

        labels = new HashMap<>();
        int i = 0;
        for (var action : stateActions) {
            var label = action.label();
            if (label != null) {
                labels.put(label, i);
            }
            i++;
        }

        context = new InterpretationContext(stateMachine::state);
    }

    public Optional<InterpreterExitReason> tryStep() {
        if (exitReason != null) {
            return Optional.of(exitReason);
        }

        if (currentAction >= stateActions.size()) {
            return Optional.of(InterpreterExitReason.FINAL_STATE_REACHED);
        }

        var action = stateActions.get(currentAction);

        try {
            action.execute().accept(context);
        } catch (InterpreterTransitionException transition) {
            stateMachine.performTransition(transition.newState());
        } catch (InterpreterJumpException jump) {
            if (labels.containsKey(jump.label())) {
                currentAction = labels.get(jump.label()) - 1;
            }
        } catch (InterpreterExitException exit) {
            exitReason = exit.reason();
            return Optional.of(exitReason);
        } finally {
            currentAction++;
        }

        return Optional.empty();
    }

    public SourceLocation location() {
        return context.location();
    }

    public Map<String, Object> namedValues() {
        return Collections.unmodifiableMap(context.namedValues());
    }

    public static class InterpreterBuilder {

        private int currentLabel = 0;

        public String generateLabel(String prefix) {
            return prefix + (currentLabel++);
        }
    }
}
