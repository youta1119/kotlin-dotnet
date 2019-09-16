package com.github.youta1119.kotlin.dotnet.compiler

import org.jetbrains.kotlin.backend.common.serialization.KotlinIr
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.Psi2IrTranslator
import org.jetbrains.kotlin.psi2ir.generators.GeneratorExtensions



private val KotlinCoreEnvironment.messageCollector: MessageCollector
    get() = configuration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)

fun runTopLevelPhases(environment: KotlinCoreEnvironment) {
    val collector = environment.messageCollector
    val analyzerWithCompilerReport =
        AnalyzerWithCompilerReport(collector, environment.configuration.languageVersionSettings)
    analyzerWithCompilerReport.analyzeAndReport(environment.getSourceFiles()) {
        val project = environment.project
        TopDownAnalyzerFacadeForDotNetIL.analyzeFiles(
            project,
            environment.getSourceFiles(),
            environment.configuration
        )
    }
    val moduleDescriptor = analyzerWithCompilerReport.analysisResult.moduleDescriptor
    val bindingContext = analyzerWithCompilerReport.analysisResult.bindingContext
    val translator = Psi2IrTranslator(environment.configuration.languageVersionSettings, Psi2IrConfiguration(false))
    val module =
        translator.generateModule(moduleDescriptor, environment.getSourceFiles(), bindingContext, GeneratorExtensions())
    module.files.forEach { irFile ->
        println("fqname=${irFile.fqName}")
        irFile.declarations.forEach { declaration ->
            println(declaration.dump())
        }
    }
    //context.irModule = module

}