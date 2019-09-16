package com.github.youta1119.kotlin.dotnet.compiler

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.languageVersionSettings

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
}