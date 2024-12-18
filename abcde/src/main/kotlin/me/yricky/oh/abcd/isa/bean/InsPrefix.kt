package me.yricky.oh.abcd.isa.bean

import com.google.gson.annotations.SerializedName



data class InsPrefix(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("opcode_idx")
    val opcodeIdx:Int
)