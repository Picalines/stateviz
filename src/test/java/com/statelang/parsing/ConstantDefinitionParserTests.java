package com.statelang.parsing;

import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithErrors;
import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithoutErrors;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.statelang.ast.ConstantDefinition;
import com.statelang.ast.InvalidValueNode;
import com.statelang.ast.NumberLiteralValue;
import com.statelang.tokenization.SourceSelection;
import com.statelang.tokenization.SourceText;
import com.statelang.tokenization.Token;

class ConstantDefinitionParserTests {

    @Test
    void normal() {
        assertThat(assertParsesWithoutErrors("const x := 123;", ConstantDefinitionParser.parser))
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
        assertThat(assertParsesWithErrors("const x := ;", ConstantDefinitionParser.parser))
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
        assertThat(assertParsesWithErrors("const x := 123", ConstantDefinitionParser.parser))
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
