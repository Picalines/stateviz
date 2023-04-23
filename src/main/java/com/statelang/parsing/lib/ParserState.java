package com.statelang.parsing.lib;

import java.util.Stack;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ParserState<T> {

    private final Stack<T> values = new Stack<>();

    public T value() {
        Preconditions.checkState(!values.empty(), "ParserState.value can be called only during parsing");

        return values.peek();
    }

    void pushValue(T value) {
        values.push(value);
    }

    void popValue() {
        values.pop();
    }
}
