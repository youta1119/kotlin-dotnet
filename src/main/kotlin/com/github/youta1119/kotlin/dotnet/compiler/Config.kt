package com.github.youta1119.kotlin.dotnet.compiler

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl

class Config(val project: Project, val configuration: CompilerConfiguration) {
    val distribution = Distribution()
    val moduleId: String
        get() = configuration.get(CommonConfigurationKeys.MODULE_NAME) ?: DEFAULT_MODULE_NAME
    val phaseConfig = configuration.get(CLIConfigurationKeys.PHASE_CONFIG)!!
    companion object {
        const val DEFAULT_MODULE_NAME = "main"
    }
}