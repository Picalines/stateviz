package com.stateviz.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.statelang.compilation.ProgramCompiler;
import com.statelang.diagnostics.Reporter;
import com.stateviz.models.CompilationResult;
import com.stateviz.models.SourceTextData;

@RestController
public class StatelangController {

    @PostMapping(path = "/statelang/compile", consumes = "application/json", produces = "application/json")
    public CompilationResult compile(@RequestBody SourceTextData sourceTextData) {
        var reporter = new Reporter();

        var sourceText = sourceTextData.toSourceText();
        var program = ProgramCompiler.compile(reporter, sourceText);

        return new CompilationResult(reporter.reports(), program);
    }
}
