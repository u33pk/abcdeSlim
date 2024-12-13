package me.yricky.oh.abcd.isa

import com.google.common.graph.MutableValueGraph
import com.google.common.graph.ValueGraphBuilder
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.code.Code
import me.yricky.oh.abcd.code.TryBlock
import me.yricky.oh.abcd.isa.Inst.Companion.toUnsignedInt
import me.yricky.oh.abcd.isa.util.*
import me.yricky.oh.abcd.literal.LiteralArray
import me.yricky.oh.common.value
import me.yricky.oh.abcd.isa.BaseBlockControlFlow.BaseBlock
//import me.yricky.oh.abcd.isa.util.PseudoCodeInstParser.Companion

expect fun loadInnerAsmMap():AsmMap

/**
 * 用于解析方法中代码段的类
 */
class Asm(
    val code: Code,
    val asmMap:AsmMap = innerAsmMap
) {
    companion object{
        val innerAsmMap by lazy { loadInnerAsmMap() }
        val operandParser = listOf(V2AInstParser, ExternModuleParser)
        val pseudoParser = PseudoCodeInstParser()
    }
    val list:List<AsmItem> by lazy{
        val li = ArrayList<AsmItem>()
        var off = 0
        while (off < code.codeSize){
            val initOff = off
            val opCode = code.instructions.get(off)
            off += 1
            asmMap.prefixInstMap[opCode]?.let {
                val subOpCode = code.instructions.get(off)
                off += 1
                val inst = it[subOpCode] ?: throw IllegalStateException("No this subOpCode:${subOpCode.toString(16)} in opCode:${opCode.toString(16)}")
                off += inst.argSize()
                li.add(AsmItem(this,inst,initOff,li.size))
            } ?: asmMap.insMap[opCode]?.let { ins ->
                off += ins.argSize()
                li.add(AsmItem(this,ins, initOff,li.size))
            } ?: throw IllegalStateException("No this opCode:${opCode.toString(16)},off:${off - 1}")
        }
        li
    }
    val baseBlocks = BaseBlockControlFlow(this.list).baseBlocks

    val dominatorTree: MutableValueGraph<BaseBlock, Boolean> by lazy {
        val cf = BaseBlockControlFlow(this.list)
        cf.generaStep1()
        cf.generaStep2()
        cf.generaStep3()
        cf.generaStep4()
        cf.dominatorTree

    }

    val baseBlockCF:MutableValueGraph<BaseBlock, Boolean> by lazy {
        val cf = BaseBlockControlFlow(this.list)
        cf.generaStep1()
        cf.generaStep2()
        cf.generaStep3()
        cf.generaStep4()
        cf.baseBlockCF

    }


    /**
     * 字节码中单个指令的汇编对象
     * @param asm 指令所在方法
     * @param codeOffset 本条指令在方法指令段中的相对offset，从0开始
     * @param ins 本条指令的指令格式
     */
    class AsmItem(
        val asm:Asm,
        val ins:Inst,
        val codeOffset:Int,
        val index:Int
    ){
        val tryBlocks:List<TryBlock> get() = asm.code.tryBlocks.filter {
            codeOffset in (it.startPc until (it.startPc+ it.length))
        }

        val next:AsmItem? get() = asm.list.getOrNull(index + 1)
        val nextOffset:Int get() = next?.codeOffset ?: asm.code.codeSize

        val bbk_type:String get() = ins.instruction.bbk_type
        val args by lazy {
            val args = ArrayList<String>()
            asmArgs(operandParser).forEach { (idx, argString) ->
                if(argString != null)
                    args.add(argString)
            }
            args
        }
        val pseudoString : String? get() = pseudoParser.parse(this)

        /**
         * 将指令的原始二进制数据拆分为一个个语义化单元，并以List<Number>表示
         *
         * 其格式为 \[prefix] opcode operand1 operand2 ...
         */
        val opUnits:List<Number> by lazy {
            val instructions = asm.code.instructions
            val opUnit = mutableListOf<Number>()
            val fmtIterator = ins.format.iterator()
            var off = codeOffset
            while (fmtIterator.hasNext()){
                val thisFmt = fmtIterator.next()
                if(thisFmt is InstFmt.Prefix || thisFmt is InstFmt.OpCode){
                    opUnit.add(instructions.get(off))
                    off += 1
                } else if(thisFmt.bitSize == 4){
                    val nextFmt = fmtIterator.next()
                    if(nextFmt.bitSize != 4){
                        throw IllegalStateException()
                    }
                    val value = instructions.get(off).toUByte().toInt()
                    opUnit.add((value and  0xf).toByte())
                    opUnit.add(value.shr(4).toByte())
                    off += 1
                } else if(thisFmt.bitSize == 8){
                    val value = instructions.get(off)
                    opUnit.add(value)
                    off += 1
                } else if(thisFmt.bitSize == 16){
                    val value = instructions.getShort(off)
                    opUnit.add(value)
                    off += 2
                } else if(thisFmt.bitSize == 32){
                    val value = instructions.getInt(off)
                    opUnit.add(value)
                    off += 4
                } else if(thisFmt.bitSize == 64){
                    val value = instructions.getLong(off)
                    opUnit.add(value)
                    off += 8
                } else throw IllegalStateException("Unsupported bitSize")
            }
            opUnit
        }

        override fun toString(): String {
            var res : StringBuilder = StringBuilder()
            res.append("${asmName} ")
            this.asmArgs(operandParser).forEach { (idx, args) ->
                if(args != null){
                    res.append("${args} ")
                }
            }
            res.append(" # ${pseudoString}")
//            println(res.toString())
            return res.toString()
        }
    }
}

