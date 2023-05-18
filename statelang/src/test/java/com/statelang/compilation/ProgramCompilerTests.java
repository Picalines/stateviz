package com.statelang.compilation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.statelang.compilation.symbol.*;
import com.statelang.diagnostics.Report;
import com.statelang.diagnostics.Reporter;
import com.statelang.model.NumberInstanceType;
import com.statelang.model.StateMachine;
import com.statelang.tokenization.SourceSelection;
import com.statelang.tokenization.SourceText;

class ProgramCompilerTests {

    @Test
    void counter() {
        var reporter = new Reporter();

        var compiledProgram = ProgramCompiler.compile(reporter, SourceText.fromString("test", """
            state {
                COUNTING,
                STOPPED,
            }

            const max := 10;
            let count := 0;

            when COUNTING {
                assert count < max;
                count := count + 1;

                if count = max {
                    state := STOPPED;
                }
            }
            """));

        assertTrue(compiledProgram.isPresent());
        assertEquals(0, reporter.reports().size());

        assertThat(compiledProgram.get())
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringFields("jumpTable", "instructions")
            .isEqualTo(
                CompiledProgram.builder()
                    .symbol(new StateSymbol("COUNTING"))
                    .symbol(new StateSymbol("STOPPED"))
                    .symbol(new ConstantSymbol("max", NumberInstanceType.INSTANCE))
                    .symbol(new VariableSymbol("count", NumberInstanceType.INSTANCE))
                    .stateMachine(
                        StateMachine.builder()
                            .state("COUNTING")
                            .state("STOPPED")
                            .initialState("COUNTING")
                            .transition("COUNTING", "COUNTING")
                            .transition("COUNTING", "STOPPED")
                            .build()
                    )
                    .build()
            );
    }

    @Test
    void unreachableState() {
        var reporter = new Reporter();

        var compiledProgram = ProgramCompiler.compile(reporter, SourceText.fromString("test", """
            state { A, B, C, D }
            when A { state := B; }
            """));

        assertTrue(compiledProgram.isPresent());

        assertThat(reporter.reports())
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(SourceSelection.class, List.class)
            .ignoringCollectionOrder()
            .isEqualTo(
                Arrays.asList(
                    Report.builder()
                        .kind(Report.Kind.UNREACHABLE_STATE)
                        .info("C")
                        .build(),
                    Report.builder()
                        .kind(Report.Kind.UNREACHABLE_STATE)
                        .info("D")
                        .build()
                )
            );
    }

    @Test
    void duplicateIdentifier() {
        var reporter = new Reporter();

        var compiledProgram = ProgramCompiler.compile(reporter, SourceText.fromString("test", """
            state { A, A, B }
            let x := 0;
            let x := 1;
            when A { state := B; }
            """));

        assertTrue(compiledProgram.isEmpty());

        assertThat(reporter.reports())
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(SourceSelection.class, List.class)
            .ignoringCollectionOrder()
            .isEqualTo(
                Arrays.asList(
                    Report.builder()
                        .kind(Report.Kind.DUPLICATE_IDENTIFIER)
                        .info("A")
                        .build(),
                    Report.builder()
                        .kind(Report.Kind.DUPLICATE_IDENTIFIER)
                        .info("x")
                        .build()
                )
            );
    }

    @Test
    void duplicateWhen() {
        var reporter = new Reporter();

        var compiledProgram = ProgramCompiler.compile(reporter, SourceText.fromString("test", """
            state { A, B }
            when A { state := B; }
            when A { }
            """));

        assertTrue(compiledProgram.isEmpty());

        assertThat(reporter.reports())
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(SourceSelection.class, List.class)
            .ignoringCollectionOrder()
            .isEqualTo(
                Arrays.asList(
                    Report.builder()
                        .kind(Report.Kind.AMBIGUOUS_DEFINITION)
                        .build()
                )
            );
    }

    @Test
    void constantAssignment() {
        var reporter = new Reporter();

        var compiledProgram = ProgramCompiler.compile(reporter, SourceText.fromString("test", """
            state { A, B }
            const c := 1;
            when A {
                c := -1;
                state := B;
            }
            """));

        assertTrue(compiledProgram.isEmpty());

        assertThat(reporter.reports())
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(SourceSelection.class, List.class)
            .ignoringCollectionOrder()
            .isEqualTo(
                Arrays.asList(
                    Report.builder()
                        .kind(Report.Kind.CONSTANT_ASSIGNMENT)
                        .build()
                )
            );
    }

    @Test
    void undefinedStateTransition() {
        var reporter = new Reporter();

        var compiledProgram = ProgramCompiler.compile(reporter, SourceText.fromString("test", """
            state { A, B }
            when A {
                state := C;
            }
            """));

        assertTrue(compiledProgram.isEmpty());

        assertThat(reporter.reports())
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(SourceSelection.class, List.class)
            .ignoringCollectionOrder()
            .isEqualTo(
                Arrays.asList(
                    Report.builder()
                        .kind(Report.Kind.UNDEFINED_STATE)
                        .info("C")
                        .build()
                )
            );
    }

    @Test
    void typeError() {
        var reporter = new Reporter();

        var compiledProgram = ProgramCompiler.compile(reporter, SourceText.fromString("test", """
            state { A, B }
            let x := 0;
            when A {
                x := true;
                state := B;
            }
            """));

        assertTrue(compiledProgram.isEmpty());

        assertThat(reporter.reports())
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(SourceSelection.class, List.class)
            .ignoringCollectionOrder()
            .isEqualTo(
                Arrays.asList(
                    Report.builder()
                        .kind(Report.Kind.TYPE_ERROR)
                        .info("boolean number")
                        .build()
                )
            );
    }
}
