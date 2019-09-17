package com.github.youta1119.kotlin.dotnet.compiler

import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.cli.common.config.KotlinSourceRoot
import org.jetbrains.kotlin.cli.jvm.compiler.createSourceFilesFromSourceRoots
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.context.ContextForNewModule
import org.jetbrains.kotlin.context.ModuleContext
import org.jetbrains.kotlin.context.ProjectContext
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList

object TopDownAnalyzerFacadeForDotNetIL {

    @JvmStatic
    fun analyzeFiles(
        files: Collection<KtFile>,
        config: Config
    ): AnalysisResult {

        val projectContext = ProjectContext(config.project, "TopDownAnalyzer for DotNet")
        val builtIns = DotNetBuiltIns(projectContext.storageManager)
        val context = ContextForNewModule(
            projectContext,
            Name.special("<${config.moduleId}>"),
            builtIns, null
        )
        val module = context.module
        builtIns.builtInsModule = module
        if(!module.isStdlib()) {
            context.setDependencies(listOf(module) + loadStdlibModules(projectContext, config))
        } else {
            context.setDependencies(module)
        }
        return analyzeFilesWithGivenTrace(files, BindingTraceContext(), context, config.configuration)
    }

    private fun analyzeFilesWithGivenTrace(
        files: Collection<KtFile>,
        trace: BindingTrace,
        moduleContext: ModuleContext,
        configuration: CompilerConfiguration
    ): AnalysisResult {

        // we print out each file we compile for now
        files.forEach{println(it)}

        val analyzerForKonan = createTopDownAnalyzerForDotNet(
            moduleContext, trace,
            FileBasedDeclarationProviderFactory(moduleContext.storageManager, files),
            configuration.get(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS)!!
        )

        analyzerForKonan.analyzeDeclarations(TopDownAnalysisMode.TopLevelDeclarations, files)
        return AnalysisResult.success(trace.bindingContext, moduleContext.module)
    }


    private fun loadStdlibModules(projectContext: ProjectContext, config: Config): ModuleDescriptorImpl {
        //createSourceFilesFromSourceRoots()
        val stdlibFiles = Files.walk(Paths.get(config.distribution.stdlibDir))
            .filter { it.toFile().extension == "kt" }
            .map { KotlinSourceRoot(it.toString(), false) }.toList()
        val ktFiles = createSourceFilesFromSourceRoots(config.configuration, config.project, stdlibFiles)
        val builtIns = DotNetBuiltIns(projectContext.storageManager)
        val context = ContextForNewModule(
            projectContext,
            STDLIB_MODULE_NAME,
            builtIns, null
        )
        val module = context.module
        builtIns.builtInsModule = module
        context.setDependencies(module)
        val analyzerForKonan = createTopDownAnalyzerForDotNet(
            context, BindingTraceContext(),
            FileBasedDeclarationProviderFactory(context.storageManager, ktFiles),
            config.configuration.get(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS)!!
        )

        analyzerForKonan.analyzeDeclarations(TopDownAnalysisMode.TopLevelDeclarations, ktFiles)
        return context.module

    }
}