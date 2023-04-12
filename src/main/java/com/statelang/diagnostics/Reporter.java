package com.statelang.diagnostics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.statelang.tokenization.SourceSelection;

public final class Reporter {
    private final List<Report> reports = new ArrayList<>();

    public List<Report> reports() {
        return Collections.unmodifiableList(reports);
    }

    public boolean hasErrors() {
        return reports.stream().anyMatch(report -> report.severity().equals(Report.Severity.ERROR));
    }

    public void reportUnexpectedCharacter(SourceSelection charSelection) {
        report(charSelection, Report.Severity.ERROR, "Unexpected character");
    }

    private void report(SourceSelection selection, Report.Severity severity, String message) {
        reports.add(new Report(selection, severity, message));
    }
}
