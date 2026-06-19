package com.fitrinurhidayat0078.movielog.network

import com.fitrinurhidayat0078.movielog.model.Film
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

const val MOVIE_PREFIX = "[MovieLog0078]"

private const val BASE_URL = "https://api.escuelajs.co/api/v1/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface FilmApiService {
    @GET("products/?title=%5BMovieLog0078%5D")
    suspend fun getFilm(): String
}

object FilmApi {
    val service: FilmApiService by lazy {
        retrofit.create(FilmApiService::class.java)
    }
}

data class ProductResponse(
    val id: Long,
    val title: String,
    val price: Int?,
    val description: String?,
    val images: List<String>?,
    val category: CategoryResponse?
) {
    fun toFilm(): Film {
        val desc = description.orEmpty()

        val genreFilm = desc.substringAfter("genre:", category?.name ?: "Movie")
            .substringBefore("\n")
            .trim()

        val ulasanFilm = desc.substringAfter("ulasan:", desc)
            .substringBefore("\nstatus:")
            .trim()

        val statusFilm = desc.substringAfter("status:", "Belum ditonton")
            .substringBefore("\n")
            .trim()

        return Film(
            id = id,
            judul = title.removePrefix("$MOVIE_PREFIX ").trim(),
            genre = genreFilm,
            ulasan = ulasanFilm,
            status = statusFilm,
            poster = images?.firstOrNull().orEmpty()
        )
    }
}

data class CategoryResponse(
    val id: Long?,
    val name: String?,
    val image: String?,
    val slug: String?
)

enum class ApiStatus {
    LOADING,
    SUCCESS,
    FAILED
}