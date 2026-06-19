package com.fitrinurhidayat0078.movielog.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fitrinurhidayat0078.movielog.model.Film

@Database(
    entities = [Film::class],
    version = 2,
    exportSchema = false
)
abstract class FilmDb : RoomDatabase() {

    abstract val dao: FilmDao

    companion object {
        @Volatile
        private var INSTANCE: FilmDb? = null

        fun getInstance(context: Context): FilmDb {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        FilmDb::class.java,
                        "film.db"
                    )
                        .fallbackToDestructiveMigration(true)
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}