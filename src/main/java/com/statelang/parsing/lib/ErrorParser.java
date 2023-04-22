package com.statelang.parsing.lib;

import java.util.HashMap;

import com.statelang.diagnostics.Report;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class ErrorParser<T> extends Parser<T> {

    private static final HashMap<Report.Kind, ErrorParser<?>> instances = new HashMap<>();

    private final Report.Kind reportKind;

    public static <T> ErrorParser<T> of(Report.Kind reportKind) {
        @SuppressWarnings("unchecked")
        var parser = (ErrorParser<T>) instances.computeIfAbsent(reportKind, ErrorParser::new);
        return parser;
    }

    @Override
    public ParserResult<T> parse(ParserContext context) {
        var reader = context.reader();

        return ParserResult.fromError(Report.builder()
                .selection(reader.selection())
                .kind(reportKind));
    }
}
