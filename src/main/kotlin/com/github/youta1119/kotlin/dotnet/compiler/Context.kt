package com.github.youta1119.kotlin.dotnet.compiler

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.ir.DeclarationFactory
import org.jetbrains.kotlin.backend.common.ir.Ir
import org.jetbrains.kotlin.backend.common.ir.SharedVariablesManager
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.descriptors.IrBuiltIns
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.BindingContext

class Context(
    val config: Config
) : CommonBackendContext {
    lateinit var environment: KotlinCoreEnvironment
    lateinit var moduleDescriptor: ModuleDescriptor
    lateinit var bindingContext: BindingContext
    override val builtIns: KotlinBuiltIns by lazy {
        moduleDescriptor.builtIns as DotNetBuiltIns
    }
    override val configuration: CompilerConfiguration
        get() = config.configuration

    override val declarationFactory: DeclarationFactory
        get() = TODO("not implemented")
    override var inVerbosePhase: Boolean = false

    override val internalPackageFqn: FqName
        get() = TODO("not implemented")

    override lateinit var ir: Ir<Context>
    override val irBuiltIns: IrBuiltIns
        get() = ir.irModule.irBuiltins

    override val sharedVariablesManager: SharedVariablesManager
        get() = TODO("not implemented")

    val messageCollector: MessageCollector
        get() = config.configuration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)

    // TODO: make lateinit?
    var irModule: IrModuleFragment? = null
        set(module) {
            if (field != null) {
                throw Error("Another IrModule in the context.")
            }
            field = module!!

            ir = DotNetIr(this, module)
        }

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

