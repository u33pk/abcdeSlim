package com.abcslim.abcde

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.cfm.ClassItem
import me.yricky.oh.abcd.cfm.ForeignClass

class ClazzItem(item: ClassItem) {
    val methods = HashMap<String, MethodCode?>()
    val fields = ArrayList<String>()
    val _item: ClassItem = item

    init {
        generaMethods()
        generaFields()
    }
    fun generaMethods() {
        val result = ArrayList<String>()
        if (_item is AbcClass) {
            _item.methods.forEach { method ->
                methods[method.name] = MethodCode(method)
            }
        }

    }
    fun generaFields() {
        val result = ArrayList<String>()
        if (_item is AbcClass) {
            _item.fields.forEach { method ->
                fields.add(method.name)
            }
        }
    }


}