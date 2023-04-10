package com.statelang.tokenization;

public class Token {
	private final SourceText sourceText;

	private final SourceSelection selection;

	private final int length;

	private final TokenKind kind;

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

	public SourceSelection selection() {
		return selection;
	}

	public TokenKind kind() {
		return kind;
	}

	@Override
	public String toString() {
		return "Token(" + kind + ", " + sourceText.sourceDescriptor() + ", {" + selection + "})";
	}
}
