package com.statelang.parsing.lib;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import com.statelang.diagnostics.Reporter;
import com.statelang.tokenization.SourceText;

public final class ParsingTestUtils {
    private ParsingTestUtils() {
    }

    public static <T> T assertParsesWithoutErrors(SourceText sourceText, Reporter reporter, Parser<T> parser) {
        var result = parser.tryParse(sourceText, reporter);

        assertTrue(result.isPresent());
        assertFalse(reporter.hasErrors());

        return result.get();
    }

    public static <T> T assertParsesWithoutErrors(String text, Parser<T> parser) {
        return assertParsesWithoutErrors(SourceText.fromString("test", text), new Reporter(), parser);
    }

    public static <T> Optional<T> assertParsesWithErrors(SourceText sourceText, Reporter reporter, Parser<T> parser) {
        var result = parser.tryParse(sourceText, reporter);

        assertTrue(reporter.hasErrors());

        return result;
    }

    public static <T> Optional<T> assertParsesWithErrors(String text, Parser<T> parser) {
        return assertParsesWithErrors(SourceText.fromString("test", text), new Reporter(), parser);
    }
}
