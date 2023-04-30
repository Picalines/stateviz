package com.statelang.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class InstanceType<T> {

    @Getter
    private final String name;

    @Getter
    private final Class<T> instanceClass;

    private final Consumer<LibraryBuilder> buildLibrary;

    private boolean builtLibrary = false;

    private record BinaryOperatorKey(BinaryOperator operator, InstanceType<?> rightType) {}

    private final Map<UnaryOperator, InstanceUnaryOperator<T, ?>> unaryOperators = new HashMap<>();

    private final Map<BinaryOperatorKey, InstanceBinaryOperator<T, ?, ?>> binaryOperators = new HashMap<>();

    final void buildLibrary() {
        if (builtLibrary) {
            return;
        }

        buildLibrary.accept(new LibraryBuilder());

        builtLibrary = true;
    }

    public final Optional<InstanceUnaryOperator<T, ?>> getOperator(UnaryOperator operator) {
        return Optional.ofNullable(unaryOperators.get(operator));
    }

    public final <U> Optional<InstanceBinaryOperator<T, U, ?>> getOperator(BinaryOperator operator, InstanceType<U> rightType) {
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

        public <R> void operator(UnaryOperator operator, InstanceType<R> returnType, Function<T, R> apply) {
            Preconditions.checkState(
                !InstanceType.this.unaryOperators.containsKey(operator), "unary operator is already defined"
            );

            InstanceType.this.unaryOperators.put(
                operator,
                InstanceUnaryOperator.<T, R>builder()
                    .operator(operator)
                    .righType(InstanceType.this)
                    .returnType(returnType)
                    .apply(apply)
                    .build()
            );
        }

        public <U, R> void operator(
            BinaryOperator operator,
            InstanceType<U> rightType,
            InstanceType<R> returnType,
            BiFunction<T, U, R> apply)
        {
            var key = new BinaryOperatorKey(operator, rightType);

            Preconditions.checkState(
                !InstanceType.this.binaryOperators.containsKey(key), "binary operator is already defined"
            );

            InstanceType.this.binaryOperators.put(
                key,
                InstanceBinaryOperator.<T, U, R>builder()
                    .operator(operator)
                    .leftType(InstanceType.this)
                    .rightType(rightType)
                    .returnType(returnType)
                    .apply(apply)
                    .build()
            );
        }
    }
}
