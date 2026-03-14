package edu.nd.pmcburne.hwapp.one

import androidx.room.Entity

@Entity(
    tableName = "games",
    primaryKeys = ["dateIso", "gender", "gameId"]
)
data class GameEntity(
    val dateIso: String,
    val gender: String,
    val gameId: String,
    val homeName: String,
    val awayName: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val isHomeWinner: Boolean,
    val isAwayWinner: Boolean,
    val gameState: String,
    val startTime: String,
    val startTimeEpoch: Long,
    val currentPeriod: String,
    val contestClock: String,
    val finalMessage: String
)