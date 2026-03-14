package edu.nd.pmcburne.hwapp.one

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import android.util.Log

class Repo(
    private val appContext: Context,
    private val api: NcaaApi,
    private val dao: GameDao
) {

    fun observe(date: LocalDate, gender: Gender): Flow<List<GameEntity>> =
        dao.observeGames(date.toString(), gender.apiSlug)

    suspend fun refresh(date: LocalDate, gender: Gender) {
        try {
            val yyyy = "%04d".format(date.year)
            val mm = "%02d".format(date.monthValue)
            val dd = "%02d".format(date.dayOfMonth)

            val response = api.getScoreboard(gender.apiSlug, yyyy, mm, dd)
            Log.d("Repo", "API Response: $response")

            if (response.games.isEmpty()) {
                Log.w("Repo", "No games returned from API for $date, ${gender.apiSlug}")
                return
            }

            val entities = response.games.map { wrapper ->
                val g = wrapper.game
                GameEntity(
                    dateIso = date.toString(),
                    gender = gender.apiSlug,
                    gameId = g.gameId,
                    homeName = g.home.names.short.ifBlank { "Home" },
                    awayName = g.away.names.short.ifBlank { "Away" },
                    homeScore = g.home.score.toIntOrNull(),
                    awayScore = g.away.score.toIntOrNull(),
                    isHomeWinner = g.home.winner,
                    isAwayWinner = g.away.winner,
                    gameState = g.gameState,
                    startTime = g.startTime,
                    startTimeEpoch = g.startTimeEpoch.toLongOrNull() ?: 0L, // make Long in data class
                    currentPeriod = g.currentPeriod,
                    contestClock = g.contestClock,
                    finalMessage = g.finalMessage
                )
            }

            dao.upsertAll(entities)
            Log.d("Repo", "Saved ${entities.size} games to database") //test
        } catch (ex: Exception) {
            Log.e("Repo", "Refresh failed", ex) //log fails
            throw ex // allow ViewModel.onFailure to catch and display message
        }
    }

    private fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}