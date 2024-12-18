package com.abcslim

import com.abcslim.global.Config
import com.abcslim.global.Context
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
    Context.initialize(Config.filePath)
    EngineMain.main(args)
}

fun Application.module() {
    configureRouting()
}

