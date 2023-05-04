package com.statelang.interpretation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.statelang.model.StateMachine;
import com.statelang.tokenization.SourceLocation;

import lombok.Builder;
import lombok.Getter;

public final class Interpreter {

    @Getter
    private final StateMachine stateMachine;

    private final List<Instruction> instructions;

    private final Map<String, Integer> labels;

    private final InterpretationContext context;

    private int instructionIndex = 0;

    @Getter
    private SourceLocation location = SourceLocation.FIRST_CHARACTER;

    private InterpreterExitReason exitReason = null;

    @Builder
    private Interpreter(
        StateMachine stateMachine,
        List<Instruction> instructions,
        Map<String, Integer> labels)
    {
        this.stateMachine = stateMachine;
        this.instructions = Collections.unmodifiableList(instructions);
        this.labels = Collections.unmodifiableMap(labels);

        context = new InterpretationContext(stateMachine::state);
    }

    public Optional<InterpreterExitReason> tryStep() {
        if (exitReason != null) {
            return Optional.of(exitReason);
        }

        if (instructionIndex >= instructions.size()) {
            return Optional.of(InterpreterExitReason.FINAL_STATE_REACHED);
        }

        var instruction = instructions.get(instructionIndex);

        instruction.location().ifPresent(instructionLocation -> location = instructionLocation);

        if (instruction instanceof ActionInstruction actionInstruction) {
            try {
                actionInstruction.execute(context);
            } catch (InterpreterTransitionException transition) {
                stateMachine.performTransition(transition.newState());
            } catch (InterpreterJumpException jump) {
                if (labels.containsKey(jump.label())) {
                    instructionIndex = labels.get(jump.label()) - 1;
                }
            } catch (InterpreterExitException exit) {
                exitReason = exit.reason();
            }
        }

        instructionIndex++;

        return Optional.ofNullable(exitReason);
    }

    public Map<String, Object> namedValues() {
        return Collections.unmodifiableMap(context.namedValues());
    }

    public static class InterpreterBuilder {

        private int currentLabel = 0;

        private InterpreterBuilder() {
            instructions = new ArrayList<>();
            labels = new HashMap<>();
        }

        public InterpreterBuilder instruction(ActionInstruction actionInstruction) {
            instructions.add(actionInstruction);
            return this;
        }

        public InterpreterBuilder instruction(SourceLocation location, ActionInstruction actionInstruction) {
            instructions.add(new ActionInstruction() {
                @Override
                public void execute(InterpretationContext context) {
                    actionInstruction.execute(context);
                }

                @Override
                public Optional<SourceLocation> location() {
                    return Optional.of(location);
                }
            });
            return this;
        }

        public InterpreterBuilder jumpLabel(String label) {
            labels.put(label, instructions.size());
            instructions.add(new LabelInstruction(label));
            return this;
        }

        public Boolean hasDefinedLabel(String label) {
            return labels.containsKey(label);
        }

        public String generateLabel(String prefix) {
            return prefix + (currentLabel++);
        }

        @SuppressWarnings("unused")
        private InterpreterBuilder instructions(List<Instruction> instructions) {
            return this;
        }

        @SuppressWarnings("unused")
        private InterpreterBuilder labels(Map<String, Integer> instructions) {
            return this;
        }
    }
}
