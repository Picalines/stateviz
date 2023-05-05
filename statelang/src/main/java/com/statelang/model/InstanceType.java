package com.statelang.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public abstract class InstanceType<T> {

    @AllArgsConstructor
    @Accessors(fluent = true)
    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    public static final class InstanceBinaryOperator<TLeft, TRight, TReturn> {

        private final InstanceType<TLeft> leftType;

        private final InstanceType<TRight> rightType;

        private final InstanceType<TReturn> returnType;
    }

    @AllArgsConstructor
    @Accessors(fluent = true)
    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    public static final class InstanceUnaryOperator<TRight, TReturn> {

        private final InstanceType<TRight> rightType;

        private final InstanceType<TReturn> returnType;
    }

    @Getter
    private final String name;

    @Getter
    private final Class<T> instanceClass;

    private final Consumer<LibraryBuilder> buildLibrary;

    private boolean builtLibrary = false;

    private record BinaryOperatorKey(BinaryOperator operator, InstanceType<?> rightType) {}

    private final Map<UnaryOperator, InstanceUnaryOperator<T, ?>> unaryOperators = new HashMap<>();

    private final Map<BinaryOperatorKey, InstanceBinaryOperator<T, ?, ?>> binaryOperators = new HashMap<>();

    private final void buildLibrary() {
        if (builtLibrary) {
            return;
        }

        buildLibrary.accept(new LibraryBuilder());

        builtLibrary = true;
    }

    public final Optional<InstanceUnaryOperator<T, ?>> getOperator(UnaryOperator operator) {
        buildLibrary();

        return Optional.ofNullable(unaryOperators.get(operator));
    }

    public final <U> Optional<InstanceBinaryOperator<T, U, ?>> getOperator(BinaryOperator operator,
        InstanceType<U> rightType)
    {
        buildLibrary();

        var key = new BinaryOperatorKey(operator, rightType);
        if (!binaryOperators.containsKey(key)) {
            return Optional.empty();
        }

        @SuppressWarnings("unchecked")
        var instanceOperator = (InstanceBinaryOperator<T, U, ?>) binaryOperators.get(key);
        return Optional.of(instanceOperator);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class LibraryBuilder {

        public <R> void operator(UnaryOperator operator, InstanceType<R> returnType) {
            Preconditions.checkState(
                !InstanceType.this.unaryOperators.containsKey(operator), "unary operator is already defined"
            );

            InstanceType.this.unaryOperators.put(
                operator,
                InstanceUnaryOperator.<T, R>builder()
                    .rightType(InstanceType.this)
                    .returnType(returnType)
                    .build()
            );
        }

        public <U, R> void operator(
            BinaryOperator operator,
            InstanceType<U> rightType,
            InstanceType<R> returnType)
        {
            var key = new BinaryOperatorKey(operator, rightType);

            Preconditions.checkState(
                !InstanceType.this.binaryOperators.containsKey(key),
                "binary operator is already defined"
            );

            InstanceType.this.binaryOperators.put(
                key,
                InstanceBinaryOperator.<T, U, R>builder()
                    .leftType(InstanceType.this)
                    .rightType(rightType)
                    .returnType(returnType)
                    .build()
            );
        }
    }
}
