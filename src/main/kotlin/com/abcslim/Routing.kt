package com.abcslim

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.abcslim.abcde.AbcClazz
import com.abcslim.abcde.getAsm
import com.abcslim.abcde.Graph
import com.abcslim.abcde.getCode
import com.abcslim.abcde.getMethodCode
import com.abcslim.global.Context

//import com.abcslim.abcde.getMethodsFromClass


fun Application.configureRouting() {

    routing {
        post("/space") {

        }
        get("/classes") {
            call.respondText(
                AbcClazz().abcClasses,
                ContentType.Application.Json
            )
        }
        get("/method") {
            val methodPath = call.request.queryParameters["method"]
            call.respondText(getMethodCode(methodPath!!), ContentType.Text.Html)
        }

        get("/asm") {
            val methodPath = call.request.queryParameters["method"]
            call.respondText(getAsm(methodPath!!), ContentType.Text.Html)
            call.respondText(getMethodCode(methodPath!!), ContentType.Text.Html)
        }
        get("/method/cfg"){
            val methodPath = call.request.queryParameters["method"]
            val code = getCode(methodPath!!)
            call.respondText(Graph().CFG(code), ContentType.Application.Json)
        }
        get("/method/dom"){
            val methodPath = call.request.queryParameters["method"]
            val code = getCode(methodPath!!)
            call.respondText(Graph().DOM(code), ContentType.Application.Json)
        }
    }
}
