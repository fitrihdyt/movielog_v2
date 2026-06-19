package com.fitrinurhidayat0078.movielog.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fitrinurhidayat0078.movielog.model.Film
import kotlinx.coroutines.flow.Flow

@Dao
interface FilmDao {

    @Insert
    suspend fun insert(film: Film)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(film: List<Film>)

    @Update
    suspend fun update(film: Film)

    @Query("SELECT * FROM film ORDER BY id DESC")
    fun getFilm(): Flow<List<Film>>

    @Query("SELECT * FROM film WHERE id = :id")
    suspend fun getFilmById(id: Long): Film?

    @Query("DELETE FROM film WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM film")
    suspend fun deleteAll()
}