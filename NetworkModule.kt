package com.example.menuannam

// Sửa lại các import bị sai và thêm các import còn thiếu
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Url

// === 1. Data Models cho API ===
@Serializable
data class GenerateTokenRequest(val email: String)

@Serializable
data class GenerateTokenResponse(val code: Int, val message: String)

@Serializable
data class ApiResponse(val code: Int, val message: String)

@Serializable
data class GenerateAudioRequest(
    val word: String,
    val email: String,
    val token: String
)

// === 2. Retrofit Interface ===
interface NetworkService {
    @PUT
    suspend fun generateToken(
        @Url url: String = "https://egsbwqh7kildllpkijk6nt4soq0wlgpe.lambda-url.ap-southeast-1.on.aws/",
        @Body request: GenerateTokenRequest
    ): GenerateTokenResponse

    @PUT("https://ityqwv3rx5vifjpyufgnpkv5te0ibrcx.lambda-url.ap-southeast-1.on.aws/")
    suspend fun generateAudio(@Body request: GenerateAudioRequest): ApiResponse
}

// === 3. Đối tượng NetworkModule để khởi tạo và cung cấp Retrofit ===
object NetworkModule {

    // Cấu hình Json để bỏ qua các key không xác định từ server
    private val json = Json { ignoreUnknownKeys = true }

    // Khởi tạo Retrofit
    private val retrofit = Retrofit.Builder()
        // Cần một Base URL, dù bạn dùng @Url. Có thể để tạm một URL hợp lệ.
        .baseUrl("https://placeholder.com/")
        // Sử dụng converter cho Kotlinx Serialization
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build() // Phương thức build() được gọi ở đây

    // Cung cấp một instance của NetworkService để các nơi khác có thể gọi
    val networkService: NetworkService by lazy {
        retrofit.create(NetworkService::class.java)
    }
}
