package com.github.youta1119.kotlin.dotnet.compiler.exec

import com.github.youta1119.kotlin.dotnet.compiler.DotNetCompilationException

class ILAsm(private val args: List<String>) {

    fun execute() {
        val command = listOf("ilasm") + args
        val builder = ProcessBuilder(command)

        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        builder.redirectInput(ProcessBuilder.Redirect.INHERIT)
        builder.redirectError(ProcessBuilder.Redirect.INHERIT)

        val process = builder.start()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            throw DotNetCompilationException("ilasm command returned non-zero exit code: $exitCode. args=$args")
        }
    }
}