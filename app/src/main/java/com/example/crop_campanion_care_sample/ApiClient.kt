import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://chatbot-ccc0609.openai.azure.com/openai/deployments/chatbot-ccc0609/chat/completions?api-version=2023-03-15-preview" // Ensure this URL is correct
    private const val API_KEY = "1ff646be54f34f98a3b317d7438887d6" // Ensure this key is correct

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $API_KEY")
                .build()
            chain.proceed(request)
        }
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
