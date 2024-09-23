package com.example.crop_campanion_care_sample

import ChatAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    private lateinit var recyclerView: RecyclerView
    private lateinit var questionEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize chat components
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
        val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=AIzaSyAuf83Jt6Er2tqVOokHFQHHjznS8PvScys"

        // Adjust the request body structure as per the Gemini API's requirements
        val requestBody = """
        {
            "contents": [
                {
                    "parts": [
                        {
                            "text": "$question"
                        }
                    ]
                }
            ]
        }
        """.trimIndent()

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Content-Type", "application/json")
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

                        val candidates = jsonObject.getJSONArray("candidates")
                        if (candidates.length() > 0) {
                            val content = candidates.getJSONObject(0).getJSONObject("content")
                            val parts = content.getJSONArray("parts")
                            if (parts.length() > 0) {
                                val messageContent = parts.getJSONObject(0).getString("text")
                                callback(messageContent)
                            } else {
                                throw Exception("No text content found in the response")
                            }
                        } else {
                            throw Exception("No candidates found in the response")
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