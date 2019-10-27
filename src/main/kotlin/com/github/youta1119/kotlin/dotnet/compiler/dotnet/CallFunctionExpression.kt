package com.github.youta1119.kotlin.dotnet.compiler.dotnet

import org.jetbrains.kotlin.name.FqName

class CallFunctionExpression(
    private val name: FqName,
    private val returnType: DotNetPrimitiveType,
    private val argument: List<Expression>
) : Expression {
    override fun emit(writer: CodeWriter) {
        argument.forEach {
            it.emit(writer)
        }
        if (name.asString() == "kotlin.io.println") {
            writer.write("call void [mscorlib]System.Console::WriteLine(string)")
        } else {

            writer.write("call $returnType $name()")
        }
    }
}
