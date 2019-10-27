package com.github.youta1119.kotlin.dotnet.compiler.dotnet

import java.io.Closeable
import java.io.PrintWriter

class CodeWriter(
    private val writer: PrintWriter,
    private val indent: String = DEFAULT_INDENT
) : Closeable {
    private var indentLevel = 0

    fun indent(levels: Int = 1) {
        indentLevel += levels
    }

    fun unindent(levels: Int = 1) {
        require(indentLevel - levels >= 0) { "cannot unindent $levels from $indentLevel" }
        indentLevel -= levels
    }

    fun write(string: String) {
        if (indentLevel != 0) writer.print(indent * indentLevel)
        writer.println(string)
    }

    private operator fun String.times(count: Int): String {
        val builder = StringBuilder()
        repeat(count) { builder.append(this) }
        return builder.toString()
    }

    override fun close() {
        writer.close()
    }
    companion object {
        private const val DEFAULT_INDENT = "  "
    }
}