val Asm.AsmItem.asmName:String get() = ins.asmName
val Asm.AsmItem.asmComment:String get() = InstCommentParser.commentString(this)

fun Asm.AsmItem.asmArgs(parser: List<InstDisAsmParser>):Sequence<Pair<Int,String?>> = sequence {
    opUnits.indices.forEach { index ->
        var argString:String? = null
        val pIterator = parser.listIterator()
        while (pIterator.hasNext() && argString == null){
            argString = pIterator.next().parseArg(this@asmArgs,index)
        }
        if(argString == null){
            argString = BaseInstParser.parseArg(this@asmArgs,index)
        }
        yield(Pair(index,argString))
    }
}

val Asm.AsmItem.calledMethods:Sequence<AbcMethod> get() = sequence {
    ins.format.forEach { instFmt ->
        if(instFmt is InstFmt.MId){
            val method = instFmt.getMethod(this@calledMethods)
            if(method is AbcMethod){
                yield(method)
            }
        } else if(instFmt is InstFmt.LId){
            val literalArray = instFmt.getLA(this@calledMethods)
            literalArray.content.forEach {
                if(it is LiteralArray.Literal.LiteralMethod){
                    val method = it.get(asm.code.method.abc)
                    if(method is AbcMethod){
                        yield(method)
                    }
                }
            }
        }
    }
}

val Asm.AsmItem.calledStrings:Sequence<String> get() = sequence {
    ins.format.forEachIndexed { index, instFmt ->
        if(instFmt is InstFmt.SId){
            val value = opUnits[index].toUnsignedInt()
            val str = asm.code.abc.stringItem(asm.code.method.region.mslIndex[value])
            yield(str.value)
        } else if(instFmt is InstFmt.LId){
            val value = opUnits[index].toUnsignedInt()
            val literalArray = asm.code.abc.literalArray(asm.code.method.region.mslIndex[value])
            literalArray.content.forEach {
                if(it is LiteralArray.Literal.Str){
                    yield(it.get(asm.code.abc))
                } else if(it is LiteralArray.Literal.ArrayStr){
                    yieldAll(it.get(asm.code.abc))
                }
            }
        }
    }
}


val Asm.AsmItem.calledResources:Sequence<Int> get() = sequence {
    ins.format.forEachIndexed { index, instFmt ->
        if(instFmt is InstFmt.LId){
            val value = opUnits[index].toUnsignedInt()
            val literalArray = asm.code.abc.literalArray(asm.code.method.region.mslIndex[value])
            if(literalArray.content.size % 2 == 0){
                val objKv = mutableMapOf<String,LiteralArray.Literal>()
                val iter = literalArray.content.iterator()
                while (iter.hasNext()){
                    val next = iter.next()
                    if(next is LiteralArray.Literal.Str){
                        objKv[next.get(asm.code.abc)] = iter.next()
                    } else break
                }
                if(
                    objKv["id"] is LiteralArray.Literal.I32 &&
                    objKv["type"] is LiteralArray.Literal.I32 &&
                    objKv["params"] != null &&
                    objKv["bundleName"] is LiteralArray.Literal.Str &&
                    objKv["moduleName"] is LiteralArray.Literal.Str
                ){
                    yield((objKv["id"] as LiteralArray.Literal.I32).value)
                }
            }
        }
    }
}