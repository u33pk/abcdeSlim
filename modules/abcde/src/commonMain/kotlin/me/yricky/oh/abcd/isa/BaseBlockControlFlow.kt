package me.yricky.oh.abcd.isa

import com.google.common.graph.MutableValueGraph
import com.google.common.graph.ValueGraphBuilder
import me.yricky.oh.abcd.isa.Asm.AsmItem
import me.yricky.oh.abcd.isa.util.ExternModuleParser
import me.yricky.oh.abcd.isa.util.V2AInstParser


class BaseBlockControlFlow(
    val asms: List<AsmItem>
){
    companion object{
        val operandParser = listOf(V2AInstParser, ExternModuleParser)
//        val operandParser2 = listOf(V2AInstParser, ExternModuleParser)
    }

    val baseBlockCF : MutableValueGraph<BaseBlock, Boolean> = ValueGraphBuilder
        .directed()
        .allowsSelfLoops(true)
        .build()
    val baseBlockOffAsm by lazy {
        val off2Asm: HashMap<Number, AsmItem> = HashMap()
        asms.forEach {
            off2Asm.put(it.codeOffset, it)
        }
        off2Asm
    }
    val baseBlockPath: HashMap<Number, AsmItem> = HashMap()
    val baseBlocks by lazy {
        generaStep1()
    }
    val revBaseblocks by lazy {
        var rev = HashMap<BaseBlock, Int>()
        baseBlocks.withIndex().forEach { (idx, bbk) ->
            rev.put(bbk, idx)
        }
        rev
    }

    fun findBBKbyAsm(asmItem: AsmItem): BaseBlock?{
        baseBlocks.forEach { bbk ->
            if(asmItem in bbk.li)
                return bbk
        }
        return null
    }
    val basicBlockMap = mutableMapOf<Int, BaseBlock>()
    // 获取正确的基本块列表 还没有连接信息
    fun generaStep1() : ArrayList<BaseBlock>{
        val start_flags = ArrayList<Number>()
        val end_flags = ArrayList<Number>()
        val li = ArrayList<BaseBlock>()
        var next_end_flag = false
        start_flags.add(0)
        end_flags.add(asms.last().codeOffset)
        asms.forEach { _asm ->
            if(next_end_flag) {
                start_flags.add(_asm.codeOffset)
                next_end_flag = false
            }
            if(_asm.bbk_type == "terminal" || _asm.bbk_type == "jump") {
                next_end_flag = true
            }
            if(_asm.bbk_type == "jump"){
                _asm.asmArgs(operandParser).forEach { (index,argString) ->
                    if(argString != null) {
                        start_flags.add(_asm.codeOffset + argString.toInt())
                    }
                }
            }
        }
        next_end_flag = false
        var bbk = BaseBlock()
        var pOff = 0
        asms.forEach { _asm ->
            if(_asm.codeOffset in start_flags){
                if(pOff != 0)
                    end_flags.add(pOff)
            }
            pOff = _asm.codeOffset
        }
        asms.forEach { _asm ->
            if(_asm.codeOffset in start_flags){
                bbk = BaseBlock()
                if(pOff != 0)
                    end_flags.add(pOff)
            }
            bbk.addAsm(_asm)
            if(_asm.codeOffset in end_flags){
                li.add(bbk)
                basicBlockMap.put(bbk.offset,bbk)
            }
        }
        return li
    }

    //生成 base block path
    fun generaStep2() {
        asms.forEach { _asm ->
            if(_asm.bbk_type != "jump"){
                baseBlockOffAsm.get(_asm.nextOffset)?.let { next_asm ->
                    baseBlockPath.put(_asm.codeOffset, next_asm)
                }
            }
            else {
                _asm.asmArgs(operandParser).forEach { (index, argString) ->
                    if (index > 0) {
                        argString?.toInt()?.let {
                            baseBlockOffAsm.get(_asm.codeOffset + it)?.let { next_asm ->
                                baseBlockPath.put(_asm.codeOffset, next_asm)
                            }
                        }
                    }
                }
            }
        }


    }

    /**
     * 生成有向图
     */
    val conditionJmp = arrayOf("jeqz", "jnez")
    val directJmp = arrayOf("jmp")
    val returnAsm = arrayOf("return", "returnundefined")

    fun generaStep3(){
        basicBlockMap.forEach{(_off, bbk) ->
            baseBlockCF.addNode(bbk)
            val terminator = bbk.getTermiinator()
            if(terminator.asmName in conditionJmp){//条件跳转
                val dest = terminator.codeOffset + terminator.opUnits[1].toInt()
                val fall = terminator.nextOffset
                val destNode = basicBlockMap.get(dest)
                val fallNode = basicBlockMap.get(fall)
                baseBlockCF.addNode(destNode)
                baseBlockCF.addNode(fallNode)
                baseBlockCF.putEdgeValue(bbk,destNode,false)
                baseBlockCF.putEdgeValue(bbk,fallNode,true)
            }else if(terminator.asmName in directJmp){//直接跳转
                val dest = terminator.codeOffset + terminator.opUnits[1].toInt()
                val destNode = basicBlockMap.get(dest)
                baseBlockCF.addNode(destNode)
                baseBlockCF.putEdgeValue(bbk,destNode,true)
            }else if(terminator.asmName in returnAsm){
                //返回语句，无fallNode，无须处理
            }else{//其他情况，顺序执行
                val fall = terminator.nextOffset
                //若当前块是最后一个指令，则不添加fallNode
                if(fall < basicBlockMap.entries.last().value.getTermiinator().codeOffset){
                    val fallNode = basicBlockMap.get(fall)
                    baseBlockCF.addNode(fallNode)
                    baseBlockCF.putEdgeValue(bbk,fallNode,true)
                }
            }
        }
    }

    // 生成有向图
//    fun generaStep3() {
//        baseBlocks.forEach {
//            baseBlockCF.addNode(it)
//        }
//        baseBlockPath.forEach { (_off, next_asm) ->
//            baseBlockOffAsm.get(_off)?.let{ cur_asm ->
//                val cur_bbk = findBBKbyAsm(cur_asm)
//                val next_bbk = findBBKbyAsm(next_asm)
//                if(cur_bbk != null && next_bbk != null&& cur_bbk != next_bbk){
//                    if(cur_bbk.name == "Normal") {
//                        val cur_idx =  revBaseblocks.get(cur_bbk)
//                        val near_bbk = cur_idx?.let { baseBlocks.get(it + 1) }
////                            println("${cur_bbk.offset} --- ${near_bbk?.let { it.offset }}")
//                        baseBlockCF.putEdgeValue(cur_bbk, near_bbk, true)
//                        baseBlockCF.putEdgeValue(cur_bbk, next_bbk, true)
//                    }
//                    else if(cur_bbk.name == "Final") {
//
//                    }
//                    else if(cur_bbk.name == "Jmp"){
//                        baseBlockCF.putEdgeValue(cur_bbk, next_bbk, true)
//                    }
//                }
//
//            }
//        }
//
//        baseBlockCF.edges().forEach { edge ->
//            println("${edge.source().offset} -> ${edge.target().offset}")
//        }
//    }
    // 有向图 -> CFG
    fun generaStep4(){

    }

    class BaseBlock {
        val li = ArrayList<AsmItem>()
        var name = "Normal"
        var entry = false
        val offset by lazy {
            if(li.size > 0)
                li[0].codeOffset
            else -1
        }

        fun getTermiinator(): Asm.AsmItem{
            return li.last()
        }

        fun getName1(): String{
            return offset.toString()
        }
        fun addAsm(asm: AsmItem): Boolean {
//            if(asm.codeOffset == 0)
//                name = "Entry"
            if(asm.asmName.startsWith("return"))
                name = "Final"
            else if(asm.asmName == "jmp")
                name = "Jmp"
            li.add(asm)
            return asm.bbk_type == "terminal" || asm.bbk_type == "jump"
        }

        override fun toString(): String {
            val bbksb = StringBuilder()
            this.li.forEach {
                bbksb.append("    ${it.codeOffset}  ${it}\n")
//                println(it.pseudoString)
            }
            return bbksb.toString()
        }
    }
}