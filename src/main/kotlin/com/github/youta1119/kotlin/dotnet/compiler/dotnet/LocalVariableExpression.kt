package com.github.youta1119.kotlin.dotnet.compiler.dotnet

data class VariableInfo(val index: Int, val type: DotNetPrimitiveType)

class SetVariableValueExpression(
    private val info: VariableInfo,
    private val loadValueExpression: Expression
) : Expression {
    override fun emit(writer: CodeWriter) {
        loadValueExpression.emit(writer)
        // see. ecma-335 III.3.63
        val opcode = when {
            info.index in 0..3 -> "stloc.${info.index}"
            info.index < 256 -> "stloc.s ${info.index}"
            else -> "stloc ${info.index}"
        }
        writer.write(opcode)
    }

}

class GetVariableValueExpression(
    private val info: VariableInfo
) : Expression {
    override fun emit(writer: CodeWriter) {
        // see. ecma-335 III.3.43
        val opcode = when {
            info.index in 0..3 -> "ldloc.${info.index}"
            info.index < 256 -> "ldloc.s ${info.index}"
            else -> "ldloc ${info.index}"
        }
        writer.write(opcode)
    }

}
