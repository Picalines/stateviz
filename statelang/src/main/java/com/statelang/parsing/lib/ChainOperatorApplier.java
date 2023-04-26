package com.statelang.parsing.lib;

@FunctionalInterface
public interface ChainOperatorApplier<TOperator, TTerm> {
    TTerm apply(TOperator operator, TTerm leftTerm, TTerm rightTerm);
}
