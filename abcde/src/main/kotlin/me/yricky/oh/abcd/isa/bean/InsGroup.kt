package me.yricky.oh.abcd.isa.bean

import com.google.gson.annotations.SerializedName



data class InsGroup(
    @SerializedName("title")
    val title:String = "",
    @SerializedName("description")
    val description:String = "",
    @SerializedName("properties")
    val properties:List<String>? = null,
    @SerializedName("exceptions")
    val exceptions:List<String> = emptyList(),
    @SerializedName("verification")
    val verification:List<String> = emptyList(),
    @SerializedName("namespace")
    val namespace:String? = null,
    @SerializedName("pseudo")
    val pseudo:String = "",
    @SerializedName("semantics")
    val semantics:String? = null,
    @SerializedName("instructions")
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