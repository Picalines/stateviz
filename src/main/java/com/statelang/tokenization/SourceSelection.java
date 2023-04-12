package com.statelang.tokenization;

public record SourceSelection(SourceLocation start, SourceLocation end) {
	public static final SourceSelection FIRST_CHARACTER = new SourceSelection(SourceLocation.FIRST_CHARACTER,
			SourceLocation.FIRST_CHARACTER);

	public SourceSelection {
		if (end.isBefore(start)) {
			throw new IllegalArgumentException("SourceSelection end is before start");
		}
	}

	public boolean contains(SourceLocation location) {
		return location.isAfterOrAt(start) && location.isBeforeOrAt(end);
	}

	@Override
	public String toString() {
		return start.equals(end) ? start.toString() : "from " + start + " to " + end;
	}
}
