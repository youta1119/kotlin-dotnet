package com.github.youta1119.kotlin.dotnet.compiler.dotnet

import com.github.youta1119.kotlin.dotnet.compiler.Context
import com.github.youta1119.kotlin.dotnet.compiler.ir.findMainEntryPoint
import org.jetbrains.kotlin.backend.common.ir.ir2string
import org.jetbrains.kotlin.backend.common.serialization.target
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.js.utils.asString
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.isUnit
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
        val functionName = declaration.descriptor.name
        val isEntryPoint = declaration.descriptor.fqNameSafe == entryPointFqName
        val returnType = declaration.returnType.toDotnetType()
        val body = irBody.statements.mapNotNull { statement ->
            when (statement) {
                is IrVariable -> null
                is IrExpression -> evaluateExpression(statement)
                else -> TODO(ir2string(statement))
            }
        }
        FunctionExpression(
            name = functionName,
            body = body,
            isEntryPoint = isEntryPoint,
            returnType = returnType
        ).emit(writer = codeWriter)
    }

    private fun evaluateExpression(expression: IrExpression): Expression {
        return when (expression) {
            is IrCall -> evaluateCall(expression)
            is IrConst<*> -> evaluateConst(expression)
            is IrReturn -> evaluateReturn(expression)
            is IrTypeOperatorCall -> evaluateTypeOperatorCall(expression)
            else -> TODO(ir2string(expression))
        }
    }

    private fun evaluateConst(value: IrConst<*>): Expression {
        val kind = value.kind
        return when (kind) {
            is IrConstKind.String -> ConstValueExpression.StringValueExpression(kind.valueOf(value))
            is IrConstKind.Int -> ConstValueExpression.Int32ValueExpression(kind.valueOf(value))
            else -> TODO("unsupported ir type :${ir2string(value)}")
        }
    }


    private fun evaluateCall(value: IrCall): Expression {
        val calleeFunction = value.symbol.owner.target
        val fqName = calleeFunction.fqNameForIrSerialization
        val returnType = calleeFunction.returnType.toDotnetType()
        val args = value.getArguments().map { (_, expr) -> evaluateExpression(expr) }
        return CallFunctionExpression(
            name = fqName, argument = args, returnType = returnType
        )
    }

    private fun evaluateReturn(declaration: IrReturn): Expression {
        return evaluateExpression(declaration.value)
    }

    private fun evaluateTypeOperatorCall(value: IrTypeOperatorCall): Expression {
        return when (value.operator) {
            IrTypeOperator.IMPLICIT_COERCION_TO_UNIT ->
                TypeOperatorCallExpression.ImplicitVoidCastExpression(evaluateExpression(value.argument))
            else -> TODO("unsupported type operation: ${ir2string(value)}")
        }
    }

    override fun close() {
        codeWriter.close()
    }

    private fun IrType.toDotnetType(): DotNetPrimitiveType {
        return when {
            isString() -> DotNetPrimitiveType.STRING
            isUnit() -> DotNetPrimitiveType.VOID
            else -> TODO("unsupported ir type: ${this.asString()}")
        }
    }
}
