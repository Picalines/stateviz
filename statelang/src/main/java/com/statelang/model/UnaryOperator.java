package com.statelang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
public enum UnaryOperator {
    PLUS("+", "+{0}"),
    MINUS("-", "-{0}"),
    NOT("not", "not {0}");

    @Getter
    private String description;

    @Getter
    private String format;

    public String format(String value) {
        return String.format(format, value);
    }
}
