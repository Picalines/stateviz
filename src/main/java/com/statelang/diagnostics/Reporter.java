package com.statelang.diagnostics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Reporter {

	private final List<Report> reports = new ArrayList<>();

	public List<Report> reports() {
		return Collections.unmodifiableList(reports);
	}

	public boolean hasErrors() {
		return reports.stream().anyMatch(report -> report.severity().equals(Report.Severity.ERROR));
	}

	public void report(Report report) {
		reports.add(report);
	}

	public void report(Report.ReportBuilder reportBuilder) {
		report(reportBuilder.build());
	}
}
