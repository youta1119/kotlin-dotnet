package com.github.youta1119.kotlin.dotnet.compiler.dotnet

sealed class ConstValueExpression : Expression
class StringValueExpression(private val value: String) : ConstValueExpression() {
    override fun emit(writer: CodeWriter) {
        writer.write("ldstr \"$value\"")
    }
}

class Int32ValueExpression(private val value: Int) : ConstValueExpression() {
    override fun emit(writer: CodeWriter) {
        writer.write("ldc.i4 $value")
    }

}
