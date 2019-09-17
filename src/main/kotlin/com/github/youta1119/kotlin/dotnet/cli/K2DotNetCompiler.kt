package com.github.youta1119.kotlin.dotnet.cli

import com.github.youta1119.kotlin.dotnet.compiler.Config
import com.github.youta1119.kotlin.dotnet.compiler.runTopLevelPhases
import com.github.youta1119.kotlin.dotnet.compiler.toplevelPhase
import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.cli.common.*
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoot
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.cli.common.messages.OutputMessageUtil
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.codegen.CompilationException
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.utils.KotlinPaths

open class K2DotNetCompiler : CLICompiler<K2DotNetCompilerArguments>() {

    override val performanceManager: CommonCompilerPerformanceManager by lazy {
        K2DotNetCompilerPerformanceManager()
    }

    override fun doExecute(
        arguments: K2DotNetCompilerArguments,
        configuration: CompilerConfiguration,
        rootDisposable: Disposable,
        paths: KotlinPaths?
    ): ExitCode {
        val environment = KotlinCoreEnvironment.createForProduction(
            rootDisposable,
            configuration,
            EnvironmentConfigFiles.NATIVE_CONFIG_FILES
        )
        val project = environment.project
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY) ?: MessageCollector.NONE
        configuration.put(CLIConfigurationKeys.PHASE_CONFIG, createPhaseConfig(toplevelPhase, arguments, messageCollector))
        val config = Config(project, configuration)
        return try {
            runTopLevelPhases(config, environment)
            ExitCode.OK
        } catch (e: CompilationException) {
            messageCollector.report(
                CompilerMessageSeverity.EXCEPTION,
                OutputMessageUtil.renderException(e),
                MessageUtil.psiElementToMessageLocation(e.element)
            )
            ExitCode.INTERNAL_ERROR
        }
    }

    override fun setupPlatformSpecificArgumentsAndServices(
        configuration: CompilerConfiguration,
        arguments: K2DotNetCompilerArguments,
        services: Services
    ) {
        val commonSources = arguments.commonSources?.toSet().orEmpty()
        arguments.freeArgs.forEach {
            configuration.addKotlinSourceRoot(it, it in commonSources)
        }
        /*loadStdlibs("./stdlib").forEach {
            configuration.addKotlinSourceRoot(it, it in commonSources)
        }*/
    }

    override fun createArguments(): K2DotNetCompilerArguments =
        K2DotNetCompilerArguments()

    override fun createMetadataVersion(versionArray: IntArray): BinaryVersion =
        K2DotNetMetadataVersion()

    override fun executableScriptFileName(): String = "kotlinc-dotnet"

    protected class K2DotNetCompilerPerformanceManager : CommonCompilerPerformanceManager("Kotlin to .Net Compiler")
    companion object {
        const val DEFAULT_MODULE_NAME = "main"
        @JvmStatic
        fun main(args: Array<String>) {
            doMain(K2DotNetCompiler(), args)
        }
    }
}

fun main(args: Array<String>) = K2DotNetCompiler.main(args)