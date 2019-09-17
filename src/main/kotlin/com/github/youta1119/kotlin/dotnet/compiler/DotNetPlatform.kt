package com.github.youta1119.kotlin.dotnet.compiler

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.konan.KonanPlatform
import org.jetbrains.kotlin.platform.konan.KonanPlatforms
import org.jetbrains.kotlin.storage.StorageManager


val STDLIB_MODULE_NAME = Name.special("<stdlib>")

fun ModuleDescriptor.isStdlib(): Boolean {
    return name == STDLIB_MODULE_NAME
}

class DotNetBuiltIns(storageManager: StorageManager) : KotlinBuiltIns(storageManager)

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