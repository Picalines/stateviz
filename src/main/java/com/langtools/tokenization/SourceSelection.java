package com.langtools.tokenization;

public record SourceSelection(SourceLocation start, SourceLocation end) {
	public static final SourceSelection FIRST_CHARACTER = new SourceSelection(SourceLocation.FIRST_CHARACTER,
			SourceLocation.FIRST_CHARACTER);

	public boolean contains(SourceLocation location) {
		return location.isAfterOrAt(start) && location.isBeforeOrAt(end);
	}

	@Override
	public String toString() {
		return start.equals(end) ? start.toString() : "from " + start + " to " + end;
	}
}
