package com.statelang.compilation.result;

import java.util.HashMap;
import java.util.Map;

import com.statelang.model.BinaryOperator;

import lombok.Getter;

public final class BinaryOperatorInstruction extends Instruction {

    private final static Map<BinaryOperator, BinaryOperatorInstruction> instances = new HashMap<>();

    @Getter
    private final BinaryOperator operator;

    public static BinaryOperatorInstruction of(BinaryOperator operator) {
        return instances.computeIfAbsent(operator, BinaryOperatorInstruction::new);
    }

    private BinaryOperatorInstruction(BinaryOperator operator) {
        super("bin_op");
        this.operator = operator;
    }
}
