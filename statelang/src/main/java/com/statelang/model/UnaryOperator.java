package com.statelang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
public enum UnaryOperator {
    PLUS("+", "+%s"),
    MINUS("-", "-%s"),
    NOT("not", "not %s");

    @Getter
    private String description;

    @Getter
    private String format;

    public String format(String value) {
        return String.format(format, value);
    }
}
