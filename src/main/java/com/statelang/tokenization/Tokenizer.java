package com.statelang.tokenization;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.statelang.diagnostics.Reporter;

public final class Tokenizer implements Iterator<Token> {

	private final SourceText sourceText;

	private final Reporter reporter;

	private int index = 0;

	private SourceLocation location = SourceLocation.FIRST_CHARACTER;

	Tokenizer(SourceText sourceText, Reporter reporter) {
		this.sourceText = sourceText;
		this.reporter = reporter;
	}

	@Override
	public boolean hasNext() {
		return index < sourceText.text().length();
	}

	@Override
	public Token next() {
		if (index >= sourceText.text().length()) {
			throw new NoSuchElementException();
		}

		for (final var tokenKind : TokenKind.values()) {
			final var matcher = tokenKind.regex().matcher(sourceText.text());

			if (!matcher.find(index) || matcher.start() != index) {
				continue;
			}

			final var tokenLength = matcher.end() - matcher.start();
			final var token = new Token(sourceText, location, index, tokenLength, tokenKind);

			index += tokenLength;

			location = location.movedTrough(token.text());

			return token;
		}

		reporter.reportUnexpectedCharacter(location.toCharSelection());
		location = location.movedTrough(Character.toString(sourceText.text().charAt(index)));
		index++;

		return null; // TODO bad token handling
	}
}
