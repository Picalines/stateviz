package com.statelang.tokenization;

import com.google.common.base.Preconditions;

public record SourceLocation(int line, int column) implements Comparable<SourceLocation> {

	public static final SourceLocation FIRST_CHARACTER = new SourceLocation(1, 1);

	public SourceLocation {
		Preconditions.checkArgument(line > 0, "line out of bounds");
		Preconditions.checkArgument(column > 0, "column out of bounds");
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

	public SourceSelection toCharSelection() {
		return new SourceSelection(this, this);
	}

	@Override
	public int compareTo(SourceLocation other) {
		return line == other.line ? column - other.column : line - other.line;
	}

	public boolean isBefore(SourceLocation other) {
		return compareTo(other) < 0;
	}

	public boolean isBeforeOrAt(SourceLocation other) {
		return compareTo(other) <= 0;
	}

	public boolean isAfter(SourceLocation other) {
		return compareTo(other) > 0;
	}

	public boolean isAfterOrAt(SourceLocation other) {
		return compareTo(other) >= 0;
	}
}
