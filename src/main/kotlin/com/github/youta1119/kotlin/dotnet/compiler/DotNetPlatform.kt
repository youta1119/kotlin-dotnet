package com.github.youta1119.kotlin.dotnet.compiler

import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.konan.KonanPlatform
import org.jetbrains.kotlin.platform.konan.KonanPlatforms

@Suppress("DEPRECATION_ERROR")
object DotNetPlatform {

    private object DefaultSimpleDotNetPlatform : KonanPlatform()
    @Deprecated(
        message = "Should be accessed only by compatibility layer, other clients should use 'defaultDotNetPlatform'",
        level = DeprecationLevel.ERROR
    )
    object CompatKonanPlatform : TargetPlatform(setOf(DefaultSimpleDotNetPlatform)),
        org.jetbrains.kotlin.resolve.TargetPlatform {
        override val platformName: String
            get() = "DotNet"
    }

    val defaultDotNetPlatform: TargetPlatform
        get() = KonanPlatforms.CompatKonanPlatform
}