package com.github.youta1119.kotlin.dotnet.compiler

import org.jetbrains.kotlin.backend.common.phaser.CompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.invokeToplevel
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.Psi2IrTranslator
import org.jetbrains.kotlin.psi2ir.generators.GeneratorExtensions


private val KotlinCoreEnvironment.messageCollector: MessageCollector
    get() = configuration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)

@Suppress("UNCHECKED_CAST")
fun runTopLevelPhases(config: Config, environment: KotlinCoreEnvironment) {
    val context = Context(config)
    context.environment = environment
    (toplevelPhase as CompilerPhase<Context, Unit, Unit>).invokeToplevel(context.phaseConfig, context, Unit)
}