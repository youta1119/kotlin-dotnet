package com.github.youta1119.kotlin.dotnet.compiler.dotnet

sealed class TypeOperatorCallExpression : Expression {
    class ImplicitVoidCastExpression(
        private val expression: Expression
    ) : TypeOperatorCallExpression() {
        override fun emit(writer: CodeWriter) {
            expression.emit(writer)
            writer.write("pop")
        }
    }
}
