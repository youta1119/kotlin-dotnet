package com.github.youta1119.kotlin.dotnet.compiler.ir

import com.github.youta1119.kotlin.dotnet.compiler.Context
import org.jetbrains.kotlin.backend.common.ir.Ir
import org.jetbrains.kotlin.backend.common.ir.Symbols
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
import org.jetbrains.kotlin.ir.util.SymbolTable
import kotlin.properties.Delegates

class DotNetIr(context: Context, irModule: IrModuleFragment) : Ir<Context>(context, irModule) {
    override var symbols: DotNetSymbols by Delegates.notNull()

}

class DotNetSymbols(
    context: Context,
    symbolTable: SymbolTable,
    lazySymbolTable: ReferenceSymbolTable
) : Symbols<Context>(context, lazySymbolTable) {

    override val externalSymbolTable
        get() = TODO("not implemented")
    val entryPoint = findMainEntryPoint(context).let { symbolTable.referenceSimpleFunction(it)}

    override val ThrowNoWhenBranchMatchedException: IrFunctionSymbol
        get() = TODO("not implemented")
    override val ThrowNullPointerException: IrFunctionSymbol
        get() = TODO("not implemented")
    override val ThrowTypeCastException: IrFunctionSymbol
        get() = TODO("not implemented")
    override val ThrowUninitializedPropertyAccessException: IrSimpleFunctionSymbol
        get() = TODO("not implemented")
    override val copyRangeTo: Map<ClassDescriptor, IrSimpleFunctionSymbol>
        get() = TODO("not implemented")
    override val coroutineContextGetter: IrSimpleFunctionSymbol
        get() = TODO("not implemented")
    override val coroutineGetContext: IrSimpleFunctionSymbol
        get() = TODO("not implemented")
    override val coroutineImpl: IrClassSymbol
        get() = TODO("not implemented")
    override val coroutineSuspendedGetter: IrSimpleFunctionSymbol
        get() = TODO("not implemented")
    override val defaultConstructorMarker: IrClassSymbol
        get() = TODO("not implemented")
    override val getContinuation: IrSimpleFunctionSymbol
        get() = TODO("not implemented")
    override val returnIfSuspended: IrSimpleFunctionSymbol
        get() = TODO("not implemented")
    override val stringBuilder: IrClassSymbol
        get() = TODO("not implemented")
    override val suspendCoroutineUninterceptedOrReturn: IrSimpleFunctionSymbol
        get() = TODO("not implemented")


}