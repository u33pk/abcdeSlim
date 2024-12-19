package com.abcslim.abcde

import com.google.common.graph.MutableValueGraph
import com.google.gson.Gson
import com.google.gson.JsonObject
import me.yricky.oh.abcd.code.Code
import me.yricky.oh.abcd.isa.BaseBlockControlFlow

class Graph {

    fun CFG(code: Code?):String{
        if(code != null) {
            val graph = code.asm.baseBlockCF
            return GraphToJson(graph)
        }else{
            return "null"
        }
    }

    fun DOM(code: Code?):String{
        if(code != null) {
            val graph = code.asm.dominatorTree
            return GraphToJson(graph)
        }else{
            return "null"
        }

    }

    fun GraphToJson(graph: MutableValueGraph<BaseBlockControlFlow.BaseBlock,Boolean>): String {
        data class Node(val node: JsonObject)
        data class Edge(val source:String, val target:String, val value: Boolean)
        data class Graph(val nodes: List<Node>, val edges: List<Edge>)
        val gson = Gson()
        val nodes = graph.nodes().map { node ->
            Node(node = node.toJson())
        }
        val edges = graph.edges().map { edge ->
            val source = edge.nodeU().getName1()
            val target = edge.nodeV().getName1()
            val value = graph.edgeValue(edge.nodeU(),edge.nodeV()).orElse(false)
            Edge(source = source,target = target,value = value)
        }

        val graphData = Graph(nodes = nodes, edges = edges)
        return gson.toJson(graphData)
    }
}

