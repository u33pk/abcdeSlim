package com.abcslim

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.yricky.oh.utils.asAbcBuf
import java.io.File


fun Application.configureRouting() {
    routing {
        post("/space/create") {

        }
        get("/classes") {
            call.respondText(test(), ContentType.Text.Html)
        }
    }
}

fun test(): String{
    val file = File("/Users/orz/project/unitTest/out/test.abc")
    val abc = file.asAbcBuf()
    val res = ArrayList<String>()
    abc.classes.forEach { l ->
        res.add(l.value.name)
    }
    return res.toString()
}