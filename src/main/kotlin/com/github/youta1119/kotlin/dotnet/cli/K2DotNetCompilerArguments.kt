package com.github.youta1119.kotlin.dotnet.cli

import org.jetbrains.kotlin.cli.common.arguments.Argument
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments

class K2DotNetCompilerArguments: CommonCompilerArguments() {
    @Argument(value="-module-name", deprecatedName = "-module_name", valueDescription = "<name>", description = "Specify a name for the compilation module")
    var moduleName: String? = null

    @Argument(value = "-output", shortName = "-o", valueDescription = "<name>", description = "Output name")
    var outputName: String? = null
}