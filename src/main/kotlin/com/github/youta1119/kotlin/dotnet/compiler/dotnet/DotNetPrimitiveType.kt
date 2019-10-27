package com.github.youta1119.kotlin.dotnet.compiler.dotnet

sealed class DotNetPrimitiveType {
    abstract val name: kotlin.String

    object String : DotNetPrimitiveType() {
        override val name: kotlin.String = "string"
    }

    object Int32 : DotNetPrimitiveType() {
        override val name: kotlin.String = "int32"
    }

    object Void : DotNetPrimitiveType() {
        override val name: kotlin.String = "void"

    }
}
