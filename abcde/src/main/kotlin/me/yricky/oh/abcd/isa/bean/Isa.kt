package me.yricky.oh.abcd.isa.bean

import com.google.gson.annotations.SerializedName


data class Isa(
    @SerializedName("chapters")
    val chapters:List<Chapter> = emptyList(),
    @SerializedName("min_version")
    val minVersion:String = "",
    @SerializedName("version")
    val version:String = "",
    @SerializedName("api_version_map")
    val apiVersionMap:List<List<Any>> = emptyList(),
    @SerializedName("incompatible_version")
    val incompatibleVersion:List<String> = emptyList(),
    @SerializedName("properties")
    val properties:List<TagDescription> = emptyList(),
    @SerializedName("exceptions")
    val exceptions:List<TagDescription> = emptyList(),
    @SerializedName("verification")
    val verification:List<TagDescription> = emptyList(),
    @SerializedName("prefixes")
    val prefixes:List<InsPrefix> = emptyList(),
    @SerializedName("groups")
    val groups:List<InsGroup> = emptyList()
)