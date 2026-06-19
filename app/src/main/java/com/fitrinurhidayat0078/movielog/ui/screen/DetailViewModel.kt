package com.fitrinurhidayat0078.movielog.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitrinurhidayat0078.movielog.database.FilmDao
import com.fitrinurhidayat0078.movielog.model.Film
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailViewModel(
    private val dao: FilmDao
) : ViewModel() {

    fun insert(
        judul: String,
        genre: String,
        ulasan: String,
        status: String,
        poster: String
    ) {
        val film = Film(
            judul = judul,
            genre = genre,
            ulasan = ulasan,
            status = status,
            poster = poster
        )

        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(film)
        }
    }

    suspend fun getFilm(id: Long): Film? {
        return dao.getFilmById(id)
    }

    fun update(
        id: Long,
        judul: String,
        genre: String,
        ulasan: String,
        status: String,
        poster: String
    ) {
        val film = Film(
            id = id,
            judul = judul,
            genre = genre,
            ulasan = ulasan,
            status = status,
            poster = poster
        )

        viewModelScope.launch(Dispatchers.IO) {
            dao.update(film)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteById(id)
        }
    }
}