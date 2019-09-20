package com.github.youta1119.kotlin.dotnet.compiler

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile

internal fun CommonBackendContext.reportCompilationError(message: String, irFile: IrFile, irElement: IrElement): Nothing {
    report(irElement, irFile, message, true)
    throw DotNetCompilationException()
}

internal fun CommonBackendContext.reportCompilationError(message: String): Nothing {
    report(null, null, message, true)
    throw DotNetCompilationException()
}

internal fun CommonBackendContext.reportCompilationWarning(message: String) {
    report(null, null, message, false)
}
