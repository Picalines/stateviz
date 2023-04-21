package com.statelang.diagnostics;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.statelang.tokenization.SourceLocation;
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

	public SourceLocation location() {
		return selection.start();
	}

	@Override
	public String toString() {
		return severity() + " (" + selection + ") " + kind.name();
	}

	public static Report determineMostRelevant(Report first, Report second) {
		if (first.severity() != second.severity()) {
			return first.severity().ordinal() > second.severity().ordinal()
					? first
					: second;
		}

		if (first.selection().equals(second.selection())) {
			var firstExpectedTokens = first.expectedTokenKinds.stream();
			var secondExpectedTokens = second.expectedTokenKinds.stream();
			var expectedTokenKinds = Stream.concat(firstExpectedTokens, secondExpectedTokens).toList();

			return Report.builder()
					.selection(first.selection())
					.kind(first.kind())
					.expectedTokenKinds(expectedTokenKinds)
					.build();
		}

		if (first.location().equals(second.location())) {
			return first.selection().end().isBefore(second.selection().end())
					? first
					: second;
		}

		return first.location().isAfter(second.location())
				? first
				: second;
	}
}
