package com.statelang.tokenization;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class Token {

	@Getter
	private final SourceSelection selection;

	@Getter
	private final TokenKind kind;

	private final SourceText sourceText;

	private final int length;

	private final int sourceIndex;

	Token(SourceText sourceText, SourceLocation startLocation, int sourceIndex, int length, TokenKind kind) {
		this.sourceText = sourceText;
		this.sourceIndex = sourceIndex;
		this.length = length;
		this.kind = kind;

		String text = this.text();

		this.selection = new SourceSelection(startLocation,
				startLocation.movedTrough(text.substring(0, text.length() - 1)));
	}

	public String text() {
		return sourceText.text().substring(sourceIndex, sourceIndex + length);
	}

	@Override
	public String toString() {
		final var oneLineValue = text().replace("\n", "\\n");
		final var sourceDesc = sourceText.sourceDescriptor();
		return "Token(" + kind + " '" + oneLineValue + "' in " + sourceDesc + " at {" + selection + "})";
	}
}
