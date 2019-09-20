package com.github.youta1119.kotlin.dotnet.compiler

import com.github.youta1119.kotlin.dotnet.compiler.ir.DotNetIr
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.ir.SharedVariablesManager
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

abstract class DotNetBackendContext(val config: Config) : CommonBackendContext {
    abstract override val builtIns: DotNetBuiltIns

    abstract override val ir: DotNetIr

    override val sharedVariablesManager: SharedVariablesManager
        get() = TODO("not implemented")

    val messageCollector: MessageCollector
        get() = config.configuration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)

}