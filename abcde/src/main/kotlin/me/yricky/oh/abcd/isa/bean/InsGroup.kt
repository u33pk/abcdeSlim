package me.yricky.oh.abcd.isa.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InsGroup(
    @SerialName("title")
    val title:String = "",
    @SerialName("description")
    val description:String = "",
    @SerialName("properties")
    val properties:List<String>? = null,
    @SerialName("exceptions")
    val exceptions:List<String> = emptyList(),
    @SerialName("verification")
    val verification:List<String> = emptyList(),
    @SerialName("namespace")
    val namespace:String? = null,
    @SerialName("pseudo")
    val pseudo:String = "",
    @SerialName("semantics")
    val semantics:String? = null,
    @SerialName("instructions")
    val instructions:List<Instruction> = emptyList()
)
{
    var genera = false
    var titles = arrayOf(
        "throw instructions",       // 抛出错误
        "jump operations",          // 直接跳转语句
        "dynamic return")           // return
//    "call instructions",        // 直接跳转语句
//    "call runtime functions",   // 直接跳转语句

    fun setType(){
        if(!this.genera) {
            instructions.forEach { ins ->
//                println(this.title)
                if (this.title in titles)
                    ins.bbk_type = "terminal"
                if (this.title == "jump operations")
                    ins.bbk_type = "jump"
            }
            this.genera = true
        }

    }
}