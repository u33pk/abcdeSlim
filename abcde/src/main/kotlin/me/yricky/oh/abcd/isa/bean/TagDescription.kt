package me.yricky.oh.abcd.isa.bean

import com.google.gson.annotations.SerializedName



data class TagDescription(
    @SerializedName("tag")
    val tag:String,
    @SerializedName("description")
    val description: String
)