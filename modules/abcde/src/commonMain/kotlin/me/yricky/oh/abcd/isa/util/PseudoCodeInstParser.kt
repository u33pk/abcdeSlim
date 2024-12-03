package me.yricky.oh.abcd.isa.util

import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.abcd.isa.asmArgs
import me.yricky.oh.abcd.isa.asmName

class PseudoCodeInstParser:InstCommentParser {

    fun splicingArg(asmItem: Asm.AsmItem, action: (Int) -> Boolean) : String{
        var res = StringBuilder()
        asmItem.args.withIndex().forEach {  (idx, arg) ->
            if(action(idx))
                res.append(" ${arg} ")
        }
        return res.toString().replace("  ", ", ")
    }

    fun splicingArgRange(asmItem: Asm.AsmItem, pos: Int): String {
        var res = StringBuilder()

        var args_len = asmItem.args[pos].toInt()
        var startVReg = asmItem.args[pos + 1]
        var vRegIdx = startVReg.split('v')[1].toInt()
        for (i in vRegIdx..(vRegIdx + args_len)) {
            res.append(" v${i} ")
        }
        return res.toString().replace("  ", ", ")
    }

    fun defaultRes(asmItem: Asm.AsmItem): String{
        val res: StringBuilder = StringBuilder()
        res.append("acc = ${asmItem.asmName}(acc")
        asmItem.args.forEach { arg ->
            res.append(", ${arg}")
        }
        res.append(")")
        return res.toString()
    }

    fun defineClassWithBuffer(asmItem: Asm.AsmItem): String{
        var data = asmItem.args[2].split(",")
//        var

        return "${asmItem.args.size}"
    }

