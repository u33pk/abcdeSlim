package com.abcslim.abcde

import com.abcslim.global.Context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.yricky.oh.utils.asAbcBuf
import java.io.File

class AbcClazz {
//    val mContext = ctx
//    val abcFile = mContext.abcFile
    val abc = Context.abc

    val abcClasses by lazy {
        // 创建根 JSON 对象
        val root = JsonObject()
        val gson = Gson()
        // 处理每个路径
        abc.classes.forEach { clazz ->
            val path = clazz.value.name
            val parts = path.split("/")
            var current = root
            val item = ClazzItem(clazz.value)

            // 遍历路径的每一部分
            for (i in 0 until parts.size - 1) {
                val key = parts[i]
                val nextKey = parts[i + 1]

                // 如果当前节点不存在，创建一个新的 JSON 对象
                if (!current.has(key)) {
                    current.add(key, JsonObject())
                }

                // 如果下一级是数组，则将当前节点转换为数组 a0/a1/a2/a3
                if (nextKey.first().isDigit()) {
                    if (current[key] !is JsonArray) {
                        current.add(key, JsonArray())
                    }
                    current = current.getAsJsonArray(key).last() as JsonObject
                } else {
                    current = current.getAsJsonObject(key)
                }
            }

            // 添加最后一级的值
            val lastKey = parts.last()
            val fields = JsonArray()
            val methods = JsonArray()
            item.fields.forEach {
                fields.add(it)
            }
            item.methods.forEach { (key, value) ->
                methods.add(key)
                Context.addData("${path}/${key}", value)
            }
            val clazzItem = JsonObject()
            clazzItem.add("fields", fields)
            clazzItem.add("methods", methods)
            current.add(lastKey, clazzItem)

        }

        // 使用 Gson 将 JSON 对象转换为字符串

        val jsonString = gson.toJson(root)
        jsonString
    }



}