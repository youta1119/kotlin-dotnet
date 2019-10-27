package com.github.youta1119.kotlin.dotnet.compiler.dotnet


enum class DotNetPrimitiveType(private val keyword: String) {
    STRING("string"),
    VOID("void");

    override fun toString(): String {
        return this.keyword;
    }
}
