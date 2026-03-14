package edu.nd.pmcburne.hwapp.one


import retrofit2.http.GET
import retrofit2.http.Path
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.google.gson.annotations.SerializedName

// Retrofit interface for the NCAA API
interface NcaaApi {
    @GET("scoreboard/basketball-{gender}/d1/{yyyy}/{mm}/{dd}")
    suspend fun getScoreboard(
        @Path("gender") gender: String,
        @Path("yyyy") yyyy: String,
        @Path("mm") mm: String,
        @Path("dd") dd: String
    ): ScoreboardResponse
}

// Data classes for parsing JSON

data class ScoreboardResponse(
    val games: List<GameWrapper> = emptyList()
)

data class GameWrapper(
    val game: ApiGame
)

//Response classes
//data class ApiResponse(val events: List<ApiGame>)
//
//data class ApiGame(
//    val id: String,
//    val home: TeamInfo,
//    val away: TeamInfo,
//    val status: StatusInfo
//)
//
//data class TeamInfo(val name: String, val score: Int?)
//
//data class StatusInfo(
//    val type: String,        // UPCOMING, IN_PROGRESS, FINAL
//    val startTime: String?,  // "2026-03-13T19:00Z"
//    val period: String?,
//    val clock: String?,
//    val winner: String?      // "home", "away", or null
//)
data class ApiGame(
    @SerializedName("gameID") val gameId: String,
    val gameState: String = "",
    val startTime: String = "",
    val startTimeEpoch: String = "0",
    val currentPeriod: String = "",
    val contestClock: String = "",
    val finalMessage: String = "",
    val home: ApiTeam,
    val away: ApiTeam
)

data class ApiTeam(
    val score: String = "",
    val winner: Boolean = false,
    val names: ApiNames
)

data class ApiNames(
    val short: String = ""
)

object NetworkModule {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: NcaaApi = Retrofit.Builder()
        .baseUrl("https://ncaa-api.henrygd.me/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NcaaApi::class.java)
}