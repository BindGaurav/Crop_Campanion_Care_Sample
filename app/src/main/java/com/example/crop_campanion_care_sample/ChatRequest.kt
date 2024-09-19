data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double,
    val top_p: Double,
    val max_tokens: Int
) {
    data class Message(
        val role: String,
        val content: String
    )
}
