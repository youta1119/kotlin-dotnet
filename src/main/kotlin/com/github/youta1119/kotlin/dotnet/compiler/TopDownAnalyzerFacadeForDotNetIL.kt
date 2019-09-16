package com.github.youta1119.kotlin.dotnet.compiler

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.context.ContextForNewModule
import org.jetbrains.kotlin.context.ModuleContext
import org.jetbrains.kotlin.context.ProjectContext
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory

object TopDownAnalyzerFacadeForDotNetIL {

    @JvmStatic
    fun analyzeFiles(
        project: Project,
        files: Collection<KtFile>,
        configuration: CompilerConfiguration
    ): AnalysisResult {

        val projectContext = ProjectContext(project, "TopDownAnalyzer for DotNet")
        val builtIns = DotNetBuiltIns(projectContext.storageManager)
        val context = ContextForNewModule(
            projectContext,
            Name.special("<${configuration.getNotNull(CommonConfigurationKeys.MODULE_NAME)}>"),
            builtIns, null
        )
        val module = context.module
        builtIns.builtInsModule = module
        context.setDependencies(module)
        return analyzeFilesWithGivenTrace(files, BindingTraceContext(), context, configuration)
    }

    fun analyzeFilesWithGivenTrace(
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
}