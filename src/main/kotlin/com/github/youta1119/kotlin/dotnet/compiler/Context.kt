package com.github.youta1119.kotlin.dotnet.compiler

import com.github.youta1119.kotlin.dotnet.compiler.ir.DotNetIr
import org.jetbrains.kotlin.backend.common.ir.DeclarationFactory
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.descriptors.IrBuiltIns
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.BindingContext

class Context(config: Config) : DotNetBackendContext(config) {
    lateinit var environment: KotlinCoreEnvironment
    lateinit var moduleDescriptor: ModuleDescriptor
    lateinit var bindingContext: BindingContext
    override val builtIns: DotNetBuiltIns by lazy {
        moduleDescriptor.builtIns as DotNetBuiltIns
    }
    override val configuration: CompilerConfiguration
        get() = config.configuration

    override val declarationFactory: DeclarationFactory
        get() = TODO("not implemented")
    override var inVerbosePhase: Boolean = false

    override val internalPackageFqn: FqName
        get() = TODO("not implemented")

    override lateinit var ir: DotNetIr

    override val irBuiltIns: IrBuiltIns
        get() = ir.irModule.irBuiltins

    lateinit var irModule: IrModuleFragment
    lateinit var symbolTable: SymbolTable
    val phaseConfig = config.phaseConfig

    override fun log(message: () -> String) {
        if (inVerbosePhase) {
            println(message())
        }
    }

    override fun report(element: IrElement?, irFile: IrFile?, message: String, isError: Boolean) {
        this.messageCollector.report(
            if (isError) CompilerMessageSeverity.ERROR else CompilerMessageSeverity.WARNING,
            message, null
        )
    }
}

