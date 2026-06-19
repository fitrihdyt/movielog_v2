package com.fitrinurhidayat0078.movielog.ui.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitrinurhidayat0078.movielog.database.FilmDao
import com.fitrinurhidayat0078.movielog.model.Film
import com.fitrinurhidayat0078.movielog.network.FilmApi
import com.fitrinurhidayat0078.movielog.network.ProductResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"

class MainViewModel(
    private val dao: FilmDao
) : ViewModel() {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(
        List::class.java,
        ProductResponse::class.java
    )

    private val jsonAdapter = moshi.adapter<List<ProductResponse>>(listType)

    val data: StateFlow<List<Film>> = dao.getFilm().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    init {
        retrieveData()
    }

    private fun retrieveData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = FilmApi.service.getFilm()
                val response = jsonAdapter.fromJson(json)

                if (response != null) {
                    val filmList = response.map { it.toFilm() }

                    dao.deleteAll()
                    dao.insertAll(filmList)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gagal mengambil data film", e)
            }
        }
    }
}