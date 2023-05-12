package com.statelang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum BinaryOperator {
    PLUS("+", "%s + %s"),
    MINUS("-", "%s - %s"),
    MULTIPLY("*", "%s * %s"),
    DIVIDE("/", "%s / %s"),
    MODULO("%", "%s % %s"),
    LESS("<", "%s < %s"),
    LESS_OR_EQUAL("<=", "%s <= %s"),
    GREATER(">", "%s > %s"),
    GREATER_OR_EQUAL(">=", "%s >= %s"),
    EQUALS("=", "%s = %s"),
    NOT_EQUALS("!=", "%s != %s"),
    AND("and", "%s and %s"),
    OR("or", "%s or %s");

    @Getter
    private final String description;

    @Getter
    private final String format;

    public String format(String leftValue, String rightValue) {
        return String.format(format, leftValue, rightValue);
    }
}
