package com.statelang.parsing;

import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithoutErrors;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.statelang.ast.StateDefinition;

class StateDefinitionParserTests {

    @Test
    void emptyList() {
        assertThat(assertParsesWithoutErrors("state {}", StateDefinitionParser.parser))
            .usingRecursiveComparison()
            .isEqualTo(
                new StateDefinition(Arrays.asList())
            );
    }

    @Test
    void singleState() {
        assertThat(assertParsesWithoutErrors("state { SINGLE }", StateDefinitionParser.parser))
            .usingRecursiveComparison()
            .isEqualTo(
                new StateDefinition(Arrays.asList("SINGLE"))
            );
    }

    @Test
    void manyStates() {
        assertThat(assertParsesWithoutErrors("state { A, B }", StateDefinitionParser.parser))
            .usingRecursiveComparison()
            .isEqualTo(
                new StateDefinition(Arrays.asList("A", "B"))
            );

        assertThat(assertParsesWithoutErrors("state { A, B, C }", StateDefinitionParser.parser))
            .usingRecursiveComparison()
            .isEqualTo(
                new StateDefinition(Arrays.asList("A", "B", "C"))
            );

        assertThat(assertParsesWithoutErrors("state { A, B, C, }", StateDefinitionParser.parser))
            .usingRecursiveComparison()
            .isEqualTo(
                new StateDefinition(Arrays.asList("A", "B", "C"))
            );
    }
}
