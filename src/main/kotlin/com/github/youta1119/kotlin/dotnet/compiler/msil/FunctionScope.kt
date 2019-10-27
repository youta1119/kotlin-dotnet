package com.github.youta1119.kotlin.dotnet.compiler.msil

import com.github.youta1119.kotlin.dotnet.compiler.Context
import com.github.youta1119.kotlin.dotnet.compiler.ir.findMainEntryPoint
import org.jetbrains.kotlin.backend.common.ir.ir2string
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.fqNameForIrSerialization
import org.jetbrains.kotlin.ir.util.getArguments
import org.jetbrains.kotlin.ir.util.isLocal
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import kotlin.properties.Delegates

var IrVariable.index: Int by Delegates.notNull()
private var VariableDescriptor._index: Int by Delegates.notNull()
val VariableDescriptor.index: Int
    get() = _index

class FunctionScope(context: Context, irFunction: IrFunction) {
    private val entryPointFqName =
        context.symbolTable.referenceSimpleFunction(findMainEntryPoint(context)).descriptor.fqNameSafe
    val name = irFunction.fqNameForIrSerialization
    //val visibility = irFunction.visibility
    //val isStatic = irFunction.isStatic
    val isEntryPoint = entryPointFqName == irFunction.fqNameForIrSerialization
    val returnType = irFunction.returnType
    val maxStackSize: Int
    val localVariables = hashMapOf<Name, IrVariable>()
    val body: IrBlockBody
    //val arguments = (irFunction as IrMemberAccessExpression).getArguments()

    init {
        irFunction.descriptor
        val body = irFunction.body as? IrBlockBody
            ?: TODO("unsupported function body type. ${ir2string(irFunction.body)}")
        this.body = body
        body.statements.forEach { stmt ->
            if (stmt is IrVariable) {
                stmt.descriptor._index =  localVariables.size
                stmt.index = localVariables.size
                localVariables[stmt.name] = stmt
            }
        }
        maxStackSize = calcMaxStackSize(body)
    }

    private fun calcMaxStackSize(irFunction: IrBlockBody): Int {
        var maxStackSize = 8 //default
        var currentStackSize = 1
        val visitor = object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitCall(expression: IrCall) {
                super.visitCall(expression)
                val argumentSize = expression.getArguments().count()
                // add stack
                currentStackSize += argumentSize
                if (currentStackSize > maxStackSize) {
                    maxStackSize = currentStackSize
                }
                //add stack return value
                currentStackSize -= argumentSize
                if (!expression.symbol.owner.returnType.isUnit()) {
                    currentStackSize += 1
                    if (currentStackSize > maxStackSize) {
                        maxStackSize = currentStackSize
                    }
                }

            }

            override fun visitVariable(declaration: IrVariable) {
                val initializer = declaration.initializer
                if (initializer is IrCall) {
                    this.visitCall(initializer)
                }

            }

        }
        irFunction.acceptChildrenVoid(visitor)
        return maxStackSize
    }
}
