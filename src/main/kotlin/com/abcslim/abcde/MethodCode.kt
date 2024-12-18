package com.abcslim.abcde

import com.abcslim.global.Config
import com.abcslim.global.Context
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.openAI.LLMServer

fun getMethodCode(methodPath: String): String{
    val _m = Context.getData(methodPath)
    if(_m is MethodCode){
        val method = _m as MethodCode
        return method.llmCode
    }
    return "not found method"
}

fun getAsm(methodPath: String): String{
    val _m = Context.getData(methodPath)
    if(_m is MethodCode){
        val method = _m as MethodCode
        return method.asmCode
    }
    return "not found method"
}

class MethodCode(
    _method: AbcMethod
){
//    var method = _method
    var code = _method.codeItem
    var name = _method.name

//    fun pseudoCode(): String{
//        return code?.pseudoCode ?: ""
//    }

    val llmCode by lazy {
        val llm = LLMServer(Config.AIServer, Config.AIKey, Config.AIModule)
        llm.ai_ode(code?.pseudoCode ?:"")
    }

    val asmCode by lazy{
        val res = StringBuilder()
        code?.asm?.baseBlocks?.forEach{ bbk ->
            res.append("lable_${bbk.offset}\n")
            res.append(bbk)
        }
        res.toString()
    }

}