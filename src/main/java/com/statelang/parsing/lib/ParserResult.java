package com.statelang.parsing.lib;

import com.google.common.base.Preconditions;
import com.statelang.diagnostics.Report;

public final class ParserResult<T> {
    private final boolean success;

    private final T value;

    private final Report error;

    private ParserResult(T value) {
        success = true;
        this.value = value;
        error = null;
    }

    private ParserResult(Report error) {
        success = false;
        value = null;
        this.error = error;
    }

    public static <T> ParserResult<T> fromValue(T value) {
        return new ParserResult<>(value);
    }

    public static <T> ParserResult<T> fromError(Report errorReport) {
        return new ParserResult<>(errorReport);
    }

    public static <T> ParserResult<T> fromError(Report.ReportBuilder errorReportBuilder) {
        return fromError(errorReportBuilder.build());
    }

    public boolean isSuccess() {
        return success;
    }

    public T value() {
        Preconditions.checkState(success);
        return value;
    }

    public Report error() {
        Preconditions.checkState(!success);
        return error;
    }
}