    override fun parse(asmItem: Asm.AsmItem): String? {

        when(asmItem.ins.group.title){
            "Dynamic move register-to-register" ->{
                // mov
                return "${asmItem.args[0]} = ${asmItem.args[1]}"
            }
            "jump operations" ->{
                return "GOTO lable_${asmItem.codeOffset + asmItem.args[0].toInt()}"
            }

            "Dynamic load accumulator from register",
            "Dynamic load accumulator from immediate",
            "Load accumulator from string constant pool" -> {
                return "acc = ${asmItem.args[0]}"
            }
            "Dynamic store accumulator" -> {
                return "${asmItem.args[0]} = acc"
            }
            "dynamic return" -> {
                return "return acc"
            }
            "no operation" -> {
                return "nop"
            }
            "call instructions" -> {
                val name = asmItem.asmName
                when {
                    name.contains("callthisrange") -> {
                        // 1111111111
                        return "acc = acc(${splicingArgRange(asmItem, 3)})"
                    }
                    name.contains("callthis") ->
                        return "acc = ${asmItem.args[1]}.acc(${splicingArg(asmItem){ idx -> idx > 1 }})"

                    name.contains("callarg") ->
                        return "acc = acc(${splicingArg(asmItem){ idx -> idx > 2 }})"

                    name.contains("callrange") ->
                        return "acc = acc(${splicingArg(asmItem){ idx -> idx > 3 }})"

                    else -> {
                        return defaultRes(asmItem)
                    }
                }
            }
            "object visitors" -> {
//                val name = asmItem.asmName
                when(asmItem.asmName) {
                    "resumegenerator" -> return "suspend_flag = 0"
                    "getresumemode" -> return "acc = suspend_flag"
                    "gettemplateobject" -> return "--------"
                    "getnextpropname" -> return "--------"
                    "delobjprop" -> return "--------"
                    "suspendgenerator" -> return "${asmItem.args[0]} = suspend_flag = 1"
                    "asyncfunctionawaituncaught" -> return "await"
                    "copydataproperties" -> return "--------"
                    "starrayspread" -> return "--------"
                    "setobjectwithproto" -> return "--------"
                    "ldobjbyvalue" -> return "--------"
                    "stobjbyvalue" -> return "--------"
                    "stownbyvalue" -> return "--------"
                    "ldsuperbyvalue" -> return "--------"
                    "stsuperbyvalue" -> return "--------"
                    "ldobjbyindex" -> return "--------"
                    "stobjbyindex" -> return "--------"
                    "stownbyindex" -> return "acc[${asmItem.args[2]}] = ${asmItem.args[1]}"
                    "asyncfunctionresolve" -> return "--------"
                    "asyncfunctionreject" -> return "--------"
                    "copyrestargs" -> return "--------"
                    "ldlexvar" -> return "--------"
                    "stlexvar" -> return "--------"
                    "getmodulenamespace" -> return "--------"
                    "stmodulevar" -> return "export acc"
                    "trystglobalbyname" -> return "--------"
                    "ldglobal", "tryldglobalbyname" -> return "acc = global[${asmItem.args[1]}]"
                    "stglobalvar" -> return "--------"
                    "ldobjbyname" -> return "acc = acc[${asmItem.args[1]}]"
                    "stobjbyname" -> return "--------"
                    "stownbyname" -> return "--------"
                    "ldsuperbyname" -> return "--------"
                    "stsuperbyname" -> return "--------"
                    "ldlocalmodulevar" -> return "--------"
                    "ldexternalmodulevar" -> return "acc = ${asmItem.args[0]}"
                    "stconsttoglobalrecord" -> return "--------"
                    "sttoglobalrecord" -> return "--------"
                    "stownbyvaluewithnameset" -> return "--------"
                    "stownbynamewithnameset" -> return "--------"
                    "ldbigint" -> return "--------"
                    "ldthisbyname" -> return "--------"
                    "stthisbyname" -> return "--------"
                    "ldthisbyvalue" -> return "--------"
                    "stthisbyvalue" -> return "--------"
                    "dynamicimport" -> return "--------"
                    "asyncgeneratorreject" -> return "--------"
                    "setgeneratorstate" -> return "--------"
                    else -> return defaultRes(asmItem)
                }
            }
            "definition instuctions" -> {
                when(asmItem.asmName) {
                    "definegettersetterbyvalue" -> return "--------"
                    "definefunc" -> return "export function ${asmItem.args[1]}"
                    "definemethod" -> return "--------"
                    "defineclasswithbuffer" -> return defineClassWithBuffer(asmItem)
                    "deprecated.defineclasswithbuffer" -> return asmItem.asmName
                    else -> return defaultRes(asmItem)
                }
            }
            "object creaters" -> {
                when (asmItem.asmName) {
                    "createemptyobject" -> return "acc = new acc()"
                    "createemptyarray" -> return "acc = []"
                    "creategeneratorobj" -> return "--------"
                    "createiterresultobj" -> return "--------"
                    "createobjectwithexcludedkeys" -> return "--------"
                    "createarraywithbuffer" -> return "--------"
                    "createobjectwithbuffer" -> return "--------"
                    "createregexpwithliteral" -> return "--------"
                    "newobjapply" -> return "--------"
                    "newobjrange" -> return "-------- acc = ${asmItem.args[2]}"
                    "newlexenv" -> return "--------"
                    "newlexenvwithname" -> return "--------"
                    "createasyncgeneratorobj" -> return "--------"
                    "asyncgeneratorresolve" -> return "--------"
                    else -> return defaultRes(asmItem)
                }
            }
            "binary operations" -> {
                when (asmItem.asmName) {
                    "add2" -> return "acc = acc + ${asmItem.args[1]}"
                    "sub2" -> return "acc = acc - ${asmItem.args[1]}"
                    "mul2" -> return "acc = acc * ${asmItem.args[1]}"
                    "div2" -> return "acc = acc / ${asmItem.args[1]}"
                    "mod2" -> return "acc = acc % ${asmItem.args[1]}"
                    "eq" -> return "acc = acc == ${asmItem.args[1]}"
                    "noteq" -> return "acc = acc != ${asmItem.args[1]}"
                    "less" -> return "acc = acc < ${asmItem.args[1]}"
                    "lesseq" -> return "acc = acc <= ${asmItem.args[1]}"
                    "greater" -> return "acc = acc > ${asmItem.args[1]}"
                    "greatereq" -> return "acc = acc >= ${asmItem.args[1]}"
                    "shl2" -> return "acc = acc << ${asmItem.args[1]}"
                    "shr2" -> return "acc = acc >> ${asmItem.args[1]}"
                    "ashr2" -> return "acc ??= acc >> ${asmItem.args[1]}"
                    "and2" -> return "acc = acc & ${asmItem.args[1]}"
                    "or2" -> return "acc = acc | ${asmItem.args[1]}"
                    "xor2" -> return "acc = acc ^ ${asmItem.args[1]}"
                    "exp" -> return "acc = exp(${asmItem.args[1]})"
                    else -> return defaultRes(asmItem)
                }
            }
            "constant object loaders" -> {
                when (asmItem.asmName) {
                    "ldnan" -> return "acc = NAN"
                    "ldinfinity" -> return "acc = INFINITY"
                    "ldundefined" -> return "acc = UNDEFINED"
                    "ldnull" -> return "acc = NULL"
                    "ldsymbol" -> return "acc ??= SYMBOL"
                    "ldglobal" -> return "acc ??= GLOBAL"
                    "ldtrue" -> return "acc = true"
                    "ldfalse" -> return "acc = false"
                    "ldhole" -> return "acc ??= HOLE"
                    "deprecated.ldlexenv" -> return "acc ??= ENV"
                    "ldnewtarget" -> return "acc = new target"
                    "ldthis" -> return "acc = this"
                    "poplexenv" -> return "acc ??= ENV"
                    "deprecated.poplexenv" -> return "acc ??= ENV"
                    "getunmappedargs" -> return "acc = getunmappedargs()"
                    "asyncfunctionenter" -> return "?? async function"
                    "ldfunction" -> return "acc ??= function"
                    "debugger" -> return "?? debug"
                    else -> return asmItem.asmName
                }
            }
            "iterator instructions" -> {
                when(asmItem.asmName) {
                    "getpropiterator" -> return "--------"
                    "getiterator" -> return "--------"
                    "closeiterator" -> return "--------"
                    "getasynciterator" -> return "--------"
                    "ldprivateproperty" -> return "--------"
                    "stprivateproperty" -> return "--------"
                    "testin" -> return "--------"
                    "definefieldbyname" -> return "var ${asmItem.args[1].replace('"', ' ')} = acc"
                    else -> return defaultRes(asmItem)
                }
            }
            "comparation instructions" -> {
                when(asmItem.asmName) {
                    "isin" -> return "--------"
                    "instanceof" -> return "--------"
                    "strictnoteq" -> return "acc = ${asmItem.args[0]} !== ${asmItem.args[1]}"
                    "stricteq" -> return "acc = ${asmItem.args[0]} === ${asmItem.args[1]}"
                    else -> return defaultRes(asmItem)
                }
            }
            else -> {
                // acc = ecma_op(acc, operand_0, ..., operands_n)
                return defaultRes(asmItem)
            }
        }

    }

}