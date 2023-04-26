package com.statelang.parsing;

import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithoutErrors;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.statelang.ast.*;
import com.statelang.model.BinaryOperator;
import com.statelang.tokenization.SourceSelection;
import com.statelang.tokenization.Token;
import static com.statelang.tokenization.Token.Kind.*;

class ProgramParserTests {

    @Test
    void counter() {
        var program = assertParsesWithoutErrors("""
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

        assertThat(program)
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(SourceSelection.class)
            .isEqualTo(
                new Program(
                    Arrays.asList(
                        new StateDefinition(Arrays.asList("COUNTING", "STOPPED")),
                        new ConstantDefinition(
                            new Token(null, IDENTIFIER, "max"),
                            new NumberLiteralValue(new Token(null, LITERAL_NUMBER, "10"), 10)
                        ),
                        new VariableDefinition(
                            new Token(null, IDENTIFIER, "count"),
                            new NumberLiteralValue(new Token(null, LITERAL_NUMBER, "0"), 0)
                        ),
                        new InStateDefinition(
                            new Token(null, IDENTIFIER, "COUNTING"),
                            new StateActionBlock(
                                Arrays.asList(
                                    new AssignmentAction(
                                        new Token(null, IDENTIFIER, "count"),
                                        new BinaryValueExpressionNode(
                                            BinaryOperator.PLUS,
                                            new VariableValueNode(new Token(null, IDENTIFIER, "count")),
                                            new NumberLiteralValue(new Token(null, LITERAL_NUMBER, "1"), 1)
                                        )
                                    ),
                                    new ConditionalAction(
                                        new BinaryValueExpressionNode(
                                            BinaryOperator.EQUALS,
                                            new VariableValueNode(new Token(null, IDENTIFIER, "count")),
                                            new VariableValueNode(new Token(null, IDENTIFIER, "max"))
                                        ),
                                        new StateActionBlock(
                                            Arrays.asList(
                                                new TransitionAction(new Token(null, IDENTIFIER, "STOPPED"))
                                            )
                                        ),
                                        null
                                    ),
                                    new AssertionAction(
                                        new BinaryValueExpressionNode(
                                            BinaryOperator.LESS_OR_EQUAL,
                                            new VariableValueNode(new Token(null, IDENTIFIER, "count")),
                                            new VariableValueNode(new Token(null, IDENTIFIER, "max"))
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            );
    }
}
