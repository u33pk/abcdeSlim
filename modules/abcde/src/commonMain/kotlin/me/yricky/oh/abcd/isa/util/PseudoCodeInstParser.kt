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

    fun splicingArgRange(regs: Int, regs_len: Int): String {
        val res = StringBuilder()

        for(i in regs..<regs + regs_len){
            res.append(" v$i ")
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

    fun withBuffer(data: String, action: (String?, String?) -> String): String{
        val regex = Regex("(\\w+):([^,]+)")
        val matchResult = regex.findAll(data)
        var res = StringBuilder()
        matchResult.forEach { match ->
            val key = match.groups[1]?.value
            val value = match.groups[2]?.value
            res.append(" ${action(key, value)} ")
        }
        return res.toString().replace("  ", ", ")
    }

    override fun parse(asmItem: Asm.AsmItem): String? {

        when(asmItem.ins.group.title){
            "Dynamic move register-to-register" ->{
                // mov
                return "${asmItem.args[0]} = ${asmItem.args[1]}"
            }
            "jump operations" ->{
                var conditions = when(asmItem.asmName) {
                    "jmp" ->  ""
                    "jeqz" ->  "if (acc == 0)"
                    "jnez" ->  "if (acc != 0)"
                    "jstricteqz" ->  "if (acc === 0)"
                    "jnstricteqz" ->  "if (acc !== 0)"
                    "jeqnull" ->  "if (acc == null)"
                    "jnenull" ->  "if (acc != null)"
                    "jstricteqnull" ->  "if (acc === 0)"
                    "jnstricteqnull" ->  "if (acc !== 0)"
                    "jequndefined" ->  "if (acc == undefined)"
                    "jneundefined" ->  "if (acc != undefined)"
                    "jstrictequndefined" ->  "if (acc === undefined)"
                    "jnstrictequndefined" ->  "if (acc !== undefined)"
                    "jeq" ->  "if (acc == ${asmItem.args[0]})"
                    "jne" ->  "if (acc != ${asmItem.args[0]})"
                    "jstricteq" ->  "if (acc === ${asmItem.args[0]})"
                    "jnstricteq" ->  "if (acc == ${asmItem.args[0]})"
                    else -> asmItem.asmName
                }
                return "${conditions} GOTO lable_${asmItem.codeOffset + asmItem.args[0].toInt()}"
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

                when(asmItem.asmName) {
                    "callarg0", "callarg1", "callarg2", "callarg3" ->
                        return "acc = acc(${splicingArg(asmItem){ idx -> idx >= 1 }})"
                    "callrange" -> {
                        val arg_len = asmItem.args[1].toInt()
                        val s_reg_idx = asmItem.args[2].split("v")[1].toInt()
                        return "acc = acc(${splicingArgRange(s_reg_idx, arg_len)})"
                    }
                    "supercallspread" -> return "acc = acc.super(${asmItem.args[1]})"
                    "apply" -> return "acc = ${asmItem.args[1]}.acc(${asmItem.args[2]})"
                    "callthis0", "callthis1", "callthis2", "callthis3" ->
                        return "acc = ${asmItem.args[1]}.acc(${splicingArg(asmItem){ idx -> idx > 1 }})"
                    "callthisrange" -> {
                        val arg_len = asmItem.args[1].toInt()
                        val s_reg_idx = asmItem.args[2].split("v")[1].toInt()
                        return "acc = ${asmItem.args[2]}(${splicingArgRange(s_reg_idx+1, arg_len)})"
                    }
                    "supercallthisrange", "supercallarrowrange" -> {
                        val arg_len = asmItem.args[1].toInt()
                        val s_reg_idx = asmItem.args[2].split("v")[1].toInt()
                        return "acc = super(${splicingArgRange(s_reg_idx, arg_len)})"
                    }
//                    "supercallarrowrange" -> {
//                        val arg_len = asmItem.args[1].toInt()
//                        val s_reg_idx = asmItem.args[2].split("v")[1].toInt()
//                        return "------------"
//                    }
                    else -> return defaultRes(asmItem)
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
                    "starrayspread" -> return "${asmItem.args[0]} = [...${asmItem.args[0]}, ...acc]"
                    "setobjectwithproto" -> return "--------"
                    "ldobjbyvalue" -> return "acc = ${asmItem.args[1]}[acc]"
//                    "stobjbyvalue" -> return "${asmItem.args[1]}[${asmItem.args[2]}] = acc"
//                    "stownbyvalue" -> return "--------"
                    "ldsuperbyvalue" -> return "acc = ${asmItem.args[1]}.super[acc]"
                    "stsuperbyvalue" -> return "${asmItem.args[1]}.super[acc] = acc"
//                    "ldobjbyindex" -> return "--------"
                    "stobjbyindex", "stobjbyvalue", "stobjbyname", "stownbyvalue","stownbyname","stownbyindex" ->
                        return "${asmItem.args[1]}[${asmItem.args[2]}] = acc"
//                    "stownbyvalue","stownbyname","stownbyindex" -> return "${asmItem.args[1]}[${asmItem.args[2]}] = acc"
                    "asyncfunctionresolve" -> return "--------"
                    "asyncfunctionreject" -> return "--------"
                    "copyrestargs" -> return "acc = arg${asmItem.args[0]}"
                    "ldlexvar" -> return "--------"
                    "stlexvar" -> return "--------"
                    "getmodulenamespace" -> return "--------"
                    "stmodulevar" -> return "export acc"
                    "trystglobalbyname" -> return "--------"
                    "ldglobalvar", "tryldglobalbyname" -> return "acc = GLOBAL[${asmItem.args[1]}]"
                    "stglobalvar", "sttoglobalrecord" -> return "GLOBAL[${asmItem.args[1]}] = acc"
                    "ldobjbyname", "ldobjbyindex" -> return "acc = acc[${asmItem.args[1]}]"
//                    "stobjbyname" -> return "--------"
//                    "stownbyname" -> return "--------"
                    "ldsuperbyname" -> return "acc = acc.super[${asmItem.args[1]}]"
                    "stsuperbyname" -> return "acc.super[${asmItem.args[1]}] = acc"
                    "ldlocalmodulevar" -> return "--------"
                    "ldexternalmodulevar" -> return "acc = ${asmItem.args[0]}"
                    "stconsttoglobalrecord" -> return "--------"
                    "stownbyvaluewithnameset" -> return "--------"
                    "stownbynamewithnameset" -> return "--------"
                    "ldbigint" -> return "--------"
                    "ldthisbyname", "ldthisbyvalue" -> return "acc = this[${asmItem.args[1]}]"
                    "stthisbyname", "stthisbyvalue" -> return "this[${asmItem.args[1]}] = acc"
//                    "ldthisbyvalue" -> return "acc = this"
//                    "stthisbyvalue" -> return "--------"
                    "dynamicimport" -> return "--------"
                    "asyncgeneratorreject" -> return "--------"
                    "setgeneratorstate" -> return "--------"
                    else -> return defaultRes(asmItem)
                }
            }
            "definition instuctions" -> {
                when(asmItem.asmName) {
                    "definegettersetterbyvalue" -> return "--------"
                    "definefunc" -> return "acc = function ${asmItem.args[1]}"
                    "definemethod" -> return "--------"
                    "defineclasswithbuffer" -> {

                        var buff = withBuffer(asmItem.args[2]){ key, value ->
                            return@withBuffer "$value : $key"
                        }
                        return "class ${asmItem.args[1]} ($buff) {}"
                    }
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
                    "createarraywithbuffer" -> {
                        val buff = withBuffer(asmItem.args[1]){ key, value ->
                            return@withBuffer value.toString()
                        }
                        return "acc = [${buff}]"
                    }
                    "createobjectwithbuffer" -> return "--------"
                    "createregexpwithliteral" -> return "--------"
                    "newobjapply" -> {
                        return "acc = new ${asmItem.args[1]}(acc)"
                    }
                    "newobjrange" -> {
                        val arg_len = asmItem.args[1].toInt() - 1
                        val s_reg_idx = asmItem.args[2].split("v")[1].toInt()
                        return "acc = new ${asmItem.args[2]}(${splicingArgRange(s_reg_idx+1, arg_len)})"
                    }
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
                    "ldsymbol" -> return "acc = SYMBOL"
                    "ldglobal" -> return "acc = GLOBAL"
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
                    "isin" -> return "acc = ${asmItem.args[1]} in acc"
                    "instanceof" -> return "acc = ${asmItem.args[1]} instanceof acc"
                    "strictnoteq" -> return "acc = ${asmItem.args[0]} !== ${asmItem.args[1]}"
                    "stricteq" -> return "acc = ${asmItem.args[0]} === ${asmItem.args[1]}"
                    else -> return defaultRes(asmItem)
                }
            }
            "unary operations" -> {
                when(asmItem.asmName) {
                    "typeof" -> return "acc = typeof(acc)"
                    "tonumber" -> return "acc = to_number(acc)"
                    "tonumeric" -> return "acc = to_numeric(acc)"
                    "neg" -> return "acc = -acc"
                    "not" -> return "acc = ~acc"
                    "inc" -> return "acc = acc + 1"
                    "dec" -> return "acc = acc - 1"
                    "istrue" -> return "acc = acc == true"
                    "isfalse" -> return "acc = acc == false"
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