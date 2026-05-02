package com.socialauto.gemma

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class OllamaClient(private val baseUrl: String, private val model: String = "gemma4") {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    interface StreamListener {
        fun onToken(token: String)
        fun onError(error: String)
        fun onComplete()
    }

    fun chatStream(messages: List<ChatMessage>, listener: StreamListener) {
        val url = baseUrl.trimEnd('/') + "/api/chat"
        val jsonMessages = JSONArray()
        for (msg in messages) {
            jsonMessages.put(JSONObject().apply {
                put("role", msg.role)
                put("content", msg.content)
            })
        }
        val bodyJson = JSONObject().apply {
            put("model", model)
            put("messages", jsonMessages)
            put("stream", true)
        }
        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    listener.onError("HTTP ${response.code}")
                    return
                }
                val source = response.body?.source() ?: run {
                    listener.onError("Empty response")
                    return
                }
                try {
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: continue
                        if (line.isBlank()) continue
                        val obj = JSONObject(line)
                        if (obj.has("message")) {
                            val msgObj = obj.getJSONObject("message")
                            val content = msgObj.optString("content", "")
                            if (content.isNotEmpty()) {
                                listener.onToken(content)
                            }
                        }
                        if (obj.optBoolean("done", false)) {
                            listener.onComplete()
                            return
                        }
                    }
                    listener.onComplete()
                } catch (e: Exception) {
                    listener.onError("Parse error: ${e.message}")
                }
            }
        })
    }
}
