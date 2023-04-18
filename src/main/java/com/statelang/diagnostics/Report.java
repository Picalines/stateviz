package com.statelang.diagnostics;

import com.statelang.tokenization.SourceSelection;

public final class Report {

	public static enum Severity {
		INFO, WARNING, ERROR,
	}

	private final SourceSelection selection;

	private final Severity severity;

	private final String message;

	Report(SourceSelection selection, Severity severity, String message) {
		this.selection = selection;
		this.severity = severity;
		this.message = message;
	}

	public SourceSelection selection() {
		return selection;
	}

	public Severity severity() {
		return severity;
	}

	public String message() {
		return message;
	}

	@Override
	public String toString() {
		return severity + " (" + selection + "): " + message;
	}
}
