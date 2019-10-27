package com.github.youta1119.kotlin.dotnet.compiler.dotnet

import org.jetbrains.kotlin.name.Name

class FunctionExpression(
    private val name: Name,
    private val variables: List<VariableInfo>,
    private val body: List<Expression>,
    private val isEntryPoint: Boolean,
    private val maxStackSize: Int = 8,
    private val returnType: DotNetPrimitiveType = DotNetPrimitiveType.STRING
) : Expression {

    override fun emit(writer: CodeWriter) {
        writer.write(".method static $returnType $name() cil managed {")
        writer.indent()
        if (isEntryPoint) {
            writer.write(".entrypoint")
        }
        writer.write(".maxstack $maxStackSize")
        variables.forEach {
            writer.write(".locals (${it.type} V_${it.index})")
        }
        body.forEach { it.emit(writer) }
        writer.write("ret")
        writer.unindent()
        writer.write("}")
    }
}
