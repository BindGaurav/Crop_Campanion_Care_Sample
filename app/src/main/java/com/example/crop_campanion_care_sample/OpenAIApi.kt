import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAIApi {
    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    fun getChatResponse(
        @Header("Authorization") authHeader: String, // Authorization header with Bearer token
        @Body request: ChatRequest
    ): Call<ChatResponse>
}
