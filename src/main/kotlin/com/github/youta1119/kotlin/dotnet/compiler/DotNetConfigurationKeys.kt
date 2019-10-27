package com.github.youta1119.kotlin.dotnet.compiler

import org.jetbrains.kotlin.config.CompilerConfigurationKey

class DotNetConfigurationKeys {
    companion object {
        val MODULE_NAME: CompilerConfigurationKey<String?> = CompilerConfigurationKey.create("module name")
        val OUTPUT_NAME: CompilerConfigurationKey<String?> = CompilerConfigurationKey.create("write name")
        val TEMPORARY_FILES_DIR: CompilerConfigurationKey<String?> =
            CompilerConfigurationKey.create("directory for temporary files")
    }
}