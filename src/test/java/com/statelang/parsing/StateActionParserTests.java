package com.statelang.parsing;

import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithErrors;
import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithoutErrors;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.statelang.ast.AssertionAction;
import com.statelang.ast.AssignmentAction;
import com.statelang.ast.BinaryValueExpressionNode;
import com.statelang.ast.ConditionalAction;
import com.statelang.ast.NumberLiteralValue;
import com.statelang.ast.StateAction;
import com.statelang.ast.StateActionBlock;
import com.statelang.ast.TransitionAction;
import com.statelang.ast.VariableValueNode;
import com.statelang.model.BinaryOperator;
import com.statelang.tokenization.SourceSelection;
import com.statelang.tokenization.Token;

class StateActionParserTests {

    @Test
    void transition() {
        assertThat(assertParsesWithoutErrors("state := NEW;", StateActionParser.transition))
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(SourceSelection.class)
            .isEqualTo(
                new TransitionAction(new Token(null, Token.Kind.IDENTIFIER, "NEW"))
            );
    }

    @Test
    void assignment() {
        assertThat(assertParsesWithoutErrors("x := 123;", StateActionParser.assignment))
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(SourceSelection.class)
            .isEqualTo(
                new AssignmentAction(
                    new Token(null, Token.Kind.IDENTIFIER, "x"),
                    new NumberLiteralValue(new Token(null, Token.Kind.LITERAL_NUMBER, "123"), 123)
                )
            );
    }

    @Test
    void assertion() {
        assertThat(assertParsesWithoutErrors("assert x > 5;", StateActionParser.assertion))
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(SourceSelection.class)
            .isEqualTo(
                new AssertionAction(
                    new BinaryValueExpressionNode(
                        BinaryOperator.GREATER,
                        new VariableValueNode(new Token(null, Token.Kind.IDENTIFIER, "x")),
                        new NumberLiteralValue(new Token(null, Token.Kind.LITERAL_NUMBER, "5"), 5)
                    )
                )
            );
    }

    @Test
    void conditionalOnlyTrue() {
        assertThat(assertParsesWithoutErrors("if x > 5 {}", StateActionParser.conditional))
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(SourceSelection.class)
            .isEqualTo(
                new ConditionalAction(
                    new BinaryValueExpressionNode(
                        BinaryOperator.GREATER,
                        new VariableValueNode(new Token(null, Token.Kind.IDENTIFIER, "x")),
                        new NumberLiteralValue(new Token(null, Token.Kind.LITERAL_NUMBER, "5"), 5)
                    ),
                    new StateActionBlock(Arrays.asList()),
                    null
                )
            );
    }

    @Test
    void conditionalBoth() {
        assertThat(assertParsesWithoutErrors("if x > 5 {} else {}", StateActionParser.conditional))
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(SourceSelection.class)
            .isEqualTo(
                new ConditionalAction(
                    new BinaryValueExpressionNode(
                        BinaryOperator.GREATER,
                        new VariableValueNode(new Token(null, Token.Kind.IDENTIFIER, "x")),
                        new NumberLiteralValue(new Token(null, Token.Kind.LITERAL_NUMBER, "5"), 5)
                    ),
                    new StateActionBlock(Arrays.asList()),
                    new StateActionBlock(Arrays.asList())
                )
            );
    }

    @Test
    void emptyBlock() {
        assertThat(assertParsesWithoutErrors("{}", StateActionParser.block))
            .usingRecursiveComparison()
            .isEqualTo(new StateActionBlock(Arrays.asList()));
    }

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
