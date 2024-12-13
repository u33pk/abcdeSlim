package io.github.yricky.oh

import org.devlive.sdk.openai.OpenAiClient
import org.devlive.sdk.openai.entity.ChatEntity
import org.devlive.sdk.openai.entity.CompletionEntity
import org.devlive.sdk.openai.entity.MessageEntity

import java.util.stream.Collectors
import kotlin.collections.ArrayList



class DeepSeek {
    fun test(code: String) {
        val api_key = ""
        var client = OpenAiClient.builder()
            .apiHost("https://api.deepseek.com/")
            .apiKey(api_key)
            .build()
        client.models.models.forEach{ println(it.name) }
        var msgs = ArrayList<MessageEntity>()
        var msg_conf = ChatEntity.builder()
            .messages(msgs)
            .build()
        msg_conf.model = "deepseek-coder"
        //提示词
        msgs.add(MessageEntity.builder()
            .content("You are a code optimizer. Starting from the next sentence, don't reply to my superfluous content. Just the code ontology, optimize the pseudo-code into javascript code, remove intermediate variables, correctly identify the order of code blocks, and use GOTO statements as little as possible. If you are ready, tell me yes")
            .build())
        client.createChatCompletion(msg_conf)
            .choices
            .forEach{choice ->
                msgs.add(choice.message)
                println(choice.message.content)
            }
        // 要优化的代码
        msgs.add(MessageEntity.builder()
            .content("import SelfC from './SelfC';\n" +
                    "\n" +
                    "function test(FunctionObject, NewTarget, this) {\n" +
                    "var aac, v0, v1, v2, v3, v4, v5, v6, v7;\n" +
                    "v0 = FunctionObject\n" +
                    "v1 = NewTarget\n" +
                    "v2 = this\n" +
                    "v6 = acc\n" +
                    "acc = SelfC\n" +
                    "v4 = acc\n" +
                    "v5 = acc\n" +
                    "acc = v4\n" +
                    "aac = aac[\"prototype\"]\n" +
                    "v7 = acc\n" +
                    "acc = v4\n" +
                    "acc = stmodulevar(acc, 0)\n" +
                    "return acc\n" +
                    "}")
            .build())
        client.createChatCompletion(msg_conf)
            .choices
            .forEach{choice -> println(choice.message.content) }
    }

}
