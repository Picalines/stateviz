package com.statelang.interpretation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.statelang.compilation.ProgramCompiler;
import com.statelang.diagnostics.Reporter;
import com.statelang.tokenization.SourceText;

class InterpreterTests {
    @Test
    void counter() {
        var reporter = new Reporter();

        var compilerResult = ProgramCompiler.compile(reporter, SourceText.fromString("test", """
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

        assertTrue(compilerResult.isPresent());
        var interpreter = compilerResult.get();

        while (true) {
            var reason = interpreter.tryStep().orElse(null);
            if (reason != null) {
                assertEquals(InterpreterExitReason.FINAL_STATE_REACHED, reason);
                assertEquals(Double.valueOf(10), interpreter.namedValues().get("count"));
                break;
            }
        }
    }
}
