package com.statelang.tokenization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SourceSelectionTests {

    private SourceSelection createSelection(int startLine, int startColumn, int endLine, int endColumn) {
        return new SourceSelection(new SourceLocation(startLine, startColumn), new SourceLocation(endLine, endColumn));
    }

    @Test
    void constructorArguments() {
        assertThrows(IllegalArgumentException.class, () -> createSelection(1, 5, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> createSelection(5, 1, 1, 1));

        assertDoesNotThrow(() -> createSelection(5, 5, 5, 5));
    }

    @Test
    void contains() {
        var selection = createSelection(5, 5, 5, 10);

        assertFalse(selection.contains(new SourceLocation(1, 1)));
        assertFalse(selection.contains(new SourceLocation(10, 1)));

        assertFalse(selection.contains(new SourceLocation(5, 1)));
        assertFalse(selection.contains(new SourceLocation(5, 20)));

        assertTrue(selection.contains(new SourceLocation(5, 5)));
        assertTrue(selection.contains(new SourceLocation(5, 10)));
        assertTrue(selection.contains(new SourceLocation(5, 7)));
    }

}
