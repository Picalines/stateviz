package com.statelang.tokenization;

import java.util.regex.Pattern;

public enum TokenKind {
	WHITESPACE("whitespace", "\\s+"), COMMENT("comment", "#.*\\n?"),

	SEMICOLON("';'", ";"), COLON("':'", ":"), COMMA("','", ","), DOT("'.'", "\\."), AT("'@'", "@"),

	OPEN_PARENTHESIS("'('", "\\("), CLOSE_PARENTHESIS("')'", "\\)"), OPEN_CURLY_BRACE("'{'",
			"\\{"), CLOSE_CURLY_BRACE("'}'", "\\}"),

	KEYWORD_STATE("'state'", "\\bstate\\b"), KEYWORD_ASSERT("'assert'", "\\bassert\\b"), KEYWORD_LET("'let'",
			"\\blet\\b"), KEYWORD_CONST("'const'", "\\bconst\\b"), KEYWORD_IF("'if'",
					"\\bif\\b"), KEYWORD_ELSE("'else'", "\\belse\\b"), KEYWORD_GOTO("'goto'", "\\bgoto\\b"),

	OPERATOR_ASSIGN("':='", ":="), OPERATOR_NOT_EQUALS("'!='", "!="), OPERATOR_EQUALS("'='",
			"="), OPERATOR_LESS_OR_EQUAL("'<='", "<="), OPERATOR_LESS("'<'", "<"), OPERATOR_GREATER_OR_EQUAL("'>='",
					">="), OPERATOR_GREATER("'>'", ">"), OPERATOR_PLUS("'+'", "\\+"), OPERATOR_MINUS("'-'",
							"\\-"), OPERATOR_MULTIPLY("'*'", "\\*"), OPERATOR_DIVIDE("'/'", "\\/"), OPERATOR_MODULO(
									"'%'", "\\%"), OPERATOR_AND("'and'", "\\band\\b"), OPERATOR_OR("'or'",
											"\\bor\\b"), OPERATOR_NOT("'not'", "\\bnot\\b"),

	LITERAL_NUMBER("number literal", "\\d+(\\.\\d+)|\\.\\d+"), LITERAL_BOOLEAN("boolean literal",
			"\\b(true|false)\\b"), LITERAL_STRING("string literal", "'\\.*?'"),

	IDENTIFIER("identifier", "\\b[a-zA-Z][a-zA-Z0-9]*\\b");

	private final String description;

	private final Pattern regex;

	private TokenKind(String description, String regex) {
		this.description = description;
		this.regex = Pattern.compile(regex);
	}

	public int priority() {
		return ordinal();
	}

	public String description() {
		return description;
	}

	public Pattern regex() {
		return regex;
	}
}
