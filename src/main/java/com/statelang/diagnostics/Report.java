package com.statelang.diagnostics;

import java.util.Collections;
import java.util.List;

import com.statelang.tokenization.SourceSelection;
import com.statelang.tokenization.TokenKind;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public final class Report {

	public static enum Severity {
		INFO,
		WARNING,
		ERROR,
	}

	public enum Kind {
		INVALID_TOKEN(Severity.ERROR),
		UNEXPECTED_TOKEN(Severity.ERROR),
		UNEXPECTED_END_OF_INPUT(Severity.ERROR),
		END_OF_INPUT_EXPECTED(Severity.ERROR);

		@Getter
		private final Severity severity;

		private Kind(Severity severity) {
			this.severity = severity;
		}
	}

	@Getter
	private final SourceSelection selection;

	@Getter
	private final Kind kind;

	@Getter
	@Builder.Default
	private final TokenKind unexpectedTokenKind = null;

	@Getter
	@Builder.Default
	private final List<TokenKind> expectedTokenKinds = Collections.emptyList();

	public Severity severity() {
		return kind.severity;
	}

	@Override
	public String toString() {
		return severity() + " (" + selection + ") " + kind.name();
	}
}
