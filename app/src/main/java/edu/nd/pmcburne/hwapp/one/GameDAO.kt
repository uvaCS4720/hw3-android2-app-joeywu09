package edu.nd.pmcburne.hwapp.one

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Query(
        """SELECT * FROM games WHERE dateIso = :dateIso AND gender = :gender ORDER BY startTimeEpoch ASC, awayName ASC"""
    )
    fun observeGames(dateIso: String, gender: String): Flow<List<GameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<GameEntity>)
}