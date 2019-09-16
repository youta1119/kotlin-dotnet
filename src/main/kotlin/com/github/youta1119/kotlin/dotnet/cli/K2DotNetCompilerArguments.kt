package com.github.youta1119.kotlin.dotnet.cli

import org.jetbrains.kotlin.cli.common.arguments.Argument
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments

class K2DotNetCompilerArguments: CommonCompilerArguments() {
    @Argument(value = "-module-name", valueDescription = "<name>", description = "Name of the generated .kotlin_module file")
    var moduleName: String? by NullableStringFreezableVar(null)
}