package com.github.youta1119.kotlin.dotnet.compiler.dotnet

interface Expression {
    fun emit(writer: CodeWriter)
}
