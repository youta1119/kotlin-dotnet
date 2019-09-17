package com.github.youta1119.kotlin.dotnet.compiler

import org.jetbrains.kotlin.backend.common.ir.Ir
import org.jetbrains.kotlin.backend.common.ir.Symbols
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import java.beans.PropertyDescriptor
import kotlin.properties.Delegates

internal class DotNetIr(context: Context, irModule: IrModuleFragment): Ir<Context>(context, irModule) {

    //TODO not implement
    override var symbols: Symbols<Context> by Delegates.notNull()

}