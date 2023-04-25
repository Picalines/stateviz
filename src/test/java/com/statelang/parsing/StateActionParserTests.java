package com.statelang.parsing;

import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithErrors;
import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithoutErrors;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.statelang.ast.AssignmentAction;
import com.statelang.ast.NumberLiteralValue;
import com.statelang.ast.StateAction;
import com.statelang.ast.StateActionBlock;
import com.statelang.ast.TransitionAction;
import com.statelang.tokenization.SourceSelection;
import com.statelang.tokenization.Token;

class StateActionParserTests {

    @Test
    void block() {
        assertThat(assertParsesWithoutErrors("{ state := NEW; x := 123; }", StateActionParser.block))
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(SourceSelection.class)
            .isEqualTo(
                new StateActionBlock(
                    Arrays.<StateAction>asList(
                        new TransitionAction(new Token(null, Token.Kind.IDENTIFIER, "NEW")),
                        new AssignmentAction(
                            new Token(null, Token.Kind.IDENTIFIER, "x"),
                            new NumberLiteralValue(new Token(null, Token.Kind.LITERAL_NUMBER, "123"), 123)
                        )
                    )
                )
            );
    }
}
