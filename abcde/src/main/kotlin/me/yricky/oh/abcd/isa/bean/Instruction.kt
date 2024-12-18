package me.yricky.oh.abcd.isa.bean

import com.google.gson.annotations.SerializedName



data class Instruction(
    @SerializedName("sig")
    val sig:String,
    @SerializedName("acc")
    val acc:String,
    @SerializedName("opcode_idx")
    val opcodeIdx:List<Int>,
    @SerializedName("format")
    val format: List<String>,
    @SerializedName("properties")
    val properties:List<String>? = null,
    @SerializedName("prefix")
    val prefix:String? = null
)
{
    var bbk_type: String = "other"
}