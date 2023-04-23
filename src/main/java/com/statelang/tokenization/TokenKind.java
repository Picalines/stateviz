package com.statelang.tokenization;

import java.util.regex.Pattern;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public enum TokenKind {
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

	LITERAL_NUMBER("number literal", "\\d+(\\.\\d+)?|\\.\\d+"),
	LITERAL_BOOLEAN("boolean literal", "\\b(true|false)\\b"),
	LITERAL_STRING("string literal", "'\\.*?'"),

	IDENTIFIER("identifier", "\\b[a-zA-Z][a-zA-Z0-9]*\\b");

	@Getter
	private final boolean ignored;

	@Getter
	private final String description;

	@Getter
	private final Pattern regex;

	private TokenKind(String description, String regex, boolean ignored) {
		this.ignored = ignored;
		this.description = description;
		this.regex = Pattern.compile(regex);
	}

	private TokenKind(String description, String regex) {
		this(description, regex, false);
	}
}
