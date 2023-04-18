package com.statelang.tokenization;

import com.google.common.base.Preconditions;

public record SourceSelection(SourceLocation start, SourceLocation end) {

	public static final SourceSelection FIRST_CHARACTER = SourceLocation.FIRST_CHARACTER.toCharSelection();

	public SourceSelection {
		Preconditions.checkNotNull(start, "start is null");
		Preconditions.checkNotNull(end, "end is null");
		Preconditions.checkArgument(start.isBeforeOrAt(end), "start is after end");
	}

	public boolean contains(SourceLocation location) {
		return location.isAfterOrAt(start) && location.isBeforeOrAt(end);
	}

	@Override
	public String toString() {
		return start.equals(end) ? start.toString() : "from " + start + " to " + end;
	}
}
