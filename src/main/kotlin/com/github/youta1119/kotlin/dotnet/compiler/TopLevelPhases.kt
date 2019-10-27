package com.github.youta1119.kotlin.dotnet.compiler

import com.github.youta1119.kotlin.dotnet.compiler.dotnet.CodeGeneratorVisitor
import com.github.youta1119.kotlin.dotnet.compiler.exec.ILAsm
import com.github.youta1119.kotlin.dotnet.compiler.ir.DotNetIr
import org.jetbrains.kotlin.backend.common.phaser.*
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.Psi2IrTranslator
import org.jetbrains.kotlin.psi2ir.generators.GeneratorExtensions


internal fun createUnitPhase(
    name: String,
    description: String,
    prerequisite: Set<AnyNamedPhase> = emptySet(),
    op: Context.() -> Unit
) = namedOpUnitPhase(name, description, prerequisite, op)

internal val frontendPhase = createUnitPhase(
    op = {
        val environment = environment
        val analyzerWithCompilerReport = AnalyzerWithCompilerReport(
            messageCollector,
            environment.configuration.languageVersionSettings
        )

        // Build AST and binding info.
        analyzerWithCompilerReport.analyzeAndReport(environment.getSourceFiles()) {
            TopDownAnalyzerFacadeForDotNetIL.analyzeFiles(environment.getSourceFiles(), this.config)
        }
        if (analyzerWithCompilerReport.hasErrors()) {
            throw DotNetCompilationException()
        }
        moduleDescriptor = analyzerWithCompilerReport.analysisResult.moduleDescriptor
        bindingContext = analyzerWithCompilerReport.analysisResult.bindingContext
    },
    name = "Frontend",
    description = "Frontend builds AST"
)

internal val psiToIrPhase = createUnitPhase(
    op = {
        val translator = Psi2IrTranslator(environment.configuration.languageVersionSettings, Psi2IrConfiguration(false))
        val symbolTable = SymbolTable()
        val generatorContext = translator.createGeneratorContext(
            moduleDescriptor,
            bindingContext,
            symbolTable,
            GeneratorExtensions()
        )
        val module = translator.generateModuleFragment(generatorContext, environment.getSourceFiles())
        irModule = module
        //val symbols = DotNetSymbols(this, symbolTable, symbolTable.lazyWrapper)
        ir = DotNetIr(this, module)
        this.symbolTable = symbolTable
        //ir.symbols = symbols
    },
    name = "Psi2Ir",
    description = "Psi to IR conversion"
)

internal val irToCILPhase = createUnitPhase(
    op = {
        CodeGeneratorVisitor(this).use {
            irModule.acceptVoid(it)
        }
    },
    name = "IrToCIL",
    description = "IR to CLI(Common Intermediate Language) conversion"
)


internal val compileCILPhase = createUnitPhase(
    op = {
        ILAsm(args = listOf("/output:${config.outputName}.exe", config.ilAsmFile.absolutePath)).execute()
    },
    name = "compileCIL",
    description = "Compile CIL"
)
val toplevelPhase: CompilerPhase<*, Unit, Unit> = namedUnitPhase(
    name = "Compiler",
    description = "The whole compilation process",
    lower = frontendPhase then
            psiToIrPhase then
            irToCILPhase then
            compileCILPhase
)
