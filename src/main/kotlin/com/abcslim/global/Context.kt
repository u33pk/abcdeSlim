package com.abcslim.global

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.utils.asAbcBuf
import java.io.File

object Context {

    lateinit var abcFile: File
    lateinit var abc: AbcBuf //= abcFile.asAbcBuf()
    val data = HashMap<String, Any?>()

    fun initialize(abcFile_path: String){
        abcFile = File(abcFile_path)
        abc = abcFile.asAbcBuf()
    }

    fun addData(key: String, value: Any?){
        data[key] = value
    }

    fun getData(key: String): Any? {
        return if(key in data)
            data[key]
        else
            null
    }
}