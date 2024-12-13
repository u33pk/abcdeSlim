package io.github.yricky.oh

import org.devlive.sdk.openai.OpenAiClient
import org.devlive.sdk.openai.entity.ChatEntity
import org.devlive.sdk.openai.entity.CompletionEntity
import org.devlive.sdk.openai.entity.MessageEntity

import java.util.stream.Collectors
import kotlin.collections.ArrayList

class DeepSeek {
    fun ai_ode(code: String) {
        val api_key = ""
        var client = OpenAiClient.builder()
            .apiHost("https://api.deepseek.com/")
            .apiKey(api_key)
            .timeout(300)
            .build()

        client.models.models.forEach{ println(it.name) }
        var msgs = ArrayList<MessageEntity>()
        var msg_conf = ChatEntity.builder()
            .messages(msgs)
            .maxTokens(4096)
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
            .content(code)
            .build())
        client.createChatCompletion(msg_conf)
            .choices
            .forEach{choice -> println(choice.message.content) }
    }

}
