package com.statelang.model;

import java.util.function.BiFunction;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
@Getter
@Builder(access = AccessLevel.PACKAGE)
public final class InstanceBinaryOperator<TLeft, TRight, TReturn> {

    private final BinaryOperator operator;

    private final InstanceType<TLeft> leftType;

    private final InstanceType<TRight> rightType;

    private final InstanceType<TReturn> returnType;

    private final BiFunction<TLeft, TRight, TReturn> apply;
}
