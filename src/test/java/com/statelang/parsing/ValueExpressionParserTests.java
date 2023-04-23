package com.statelang.parsing;

import com.statelang.ast.BinaryValueExpressionNode;
import com.statelang.ast.BooleanLiteralValue;
import com.statelang.ast.InvalidValueNode;
import com.statelang.ast.NumberLiteralValue;
import com.statelang.ast.StringLiteralValue;
import com.statelang.ast.UnaryValueExpressionNode;
import com.statelang.model.BinaryOperator;
import com.statelang.model.UnaryOperator;
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
                    .isEqualTo(new NumberLiteralValue(null, 1.25));
        }

        @Test
        void booleanLiteral() {
            assertThat(assertParsesWithoutErrors("true", ValueExpressionParser.lambda))
                    .usingRecursiveComparison()
                    .ignoringFieldsOfTypes(Token.class)
                    .isEqualTo(new BooleanLiteralValue(null, true));

            assertThat(assertParsesWithoutErrors("false", ValueExpressionParser.lambda))
                    .usingRecursiveComparison()
                    .ignoringFieldsOfTypes(Token.class)
                    .isEqualTo(new BooleanLiteralValue(null, false));
        }

        @Test
        void stringLiteral() {
            assertThat(assertParsesWithoutErrors("'success'", ValueExpressionParser.lambda))
                    .usingRecursiveComparison()
                    .ignoringFieldsOfTypes(Token.class)
                    .isEqualTo(new StringLiteralValue(null, "success"));
        }
    }

    @Test
    void sum() {
        assertThat(assertParsesWithoutErrors("1 + 2 + 3", ValueExpressionParser.lambda))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Token.class)
                .isEqualTo(new BinaryValueExpressionNode(
                        new BinaryValueExpressionNode(
                                new NumberLiteralValue(null, 1),
                                new NumberLiteralValue(null, 2),
                                BinaryOperator.PLUS),
                        new NumberLiteralValue(null, 3),
                        BinaryOperator.PLUS));

        assertThat(assertParsesWithoutErrors("1 - 'str' + 3", ValueExpressionParser.lambda))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Token.class)
                .isEqualTo(new BinaryValueExpressionNode(
                        new BinaryValueExpressionNode(
                                new NumberLiteralValue(null, 1),
                                new StringLiteralValue(null, "str"),
                                BinaryOperator.MINUS),
                        new NumberLiteralValue(null, 3),
                        BinaryOperator.PLUS));
    }

    @Test
    void relation() {
        assertThat(assertParsesWithoutErrors("10 + 6 > 5", ValueExpressionParser.lambda))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Token.class)
                .isEqualTo(new BinaryValueExpressionNode(
                        new BinaryValueExpressionNode(
                                new NumberLiteralValue(null, 10),
                                new NumberLiteralValue(null, 6),
                                BinaryOperator.PLUS),
                        new NumberLiteralValue(null, 5),
                        BinaryOperator.GREATER));
    }

    @Test
    void invalid() {
        assertThat(assertParsesWithErrors("() * 2", ValueExpressionParser.lambda))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Token.class)
                .isEqualTo(Optional.of(new BinaryValueExpressionNode(
                        InvalidValueNode.instance,
                        new NumberLiteralValue(null, 2),
                        BinaryOperator.MULTIPLY)));

        assertThat(assertParsesWithErrors("2 + (1 -)", ValueExpressionParser.lambda))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Token.class)
                .isEqualTo(Optional.of(new BinaryValueExpressionNode(
                        new NumberLiteralValue(null, 2),
                        new BinaryValueExpressionNode(
                                new NumberLiteralValue(null, 1),
                                InvalidValueNode.instance,
                                BinaryOperator.MINUS),
                        BinaryOperator.PLUS)));
    }

    @Test
    void sumWithUnary() {
        assertThat(assertParsesWithoutErrors("2 + -1", ValueExpressionParser.lambda))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Token.class)
                .isEqualTo(new BinaryValueExpressionNode(
                        new NumberLiteralValue(null, 2),
                        new UnaryValueExpressionNode(
                                new NumberLiteralValue(null, 1),
                                UnaryOperator.MINUS,
                                null),
                        BinaryOperator.PLUS));
    }

    @Test
    void priority() {
        assertThat(assertParsesWithoutErrors("1 * 2 + 3", ValueExpressionParser.lambda))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Token.class)
                .isEqualTo(new BinaryValueExpressionNode(
                        new BinaryValueExpressionNode(
                                new NumberLiteralValue(null, 1),
                                new NumberLiteralValue(null, 2),
                                BinaryOperator.MULTIPLY),
                        new NumberLiteralValue(null, 3),
                        BinaryOperator.PLUS));

        assertThat(assertParsesWithoutErrors("1 + 2 * 3", ValueExpressionParser.lambda))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(Token.class)
                .isEqualTo(new BinaryValueExpressionNode(
                        new NumberLiteralValue(null, 1),
                        new BinaryValueExpressionNode(
                                new NumberLiteralValue(null, 2),
                                new NumberLiteralValue(null, 3),
                                BinaryOperator.MULTIPLY),
                        BinaryOperator.PLUS));
    }
}
