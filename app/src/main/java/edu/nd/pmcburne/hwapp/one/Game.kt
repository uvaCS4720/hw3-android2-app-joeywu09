package edu.nd.pmcburne.hwapp.one

import android.content.Context
import androidx.room.Room
import androidx.room.Database
import androidx.room.RoomDatabase
enum class Gender(val apiSlug: String) {
    MEN("men"),
    WOMEN("women")
}

object DbModule {
    fun create(context: Context): AppDb =
        Room.databaseBuilder(context, AppDb::class.java, "scores.db")
            .fallbackToDestructiveMigration()
            .build()
}


@Database(
    entities = [GameEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDb : RoomDatabase() {
    abstract fun gameDao(): GameDao
}