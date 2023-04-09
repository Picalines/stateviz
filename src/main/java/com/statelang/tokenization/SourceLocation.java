package com.statelang.tokenization;

public record SourceLocation(int line, int column) implements Comparable<SourceLocation> {

	public static final SourceLocation FIRST_CHARACTER = new SourceLocation(1, 1);

	public SourceLocation {
		if (line <= 0) {
			throw new IllegalArgumentException("line");
		}

		if (column <= 0) {
			throw new IllegalArgumentException("column");
		}
	}

	public SourceLocation() {
		this(1, 1);
	}

	@Override
	public String toString() {
		return "Line " + line + ", Column " + column;
	}

	public SourceLocation movedTrough(String textSpan) {
		int linesCount = 0, lastLineLength = 0;

		for (int i = 0; i < textSpan.length(); i++) {
			char ch = textSpan.charAt(i);
			if (ch == '\n') {
				linesCount++;
				lastLineLength = 0;
			}

			lastLineLength++;
		}

		int line = this.line, column = this.column;

		if (linesCount > 0) {
			line += linesCount;
			column = lastLineLength;
		} else {
			column += lastLineLength;
		}

		return new SourceLocation(line, column);
	}

	@Override
	public int compareTo(SourceLocation other) {
		return line == other.line ? column - other.column : line - other.line;
	}

	public boolean isBefore(SourceLocation other) {
		return line == other.line ? column < other.column : line < other.line;
	}

	public boolean isBeforeOrAt(SourceLocation other) {
		return line == other.line ? column <= other.column : line < other.line;
	}

	public boolean isAfter(SourceLocation other) {
		return line == other.line ? column > other.column : line > other.line;
	}

	public boolean isAfterOrAt(SourceLocation other) {
		return line == other.line ? column >= other.column : line > other.line;
	}
}
