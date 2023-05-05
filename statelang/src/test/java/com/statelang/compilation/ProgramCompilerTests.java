package com.statelang.compilation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.statelang.diagnostics.Reporter;
import com.statelang.tokenization.SourceText;

class ProgramCompilerTests {

    @Test
    void counter() {
        var reporter = new Reporter();

        var compiledProgram = ProgramCompiler.compile(reporter, SourceText.fromString("test", """
            state {
                COUNTING,
                STOPPED,
            }

            const max := 10;
            let count := 0;

            when COUNTING {
                count := count + 1;

                if count = max {
                    state := STOPPED;
                }

                assert count <= max;
            }
            """));

        assertTrue(compiledProgram.isPresent());
        assertEquals(0, reporter.reports().size());
    }
}
