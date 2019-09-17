package com.github.youta1119.kotlin.dotnet.compiler

import java.io.File

class Distribution {
    private val konanHome = findKonanHome()
    val stdlibDir = "$konanHome/stdlib"
    private fun findKonanHome(): String {
        val value = System.getProperty("kotlin.dotnet.home", "dotnet")
        val path = File(value).absolutePath
        return path
    }
}