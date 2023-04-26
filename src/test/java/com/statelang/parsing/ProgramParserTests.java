package com.statelang.parsing;

import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithoutErrors;

import org.junit.jupiter.api.Test;

class ProgramParserTests {

    @Test
    void counter() {
    assertParsesWithoutErrors("""
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
            """, ProgramParser.program);
    }
}
