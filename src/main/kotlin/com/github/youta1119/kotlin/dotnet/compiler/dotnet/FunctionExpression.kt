package com.github.youta1119.kotlin.dotnet.compiler.dotnet

import org.jetbrains.kotlin.name.Name

class FunctionExpression(
    private val name: Name,
    private val body: List<Expression>,
    private val isEtryPoint: Boolean,
    private val maxStackSize: Int = 8,
    private val returnType: DotNetPrimitiveType = DotNetPrimitiveType.Void
) : Expression {

    override fun emit(writer: CodeWriter) {
        writer.write(".method static ${returnType.name} $name() cil managed {")
        writer.indent()
        body.forEach { it.emit(writer) }
        writer.write("ret")
        writer.unindent()
        writer.write("}")
    }
}
