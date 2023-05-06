package com.statelang.compilation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.statelang.compilation.result.Instruction;
import com.statelang.compilation.result.LabelInstruction;
import com.statelang.model.StateMachine;

import lombok.Builder;
import lombok.Getter;

public final class CompiledProgram {

    @Getter
    private final StateMachine stateMachine;

    @Getter
    private final List<Instruction> instructions;

    @Getter
    private final Map<String, Integer> jumpTable;

    @Builder
    private CompiledProgram(StateMachine stateMachine, List<Instruction> instructions, Map<String, Integer> jumpTable) {
        this.stateMachine = stateMachine;
        this.instructions = Collections.unmodifiableList(instructions);
        this.jumpTable = Collections.unmodifiableMap(jumpTable);
    }

    public static final class CompiledProgramBuilder {

        private CompiledProgramBuilder() {
            instructions = new ArrayList<>();
            jumpTable = new HashMap<>();
        }

        public CompiledProgramBuilder instruction(Instruction instruction) {
            instructions.add(instruction);

            if (instruction instanceof LabelInstruction labelInstruction) {
                var label = labelInstruction.label();
                Preconditions.checkState(!jumpTable.containsKey(label), "duplicate label instruction");
                jumpTable.put(label, instructions.size() - 1);
            }

            return this;
        }

        public boolean hasDefinedLabel(String label) {
            return jumpTable.containsKey(label);
        }

        @SuppressWarnings("unused")
        private CompiledProgramBuilder instructions(List<Instruction> instructions) {
            this.instructions = instructions;
            return this;
        }

        @SuppressWarnings("unused")
        private CompiledProgramBuilder jumpTable(Map<String, Integer> jumpTable) {
            this.jumpTable = jumpTable;
            return this;
        }
    }
}
