package com.statelang.parsing;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithErrors;
import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithoutErrors;
import static org.assertj.core.api.Assertions.assertThat;

import com.statelang.ast.ConstantDefinition;
import com.statelang.ast.InStateDefinition;
import com.statelang.ast.InvalidValueNode;
import com.statelang.ast.NumberLiteralValue;
import com.statelang.ast.StateActionBlock;
import com.statelang.ast.StateDefinition;
import com.statelang.ast.VariableDefinition;
import com.statelang.tokenization.SourceSelection;
import com.statelang.tokenization.SourceText;
import com.statelang.tokenization.Token;

class DefinitionParserTests {

    @Nested
    class Constant {

        @Test
        void normal() {
            assertThat(assertParsesWithoutErrors("const x := 123;", DefinitionParser.constant))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(SourceSelection.class, SourceText.class)
                .isEqualTo(
                    new ConstantDefinition(
                        new Token(null, Token.Kind.IDENTIFIER, "x"),
                        new NumberLiteralValue(new Token(null, Token.Kind.LITERAL_NUMBER, "123"), 123)
                    )
                );
        }

        @Test
        void withoutExpression() {
            assertThat(assertParsesWithErrors("const x := ;", DefinitionParser.constant))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(SourceSelection.class, SourceText.class)
                .isEqualTo(
                    Optional.of(
                        new ConstantDefinition(
                            new Token(null, Token.Kind.IDENTIFIER, "x"),
                            InvalidValueNode.instance
                        )
                    )
                );
        }

        @Test
        void withoutSemicollon() {
            assertThat(assertParsesWithErrors("const x := 123", DefinitionParser.constant))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(SourceSelection.class, SourceText.class)
                .isEqualTo(
                    Optional.of(
                        new ConstantDefinition(
                            new Token(null, Token.Kind.IDENTIFIER, "x"),
                            new NumberLiteralValue(new Token(null, Token.Kind.LITERAL_NUMBER, "123"), 123)
                        )
                    )
                );
        }
    }

    @Nested
    class Variable {

        @Test
        void normal() {
            assertThat(assertParsesWithoutErrors("let x := 123;", DefinitionParser.variable))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(SourceSelection.class, SourceText.class)
                .isEqualTo(
                    new VariableDefinition(
                        new Token(null, Token.Kind.IDENTIFIER, "x"),
                        new NumberLiteralValue(new Token(null, Token.Kind.LITERAL_NUMBER, "123"), 123)
                    )
                );
        }

        @Test
        void withoutExpression() {
            assertThat(assertParsesWithErrors("let x := ;", DefinitionParser.variable))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(SourceSelection.class, SourceText.class)
                .isEqualTo(
                    Optional.of(
                        new VariableDefinition(
                            new Token(null, Token.Kind.IDENTIFIER, "x"),
                            InvalidValueNode.instance
                        )
                    )
                );
        }

        @Test
        void withoutSemicollon() {
            assertThat(assertParsesWithErrors("let x := 123", DefinitionParser.variable))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(SourceSelection.class, SourceText.class)
                .isEqualTo(
                    Optional.of(
                        new VariableDefinition(
                            new Token(null, Token.Kind.IDENTIFIER, "x"),
                            new NumberLiteralValue(new Token(null, Token.Kind.LITERAL_NUMBER, "123"), 123)
                        )
                    )
                );
        }
    }

    @Nested
    class State {

        @Test
        void emptyList() {
            assertThat(assertParsesWithoutErrors("state {}", DefinitionParser.state))
                .usingRecursiveComparison()
                .isEqualTo(
                    new StateDefinition(Arrays.asList())
                );
        }

        @Test
        void singleState() {
            assertThat(assertParsesWithoutErrors("state { SINGLE }", DefinitionParser.state))
                .usingRecursiveComparison()
                .isEqualTo(
                    new StateDefinition(Arrays.asList("SINGLE"))
                );
        }

        @Test
        void manyStates() {
            assertThat(assertParsesWithoutErrors("state { A, B }", DefinitionParser.state))
                .usingRecursiveComparison()
                .isEqualTo(
                    new StateDefinition(Arrays.asList("A", "B"))
                );

            assertThat(assertParsesWithoutErrors("state { A, B, C }", DefinitionParser.state))
                .usingRecursiveComparison()
                .isEqualTo(
                    new StateDefinition(Arrays.asList("A", "B", "C"))
                );

            assertThat(assertParsesWithoutErrors("state { A, B, C, }", DefinitionParser.state))
                .usingRecursiveComparison()
                .isEqualTo(
                    new StateDefinition(Arrays.asList("A", "B", "C"))
                );
        }
    }

    @Nested
    class InState {

        @Test
        void normal() {
            assertThat(assertParsesWithoutErrors("when A {}", DefinitionParser.inState))
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(SourceSelection.class)
                .isEqualTo(
                    new InStateDefinition(
                        new Token(null, Token.Kind.IDENTIFIER, "A"),
                        new StateActionBlock(Arrays.asList())
                    )
                );
        }
    }
}
