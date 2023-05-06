package com.statelang.compilation.instruction;

import java.util.HashMap;
import java.util.Map;

import com.statelang.model.UnaryOperator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnaryOperatorInstruction extends Instruction {

    private final static Map<UnaryOperator, UnaryOperatorInstruction> instances = new HashMap<>();

    @Getter
    private final UnaryOperator operator;

    public static UnaryOperatorInstruction of(UnaryOperator operator) {
        return instances.computeIfAbsent(operator, UnaryOperatorInstruction::new);
    }
}
