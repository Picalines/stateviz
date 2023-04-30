package com.statelang.compilation;

import com.statelang.interpretation.InterpretationAction;
import com.statelang.model.InstanceType;
import com.statelang.model.InvalidInstanceType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
final class CompiledExpression {

    private final InstanceType<?> type;

    private final InterpretationAction action;

    public static final CompiledExpression INVALID = new CompiledExpression(
        InvalidInstanceType.INSTANCE, InterpretationAction.EMPTY
    );
}
