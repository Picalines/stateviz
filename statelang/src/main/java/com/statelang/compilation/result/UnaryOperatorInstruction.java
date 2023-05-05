package com.statelang.compilation.result;

import java.util.HashMap;
import java.util.Map;

import com.statelang.model.UnaryOperator;

import lombok.Getter;

public final class UnaryOperatorInstruction extends Instruction {

    private final static Map<UnaryOperator, UnaryOperatorInstruction> instances = new HashMap<>();

    @Getter
    private final UnaryOperator operator;

    public static UnaryOperatorInstruction of(UnaryOperator operator) {
        return instances.computeIfAbsent(operator, UnaryOperatorInstruction::new);
    }

    private UnaryOperatorInstruction(UnaryOperator operator) {
        super("un_op");
        this.operator = operator;
    }
}
