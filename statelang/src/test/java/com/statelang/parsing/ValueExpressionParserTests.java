package com.statelang.parsing;

import com.statelang.ast.BinaryValueExpressionNode;
import com.statelang.ast.BooleanLiteralValue;
import com.statelang.ast.InvalidValueNode;
import com.statelang.ast.NumberLiteralValue;
import com.statelang.ast.StringLiteralValue;
import com.statelang.ast.UnaryValueExpressionNode;
import com.statelang.model.BinaryOperator;
import com.statelang.model.UnaryOperator;
import com.statelang.tokenization.SourceSelection;
import com.statelang.tokenization.Token;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithoutErrors;
import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithErrors;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

class ValueExpressionParserTests {

    @Nested
    class Literals {

        @Test
        void numberLiteral() {
            assertThat(assertParsesWithoutErrors("1.25", ValueExpressionParser.lambda))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Token.class)
                .isEqualTo(
                    new NumberLiteralValue(null, 1.25)
                );
        }

        @Test
        void booleanLiteral() {
            assertThat(assertParsesWithoutErrors("true", ValueExpressionParser.lambda))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Token.class)
                .isEqualTo(
                    new BooleanLiteralValue(null, true)
                );

            assertThat(assertParsesWithoutErrors("false", ValueExpressionParser.lambda))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Token.class)
                .isEqualTo(
                    new BooleanLiteralValue(null, false)
                );
        }

        @Test
        void stringLiteral() {
            assertThat(assertParsesWithoutErrors("'success'", ValueExpressionParser.lambda))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Token.class)
                .isEqualTo(
                    new StringLiteralValue(null, "success")
                );
        }
    }

    @Test
    void sum() {
        assertThat(assertParsesWithoutErrors("1 + 2 + 3", ValueExpressionParser.lambda))
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(Token.class)
            .isEqualTo(
                new BinaryValueExpressionNode(
                    BinaryOperator.PLUS,
                    new BinaryValueExpressionNode(
                        BinaryOperator.PLUS,
                        new NumberLiteralValue(null, 1),
                        new NumberLiteralValue(null, 2)
                    ),
                    new NumberLiteralValue(null, 3)
                )
            );

        assertThat(assertParsesWithoutErrors("1 - 'str' + 3", ValueExpressionParser.lambda))
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(Token.class)
            .isEqualTo(
                new BinaryValueExpressionNode(
                    BinaryOperator.PLUS,
                    new BinaryValueExpressionNode(
                        BinaryOperator.MINUS,
                        new NumberLiteralValue(null, 1),
                        new StringLiteralValue(null, "str")
                    ),
                    new NumberLiteralValue(null, 3)
                )
            );
    }

    @Test
    void relation() {
        assertThat(assertParsesWithoutErrors("10 + 6 > 5", ValueExpressionParser.lambda))
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(Token.class)
            .isEqualTo(
                new BinaryValueExpressionNode(
                    BinaryOperator.GREATER,
                    new BinaryValueExpressionNode(
                        BinaryOperator.PLUS,
                        new NumberLiteralValue(null, 10),
                        new NumberLiteralValue(null, 6)
                    ),
                    new NumberLiteralValue(null, 5)
                )
            );
    }

    @Test
    void invalid() {
        assertThat(assertParsesWithErrors("() * 2", ValueExpressionParser.lambda))
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(Token.class, SourceSelection.class)
            .isEqualTo(
                Optional.of(
                    new BinaryValueExpressionNode(
                        BinaryOperator.MULTIPLY,
                        new InvalidValueNode(null),
                        new NumberLiteralValue(null, 2)
                    )
                )
            );

        assertThat(assertParsesWithErrors("2 + (1 -)", ValueExpressionParser.lambda))
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(Token.class, SourceSelection.class)
            .isEqualTo(
                Optional.of(
                    new BinaryValueExpressionNode(
                        BinaryOperator.PLUS,
                        new NumberLiteralValue(null, 2),
                        new BinaryValueExpressionNode(
                            BinaryOperator.MINUS,
                            new NumberLiteralValue(null, 1),
                            new InvalidValueNode(null)
                        )
                    )
                )
            );
    }

    @Test
    void sumWithUnary() {
        assertThat(assertParsesWithoutErrors("2 + -1", ValueExpressionParser.lambda))
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(Token.class)
            .isEqualTo(
                new BinaryValueExpressionNode(
                    BinaryOperator.PLUS,
                    new NumberLiteralValue(null, 2),
                    new UnaryValueExpressionNode(
                        UnaryOperator.MINUS,
                        new NumberLiteralValue(null, 1),
                        null
                    )
                )
            );
    }

    @Test
    void priority() {
        assertThat(assertParsesWithoutErrors("1 * 2 + 3", ValueExpressionParser.lambda))
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(Token.class)
            .isEqualTo(
                new BinaryValueExpressionNode(
                    BinaryOperator.PLUS,
                    new BinaryValueExpressionNode(
                        BinaryOperator.MULTIPLY,
                        new NumberLiteralValue(null, 1),
                        new NumberLiteralValue(null, 2)
                    ),
                    new NumberLiteralValue(null, 3)
                )
            );

        assertThat(assertParsesWithoutErrors("1 + 2 * 3", ValueExpressionParser.lambda))
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(Token.class)
            .isEqualTo(
                new BinaryValueExpressionNode(
                    BinaryOperator.PLUS,
                    new NumberLiteralValue(null, 1),
                    new BinaryValueExpressionNode(
                        BinaryOperator.MULTIPLY,
                        new NumberLiteralValue(null, 2),
                        new NumberLiteralValue(null, 3)
                    )
                )
            );
    }
}
