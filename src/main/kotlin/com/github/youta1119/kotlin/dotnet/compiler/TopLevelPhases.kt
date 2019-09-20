package com.github.youta1119.kotlin.dotnet.compiler

import com.github.youta1119.kotlin.dotnet.compiler.exec.ILAsm
import com.github.youta1119.kotlin.dotnet.compiler.ir.DotNetIr
import com.github.youta1119.kotlin.dotnet.compiler.ir.findMainEntryPoint
import org.jetbrains.kotlin.backend.common.phaser.*
import org.jetbrains.kotlin.backend.common.serialization.target
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.Psi2IrTranslator
import org.jetbrains.kotlin.psi2ir.generators.GeneratorExtensions
import java.io.FileWriter
import java.io.BufferedWriter
import java.io.PrintWriter



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
        val pw = PrintWriter(BufferedWriter(FileWriter(config.tempFile)))
        pw.println(".assembly extern mscorlib {}")
        pw.println(".assembly ${config.moduleId} {}")
        pw.println(".method static void Main() cil managed {")
        pw.println(".entrypoint")
        val entryPoint = symbolTable.referenceSimpleFunction(findMainEntryPoint(this))
        ///println("owner= ${entryPoint.owner} desc=${entryPoint.descriptor}")
        val body = entryPoint.owner.body!! as IrBlockBody
        body.statements.forEach { statement ->
            if (statement is IrCall) {
                val function = statement.symbol.owner.target
                if (function.fqNameForIrSerialization.asString() == "kotlin.io.println") {
                    val args =  (statement as IrMemberAccessExpression).getArguments()
                    args.forEach {(_, expr) ->
                        if (expr is IrConst<*>) {
                            pw.println("ldstr \"${expr.value}\"")
                        }
                    }
                    pw.println("call void [mscorlib]System.Console::WriteLine(string)")
                }
            }
        }
        pw.println("ret")
        pw.println("}")
        pw.close()
    },
    name = "IrToCIL",
    description = "IR to CLI(Common Intermediate Language) conversion"
)


internal val compileCILPhase = createUnitPhase(
    op = {
        ILAsm(args = listOf("/output:${config.outputName}.exe",config.tempFile.absolutePath)).execute()
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