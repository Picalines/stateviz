package com.langtools.tokenization;

public record SourceSelection(SourceLocation start, SourceLocation end) {
    public static final SourceSelection FirstCharacter = new SourceSelection(
            SourceLocation.FIRST_CHARACTER,
            SourceLocation.FIRST_CHARACTER);

    public boolean contains(SourceLocation location) {
        return location.isAfterOrAt(start) && location.isBeforeOrAt(end);
    }

    @Override
    public String toString() {
        return start == end
                ? start.toString()
                : "from " + start + " to " + end;
    }
}
