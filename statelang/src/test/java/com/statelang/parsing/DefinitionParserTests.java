package com.statelang.parsing;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import com.statelang.tokenization.SourceLocation;
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
                            new InvalidValueNode(null)
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
                            new InvalidValueNode(null)
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
                    new StateDefinition(
                        Arrays.asList(),
                        new Token(
                            new SourceSelection(new SourceLocation(1, 1), new SourceLocation(1, 5)),
                            Token.Kind.KEYWORD_STATE,
                            "state"
                        )
                    )
                );
        }

        private static List<StateDefinition.State> listOfStates(String... states) {
            return Arrays.stream(states)
                .map(
                    name -> new StateDefinition.State(
                        new Token(null, Token.Kind.IDENTIFIER, name), Collections.emptyList()
                    )
                )
                .toList();
        }

        @Test
        void singleState() {
            assertThat(assertParsesWithoutErrors("state { SINGLE }", DefinitionParser.state))
                .usingRecursiveComparison()
                .ignoringFields("stateToken")
                .ignoringFieldsOfTypes(SourceSelection.class, Token.Kind.class)
                .isEqualTo(
                    new StateDefinition(listOfStates("SINGLE"), null)
                );
        }

        @Test
        void manyStates() {
            assertThat(assertParsesWithoutErrors("state { A, B }", DefinitionParser.state))
                .usingRecursiveComparison()
                .ignoringFields("stateToken")
                .ignoringFieldsOfTypes(SourceSelection.class, Token.Kind.class)
                .isEqualTo(
                    new StateDefinition(listOfStates("A", "B"), null)
                );

            assertThat(assertParsesWithoutErrors("state { A, B, C }", DefinitionParser.state))
                .usingRecursiveComparison()
                .ignoringFields("stateToken")
                .ignoringFieldsOfTypes(SourceSelection.class, Token.Kind.class)
                .isEqualTo(
                    new StateDefinition(listOfStates("A", "B", "C"), null)
                );

            assertThat(assertParsesWithoutErrors("state { A, B, C, }", DefinitionParser.state))
                .usingRecursiveComparison()
                .ignoringFields("stateToken")
                .ignoringFieldsOfTypes(SourceSelection.class, Token.Kind.class)
                .isEqualTo(
                    new StateDefinition(listOfStates("A", "B", "C"), null)
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
