package com.statelang.tokenization;

import com.google.common.base.Preconditions;
import com.google.common.collect.Comparators;

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

	public static SourceSelection hull(SourceSelection a, SourceSelection b) {
		var minStart = Comparators.min(a.start, b.start);
		var maxEnd = Comparators.max(a.end, b.end);
		return new SourceSelection(minStart, maxEnd);
	}

	@Override
	public String toString() {
		return start.equals(end) ? start.toString() : "from " + start + " to " + end;
	}
}
