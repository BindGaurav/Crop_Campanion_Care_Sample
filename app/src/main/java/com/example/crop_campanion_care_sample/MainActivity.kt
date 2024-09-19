package com.example.crop_campanion_care_sample

import ChatAdapter
import ChatRequest
import ChatResponse
import OpenAIApi
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var questionEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatAdapter: ChatAdapter // Adapter to manage chat messages

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        recyclerView = findViewById(R.id.recycle_view)
        questionEditText = findViewById(R.id.Question)
        sendButton = findViewById(R.id.Send)

        // Set up RecyclerView
        chatAdapter = ChatAdapter()
        recyclerView.adapter = chatAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Send the request when the user clicks the send button
        sendButton.setOnClickListener {
            val userQuestion = questionEditText.text.toString()
            if (userQuestion.isNotEmpty()) {
                sendPrompt(userQuestion)
                questionEditText.text.clear()
            }
        }
    }

    private fun sendPrompt(question: String) {
        val api = ApiClient.retrofit.create(OpenAIApi::class.java)

        // Create request messages
        val userMessage = ChatRequest.Message(role = "user", content = question)

        // Build the request body
        val request = ChatRequest(
            model = "gpt-4o",  // Ensure this is the correct model you're using.
            messages = listOf(userMessage),
            temperature = 0.7,
            top_p = 0.95,
            max_tokens = 800
        )

        // Enqueue the API call
        val call = api.getChatResponse("Bearer YOUR_API_KEY", request) // Include your API key here
        call.enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()?.choices?.get(0)?.message?.content ?: "No response"
                    displayResponse(result)
                } else {
                    displayResponse("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                displayResponse("Failure: ${t.message}")
            }
        })
    }

    private fun displayResponse(response: String) {
        val chatItem = ChatItem(response, isUserMessage = false) // Set isUserMessage appropriately
        chatAdapter.addMessage(chatItem)
        recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
    }
}
