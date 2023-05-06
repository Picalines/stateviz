package com.statelang.compilation.instruction;

import java.util.HashMap;
import java.util.Map;

import com.statelang.model.BinaryOperator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BinaryOperatorInstruction extends Instruction {

    private final static Map<BinaryOperator, BinaryOperatorInstruction> instances = new HashMap<>();

    @Getter
    private final BinaryOperator operator;

    public static BinaryOperatorInstruction of(BinaryOperator operator) {
        return instances.computeIfAbsent(operator, BinaryOperatorInstruction::new);
    }
}
