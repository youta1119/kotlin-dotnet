package com.github.youta1119.kotlin.dotnet.cli

import com.github.youta1119.kotlin.dotnet.compiler.runTopLevelPhases
import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.cli.common.CLICompiler
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.CommonCompilerPerformanceManager
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoot
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.cli.common.messages.OutputMessageUtil
import org.jetbrains.kotlin.cli.common.modules.ModuleBuilder
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.codegen.CompilationException
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.utils.KotlinPaths
import java.io.File
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.streams.toList

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

        val messageCollector = configuration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)
        try {
            val environment = createCoreEnvironment(rootDisposable, configuration, messageCollector)
                ?: return ExitCode.COMPILATION_ERROR
            runTopLevelPhases(environment)
            return ExitCode.OK
        } catch (e: CompilationException) {
            messageCollector.report(
                CompilerMessageSeverity.EXCEPTION,
                OutputMessageUtil.renderException(e),
                MessageUtil.psiElementToMessageLocation(e.element)
            )
            return ExitCode.INTERNAL_ERROR
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
        loadStdlibs("./stdlib").forEach {
            configuration.addKotlinSourceRoot(it, it in commonSources)
        }
        val moduleName = arguments.moduleName ?: DEFAULT_MODULE_NAME
        configuration.put(CommonConfigurationKeys.MODULE_NAME, moduleName)
    }

    override fun createArguments(): K2DotNetCompilerArguments =
        K2DotNetCompilerArguments()

    override fun createMetadataVersion(versionArray: IntArray): BinaryVersion =
        K2DotNetMetadataVersion()

    override fun executableScriptFileName(): String = "kotlinc-dotnet"

    private fun createCoreEnvironment(
        rootDisposable: Disposable,
        configuration: CompilerConfiguration,
        messageCollector: MessageCollector
    ): KotlinCoreEnvironment? {
        if (messageCollector.hasErrors()) return null

        val environment = KotlinCoreEnvironment.createForProduction(
            rootDisposable,
            configuration,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        )
        performanceManager.notifyCompilerInitialized()
        return if (messageCollector.hasErrors()) null else environment
    }

    protected class K2DotNetCompilerPerformanceManager : CommonCompilerPerformanceManager("Kotlin to .Net Compiler")
    companion object {
        const val DEFAULT_MODULE_NAME = "main"
        @JvmStatic
        fun main(args: Array<String>) {
            doMain(K2DotNetCompiler(), args)
        }
    }
}

fun loadStdlibs(path: String) : List<String>{
    //val files = mutableListOf<String>()
    return Files.walk(Paths.get(path)).filter { it.toFile().extension == "kt" }.map { it.toString() }.toList()
}
fun main(args: Array<String>) = K2DotNetCompiler.main(args)