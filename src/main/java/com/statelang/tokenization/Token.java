package com.statelang.tokenization;

import java.util.regex.Pattern;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class Token {

	public static enum Kind {
		WHITESPACE("whitespace", "\\s+", true),
		COMMENT("comment", "#.*\\n?", true),

		SEMICOLON("';'", ";"),
		COLON("':'", ":"),
		COMMA("','", ","),
		DOT("'.'", "\\."),
		AT("'@'", "@"),

		OPEN_PARENTHESIS("'('", "\\("),
		CLOSE_PARENTHESIS("')'", "\\)"),
		OPEN_CURLY_BRACE("'{'", "\\{"),
		CLOSE_CURLY_BRACE("'}'", "\\}"),

		LITERAL_NUMBER("number literal", "\\d+(\\.\\d+)?|\\.\\d+"),
		LITERAL_BOOLEAN("boolean literal", "\\b(true|false)\\b"),
		LITERAL_STRING("string literal", "'.*?'"),

		KEYWORD_STATE("'state'", "\\bstate\\b"),
		KEYWORD_ASSERT("'assert'", "\\bassert\\b"),
		KEYWORD_WHEN("'when'", "\\bwhen\\b"),
		KEYWORD_LET("'let'", "\\blet\\b"),
		KEYWORD_CONST("'const'", "\\bconst\\b"),
		KEYWORD_IF("'if'", "\\bif\\b"),
		KEYWORD_ELSE("'else'", "\\belse\\b"),

		OPERATOR_ASSIGN("':='", ":="),
		OPERATOR_NOT_EQUALS("'!='", "!="),
		OPERATOR_EQUALS("'='", "="),
		OPERATOR_LESS_OR_EQUAL("'<='", "<="),
		OPERATOR_LESS("'<'", "<"),
		OPERATOR_GREATER_OR_EQUAL("'>='", ">="),
		OPERATOR_GREATER("'>'", ">"),
		OPERATOR_PLUS("'+'", "\\+"),
		OPERATOR_MINUS("'-'", "\\-"),
		OPERATOR_MULTIPLY("'*'", "\\*"),
		OPERATOR_DIVIDE("'/'", "\\/"),
		OPERATOR_MODULO("'%'", "\\%"),
		OPERATOR_AND("'and'", "\\band\\b"),
		OPERATOR_OR("'or'", "\\bor\\b"),
		OPERATOR_NOT("'not'", "\\bnot\\b"),

		IDENTIFIER("identifier", "\\b[a-zA-Z_][a-zA-Z0-9_]*\\b");

		@Getter
		private final boolean ignored;

		@Getter
		private final String description;

		@Getter
		private final Pattern regex;

		private Kind(String description, String regex, boolean ignored) {
			this.ignored = ignored;
			this.description = description;
			this.regex = Pattern.compile(regex);
		}

		private Kind(String description, String regex) {
			this(description, regex, false);
		}
	}

	@Getter
	private final SourceSelection selection;

	@Getter
	private final Kind kind;

	@Getter
	private final String text;

	private final SourceText sourceText;

	Token(SourceText sourceText, SourceLocation startLocation, int sourceIndex, int length, Kind kind) {
		this.sourceText = sourceText;
		this.kind = kind;

		text = sourceText.text().substring(sourceIndex, sourceIndex + length);

		this.selection = new SourceSelection(startLocation,
				startLocation.movedTrough(text.substring(0, text.length() - 1)));
	}

	@Override
	public String toString() {
		final var oneLineValue = text().replace("\n", "\\n");
		final var sourceDesc = sourceText.sourceDescriptor();
		return "Token(" + kind + " '" + oneLineValue + "' in " + sourceDesc + " at {" + selection + "})";
	}
}
