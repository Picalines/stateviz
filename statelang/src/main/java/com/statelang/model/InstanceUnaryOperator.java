package com.statelang.model;

import java.util.function.Function;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
@Getter
@Builder(access = AccessLevel.PACKAGE)
public final class InstanceUnaryOperator<TRight, TReturn> {

    private final UnaryOperator operator;

    private final InstanceType<TRight> righType;

    private final InstanceType<TReturn> returnType;

    private final Function<TRight, TReturn> apply;
}
