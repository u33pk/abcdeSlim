package me.yricky.oh.abcd.isa.bean

import com.google.gson.annotations.SerializedName



data class Chapter(
    @SerializedName("name")
    val name:String = "",
    @SerializedName("text")
    val text:String = ""
)