package me.yricky.oh.abcd.isa

import com.google.common.graph.MutableValueGraph
import com.google.common.graph.ValueGraphBuilder
import com.google.gson.JsonObject
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

    val dominatorTree : MutableValueGraph<BaseBlock, Boolean> = ValueGraphBuilder
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
                if (_asm.codeOffset == 0) bbk.isStart = true
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

    fun generaStep4(){
        val nodes = baseBlockCF.nodes()
        //块的支配者集合
        val dominators = mutableMapOf<BaseBlock, MutableSet<BaseBlock>>()

        //除初始块外，将所有块的支配者集合初始化为所有块，在后续迭代过程中收敛
        for (node in nodes) {
            dominators[node] = if (node.isStart) mutableSetOf(node) else nodes.toMutableSet()
        }

        var changed: Boolean
        do {//外循环，当不再有节点的支配者集合被更新时，说明收敛完成
            changed = false
            for (node in nodes) {//内循环，遍历并计算每个节点的支配者集合。理想情况下，单次循环可计算完成每个节点的支配者集合
                if (node.isStart) continue

                //获取当前块的前置节点
                val predecessors =  baseBlockCF.predecessors(node)
                //初始化当前块的支配者集合，必定包含当前节点本身
                val newDominators = mutableSetOf<BaseBlock>(node)
                //获取当前节点前置节点的支配者集合，做交集计算后返回
                if(predecessors.isNotEmpty()){
                    newDominators.addAll(predecessors.map {dominators[it]!!}.reduce{ acc, domSet -> acc.intersect(domSet).toMutableSet() })
                }

                //更新当前节点的支配者集合
                if(newDominators != dominators[node]){
                    dominators[node] = newDominators
                    changed = true
                }
            }
        }while (changed)

        //直接支配者<被支配者, 支配者>
        val immediateDominators = mutableMapOf<BaseBlock, BaseBlock?>()
        //计算直接支配者
        for(node in nodes) {
            if(node.isStart) {
                immediateDominators[node] = null
            }else{
                //排除自身
                val doms = dominators[node]!!.filter { it != node }
                //other为备选节点，当doms中所有节点（除other外）的支配者集合都不包含other时，说明other离node最近，因此other为node的直接支配者
                immediateDominators[node] = doms.find { other -> doms.all { it == other || !dominators[it]!!.contains(other) } }
            }
        }

        //生成支配树
        immediateDominators.keys.forEach {dominatorTree.addNode(it)}
        immediateDominators.forEach { (node, idom) ->
            if(idom != null){
                dominatorTree.putEdgeValue(idom, node, true)
            }
        }

    }



    class BaseBlock {
        val li = ArrayList<AsmItem>()
        var name = "Normal"
        var entry = false
        var isStart = false
        val offset by lazy {
            if(li.size > 0)
                li[0].codeOffset
            else -1
        }

        fun getTermiinator(): Asm.AsmItem{
            return li.last()
        }
        fun toJson():String{
            val asm = this.toString()
            val offset = this.getName1()
            val jsonObject = JsonObject()
            jsonObject.addProperty("offset", offset)
            jsonObject.addProperty("asm", asm)
            return jsonObject.toString()
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
        fun toString(all: Boolean): String{
            if(all)
                return this.toString()
            else{
                val bbksb = StringBuilder()
                this.li.forEach {
                    val _code = it.toString().split("# ")[1]
                    if(_code.indexOf("---") == -1){
                        bbksb.append("    ${_code}\n")
                    }

                }
                return bbksb.toString()
            }
        }
    }
}