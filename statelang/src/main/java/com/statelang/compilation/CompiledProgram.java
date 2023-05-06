package com.statelang.compilation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.statelang.compilation.instruction.Instruction;
import com.statelang.compilation.instruction.LabelInstruction;
import com.statelang.compilation.symbol.Symbol;
import com.statelang.model.StateMachine;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

public final class CompiledProgram {

    @Getter
    private final StateMachine stateMachine;

    @Getter
    private final List<Instruction> instructions;

    @Getter
    private final Map<String, Integer> jumpTable;

    @Getter
    private final Map<String, Symbol> symbols;

    @Builder(access = AccessLevel.PACKAGE)
    private CompiledProgram(
        StateMachine stateMachine,
        List<Instruction> instructions,
        Map<String, Integer> jumpTable,
        Map<String, Symbol> symbols)
    {
        this.stateMachine = stateMachine;
        this.instructions = Collections.unmodifiableList(instructions);
        this.jumpTable = Collections.unmodifiableMap(jumpTable);
        this.symbols = Collections.unmodifiableMap(symbols);
    }

    public static final class CompiledProgramBuilder {

        private CompiledProgramBuilder() {
            instructions = new ArrayList<>();
            jumpTable = new HashMap<>();
            symbols = new HashMap<>();
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

        public CompiledProgramBuilder symbol(Symbol symbol) {
            var id = symbol.id();
            Preconditions.checkState(!symbols.containsKey(id), "duplicate symbol");
            symbols.put(id, symbol);
            return this;
        }

        public boolean hasDefinedLabel(String label) {
            return jumpTable.containsKey(label);
        }

        public Map<String, Symbol> definedSymbols() {
            return Collections.unmodifiableMap(symbols);
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

        @SuppressWarnings("unused")
        private CompiledProgramBuilder symbols(Map<String, Symbol> symbols) {
            this.symbols = symbols;
            return this;
        }
    }
}
