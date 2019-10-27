package com.github.youta1119.kotlin.dotnet.compiler.dotnet

import com.github.youta1119.kotlin.dotnet.compiler.Context
import com.github.youta1119.kotlin.dotnet.compiler.ir.findMainEntryPoint
import org.jetbrains.kotlin.backend.common.ir.ir2string
import org.jetbrains.kotlin.backend.common.serialization.target
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.util.fqNameForIrSerialization
import org.jetbrains.kotlin.ir.util.getArguments
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import java.io.Closeable

class CodeGeneratorVisitor(context: Context) : IrElementVisitorVoid, Closeable {
    private val codeWriter = CodeWriter(context.config.ilAsmFile.printWriter())
    private val entryPointFqName =
        context.symbolTable.referenceSimpleFunction(findMainEntryPoint(context)).descriptor.fqNameSafe


    init {
        codeWriter.write(".assembly extern mscorlib {}")
        codeWriter.write(".assembly ${context.config.moduleId} {}")
    }

    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitFunction(declaration: IrFunction) {
        val irBody = declaration.body as? IrBlockBody
        if (irBody == null || declaration.isExternal) {
            return
        }
        val body = irBody.statements.mapNotNull { statement ->
            when (statement) {
                is IrVariable -> null
                is IrExpression -> evaluateExpression(statement)
                else -> TODO(ir2string(statement))
            }
        }
        val functionName = declaration.descriptor.name.asString()
        codeWriter.write(".method static void $functionName() cil managed {")
        codeWriter.indent()
        if (declaration.descriptor.fqNameSafe == entryPointFqName) {
            codeWriter.write(".entrypoint")
        }
        body.forEach {
            it.emit(codeWriter)
        }
        codeWriter.write("ret")
        codeWriter.unindent()
        codeWriter.write("}")

        //FunctionExpression().emit(writer = codeWriter)
    }

    private fun evaluateExpression(expression: IrExpression): Expression {
        return when (expression) {
            is IrCall -> evaluateCall(expression)
            is IrConst<*> -> evaluateConst(expression)
            else -> TODO(ir2string(expression))
        }
    }

    private fun evaluateConst(const: IrConst<*>): Expression {
        val kind = const.kind
        if (kind is IrConstKind.String) {
            return ConstValueExpression.StringValueExpression(kind.valueOf(const))
        }
        TODO("unsupported ir type :${ir2string(const)}")
    }


    private fun evaluateCall(call: IrCall): Expression {
        val fqName = call.symbol.owner.target.fqNameForIrSerialization
        val args = call.getArguments().map { (_, expr) -> evaluateExpression(expr) }
        return CallFunctionExpression(fqName, args)
    }


    override fun close() {
        codeWriter.close()
    }

}
