package com.github.youta1119.kotlin.dotnet.compiler.ir

import com.github.youta1119.kotlin.dotnet.compiler.Context
import com.github.youta1119.kotlin.dotnet.compiler.reportCompilationError
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isUnit

private const val ENTRY_POINT_NAME = "main"
internal fun findMainEntryPoint(context: Context): FunctionDescriptor {
    val entryPoint = FqName(ENTRY_POINT_NAME)

    val entryName = entryPoint.shortName()
    val packageName = entryPoint.parent()

    val packageScope = context.builtIns.builtInsModule.getPackage(packageName).memberScope

    val candidates = packageScope.getContributedFunctions(
        entryName,
        NoLookupLocation.FROM_BACKEND
    ).filter {
        it.returnType?.isUnit() == true &&
                it.typeParameters.isEmpty() &&
                it.visibility.isPublicAPI
    }

    val main =
        candidates.singleOrNull { it.hasSingleArrayOfStringParameter }
            ?: candidates.singleOrNull { it.hasNoParameters }
            ?: context.reportCompilationError("Could not find '$entryName' in '$packageName' package.")

    if (main.isSuspend) {
        context.reportCompilationError("Entry point can not be a suspend function.")
    }

    return main
}


private val arrayTypes = setOf(
    "kotlin.Array",
    "kotlin.ByteArray",
    "kotlin.CharArray",
    "kotlin.ShortArray",
    "kotlin.IntArray",
    "kotlin.LongArray",
    "kotlin.FloatArray",
    "kotlin.DoubleArray",
    "kotlin.BooleanArray",
    "kotlin.native.ImmutableBlob",
    "kotlin.native.internal.NativePtrArray"
)

private val KotlinType.filterClass: ClassDescriptor?
    get() {
        val constr = constructor.declarationDescriptor
        return constr as? ClassDescriptor
    }

private val ClassDescriptor.isString
    get() = fqNameSafe.asString() == "kotlin.String"

private val KotlinType.isString
    get() = filterClass?.isString ?: false

private val ClassDescriptor.isArray: Boolean
    get() = this.fqNameSafe.asString() in arrayTypes

private val KotlinType.isArrayOfString: Boolean
    get() = (filterClass?.isArray ?: false) &&
            (arguments.singleOrNull()?.type?.isString ?: false)

private val FunctionDescriptor.hasSingleArrayOfStringParameter: Boolean
    get() = valueParameters.singleOrNull()?.type?.isArrayOfString ?: false

private val FunctionDescriptor.hasNoParameters: Boolean
    get() = valueParameters.isEmpty()
