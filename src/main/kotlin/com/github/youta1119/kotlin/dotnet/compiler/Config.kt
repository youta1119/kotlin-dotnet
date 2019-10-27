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

    val ilAsmFile = this.createTempFile()

    private fun createTempFile(): File {
        val pathToTemporaryDir = configuration.get(DotNetConfigurationKeys.TEMPORARY_FILES_DIR)?.let { Paths.get(it) }
        val deleteOnExit = pathToTemporaryDir == null

        val pathToTempFile = if (pathToTemporaryDir != null) {
            pathToTemporaryDir.toFile().mkdirs()
            File(pathToTemporaryDir.toString(),"$outputName.il").toPath()
        } else {
            Files.createTempFile(outputName, ".il")
        }
        val file = pathToTempFile.toFile()
        if (deleteOnExit) {
            file.deleteOnExit()
        }
        return file
    }

    companion object {
        private const val DEFAULT_MODULE_NAME = "main"
        private const val DEFAULT_OUTPUT_NAME = "program"
    }
}