package com.example.crop_campanion_care_sample

import ChatAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    private lateinit var recyclerView: RecyclerView
    private lateinit var questionEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycle_view)
        questionEditText = findViewById(R.id.Question)
        sendButton = findViewById(R.id.Send)

        chatAdapter = ChatAdapter()
        recyclerView.adapter = chatAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        sendButton.setOnClickListener {
            val question = questionEditText.text.toString().trim()
            if (question.isNotEmpty()) {
                // Add user message to chat
                addMessageToChat(question, true)

                // Show typing effect before bot response
                showTypingEffect()

                getResponse(question) { response ->
                    runOnUiThread {
                        // Update typing message with actual response
                        chatAdapter.updateLastMessage(response)
                    }
                }
                questionEditText.text.clear()
            } else {
                Toast.makeText(this, "Please enter a question", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addMessageToChat(message: String, isUserMessage: Boolean) {
        chatAdapter.addMessage(ChatItem(message, isUserMessage))
        recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
    }

    private fun showTypingEffect() {
        // Add typing indicator message
        addMessageToChat("...", false)

    }

    private fun getResponse(question: String, callback: (String) -> Unit) {
        val apiKey = "004009d9bb3a4d029262e9691d60ccbc"  // Replace with your actual Azure OpenAI API key
        val url = "https://chatbot-ccc0609.openai.azure.com/openai/deployments/chatbot-ccc0609/chat/completions?api-version=2023-03-15-preview"

        val requestBody = """
        {
            "messages": [
                {
                    "role": "user",
                    "content": "$question"
                }
            ],
            "temperature": 0.7,
            "max_tokens": 800,
            "top_p": 0.95
        }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("api-key", apiKey)
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", "API failed", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "API request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    addMessageToChat("Error occurred: ${e.message}", false)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body != null) {
                    Log.v("data", "Response Body: $body")
                    try {
                        val jsonObject = JSONObject(body)

                        if (jsonObject.has("error")) {
                            val errorMessage = jsonObject.getJSONObject("error").getString("message")
                            throw Exception("API Error: $errorMessage")
                        }

                        val choicesArray = jsonObject.getJSONArray("choices")
                        if (choicesArray.length() > 0) {
                            val firstChoice = choicesArray.getJSONObject(0)
                            val messageContent = when {
                                firstChoice.has("message") -> firstChoice.getJSONObject("message").getString("content")
                                firstChoice.has("text") -> firstChoice.getString("text")
                                else -> throw Exception("Unexpected response format")
                            }
                            callback(messageContent)
                        } else {
                            throw Exception("No choices in the response")
                        }
                    } catch (e: Exception) {
                        Log.e("error", "Failed to parse response", e)
                        runOnUiThread {
                            addMessageToChat("Failed to parse the response: ${e.message}", false)
                        }
                    }
                } else {
                    Log.v("data", "empty")
                    runOnUiThread {
                        addMessageToChat("Empty response from the server", false)
                    }
                }
            }
        })
    }
}
