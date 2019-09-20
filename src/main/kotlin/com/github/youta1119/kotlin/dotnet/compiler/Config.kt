package com.github.youta1119.kotlin.dotnet.compiler

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path


class Config(val project: Project, val configuration: CompilerConfiguration) {
    val distribution = Distribution()
    val moduleId: String
        get() = configuration.get(DotNetConfigurationKeys.MODULE_NAME) ?: DEFAULT_MODULE_NAME
    val phaseConfig = configuration.get(CLIConfigurationKeys.PHASE_CONFIG)!!

    val outputName: String
        get() = configuration.get(DotNetConfigurationKeys.OUTPUT_NAME) ?: DEFAULT_OUTPUT_NAME

    val tempFile = this.createTempFile(outputName, ".il")

    private fun createTempFile(prefix: String, suffix: String): File {
        val tempFile = Files.createTempFile(prefix, suffix)
        return File(tempFile.toUri()).also { it.deleteOnExit() }
    }

    companion object {
        private const val DEFAULT_MODULE_NAME = "main"
        private const val DEFAULT_OUTPUT_NAME = "program"
    }
}