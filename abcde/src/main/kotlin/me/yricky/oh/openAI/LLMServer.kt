package me.yricky.oh.openAI

import org.devlive.sdk.openai.OpenAiClient
import org.devlive.sdk.openai.entity.ChatEntity
import org.devlive.sdk.openai.entity.CompletionEntity
import org.devlive.sdk.openai.entity.MessageEntity

import java.util.stream.Collectors
import kotlin.collections.ArrayList

class LLMServer(host: String, key: String, mod: String) {
    var api_key = key
    var api_host = host
    var api_mod = mod
    fun ai_ode(code: String): String {
        var client = OpenAiClient.builder()
            .apiHost(api_host)
            .apiKey(api_key)
            .timeout(300)
            .build()

        client.models.models.forEach{ println(it.name) }
        var msgs = ArrayList<MessageEntity>()
        var msg_conf = ChatEntity.builder()
            .messages(msgs)
            .maxTokens(4096)
            .build()
        msg_conf.model = api_mod
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
        var result = StringBuilder()
        client.createChatCompletion(msg_conf)
            .choices
            .forEach{choice -> result.append(choice.message.content) }
        return result.toString()
    }
}