package com.github.youta1119.kotlin.dotnet.cli

import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion


class K2DotNetMetadataVersion(vararg numbers: Int) : BinaryVersion(*numbers) {

    override fun isCompatible(): Boolean = false

    companion object {
        @JvmField
        val INSTANCE = K2DotNetMetadataVersion(0,0,1)
    }
}
