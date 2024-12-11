package me.yricky.abcde.page

import edu.uci.ics.jung.algorithms.layout.FRLayout
import edu.uci.ics.jung.graph.DirectedSparseMultigraph
import edu.uci.ics.jung.visualization.Layer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin
import me.yricky.oh.abcd.code.Code
import me.yricky.oh.abcd.isa.BaseBlockControlFlow
import java.awt.Dimension
import java.awt.Font
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import javax.swing.JFrame
import javax.swing.WindowConstants

class CFGView {

    fun CFG(code: Code){

        val graph = code.asm.baseBlockCF
        val jungGraph = DirectedSparseMultigraph<BaseBlockControlFlow.BaseBlock, String>()
        graph.nodes().forEach { node ->
            jungGraph.addVertex(node) // 添加节点
        }
        var a = 0
        graph.edges().forEach { edge ->
            val source = edge.nodeU()
            val target = edge.nodeV()

            val edgeLabel = graph.edgeValueOrDefault(source, target, false)
            jungGraph.addEdge(edgeLabel.toString()+a.toString(),source,target) // 添加边
            a+=1
        }

        // 创建一个布局并绘制 JUNG 图
        val layout = FRLayout(jungGraph)
        layout.setRepulsionMultiplier(2.0)

        layout.size = Dimension(1000, 1000)
        val visualizationServer = VisualizationViewer<BaseBlockControlFlow.BaseBlock, String>(layout, Dimension(500, 500))
//                        val visualizationServer = BasicVisualizationServer<BaseBlockControlFlow.BaseBlock, String>(layout, Dimension(500, 500))


        //设置节点居中，形状，大小
        visualizationServer.renderContext.setVertexShapeTransformer { vertex ->
            val width = 200
            val lines = vertex.li.size
            val height = lines * 20
            val rectangle = Rectangle(-width/2, -height/2, width, height)
            rectangle
        }

        //节点内容渲染器，将代码绘制进节点
        visualizationServer.renderer.setVertexRenderer { rc, layout, v ->
            val graphics = rc.graphicsContext
            var p: Point2D = layout.apply(v)
            p = rc.multiLayerTransformer.transform(Layer.LAYOUT,p)
            val shape = rc.vertexShapeTransformer.apply(v)
            val bounds = shape.bounds
            //平移形状到正确位置
            val translatedShape = AffineTransform.getTranslateInstance(p.x, p.y).createTransformedShape(shape)

            //填充节点背景颜色
            graphics.color = java.awt.Color.lightGray
            graphics.fill(translatedShape)
            graphics.color = java.awt.Color.black
            graphics.draw(translatedShape)

            //计算填充节点文字的位置
            val x: Double = p.x - bounds.width/2
            val y: Double = p.y - bounds.height/2 + 15 //高度微调
            graphics.font = Font("Arial", Font.BOLD, 12)
            val text = v.toString()
            val metrics = graphics.fontMetrics
            val textWidth = metrics.stringWidth(text)
            val textHeight = metrics.height
            graphics.color = java.awt.Color.black
            //遍历bbk，绘制指令到节点中
            val lines = text.split("\n")
            var currentY = y.toInt()
            lines.forEach { line ->
                graphics.drawString(line, x.toInt(), currentY)
                currentY += 20
            }
        }

        //设置跳转边和顺序边颜色
        visualizationServer.renderContext.setEdgeDrawPaintTransformer { edge ->
            var flag = "true"
            if(edge.startsWith("false")) flag = "false"
            when(flag){
                "true"-> java.awt.Color.BLUE
                else -> java.awt.Color.RED
            } as java.awt.Paint
        }


        // 将结果放入一个 Swing 窗口
        val frame = JFrame("MutableValueGraph Visualization")
        frame.contentPane.add(visualizationServer)
        frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        frame.pack()
        frame.isVisible = true



        // 创建 DefaultModalGraphMouse
        val graphMouse = DefaultModalGraphMouse<String, String>()
        // 获取 graphMouse 的模式
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING)
        // 添加节点拖拽插件
        val pickingPlugin = PickingGraphMousePlugin<String, String>()
        graphMouse.add(pickingPlugin)
        // 添加空白处拖拽平移插件
        val translatingPlugin = TranslatingGraphMousePlugin()
        graphMouse.add(translatingPlugin)
        // 应用到 VisualizationServer
        visualizationServer.graphMouse = graphMouse

    }
}