package com.statelang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
public enum BinaryOperator {
    PLUS("+", "{0} + {1}"),
    MINUS("-", "{0} - {1}"),
    MULTIPLY("*", "{0} * {1}"),
    DIVIDE("/", "{0} / {1}"),
    MODULO("%", "{0} % {1}"),
    LESS("<", "{0} < {1}"),
    LESS_OR_EQUAL("<=", "{0} <= {1}"),
    GREATER(">", "{0} > {1}"),
    GREATER_OR_EQUAL(">=", "{0} >= {1}"),
    EQUALS("=", "{0} = {1}"),
    NOT_EQUALS("!=", "{0} != {1}"),
    AND("and", "{0} and {1}"),
    OR("or", "{0} or {1}");

    @Getter
    private final String description;

    @Getter
    private final String format;

    public String format(String leftValue, String rightValue) {
        return String.format(format, leftValue, rightValue);
    }
}
