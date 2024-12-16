package com.abcslim

import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.engine.*
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.utils.asAbcBuf
import java.io.File
import java.nio.channels.FileChannel


fun main(args: Array<String>) {
    embeddedServer(Netty, 8080) {
        routing {
            get("/") {
                call.respondText(test(), ContentType.Text.Html)
            }
        }
    }.start(wait = true)
}

fun Application.module() {
    configureRouting()
}

fun test(): String{
    val file = File("/Users/orz/DevEcoStudioProjects/RevComp/entry/build/default/intermediates/loader_out/default/ets/360.abc")
    val abc = file.asAbcBuf()
    val sb = StringBuilder()
    abc.classes.forEach { l ->
        sb.append(l.value.name, "\n")
    }
    return sb.toString()
}