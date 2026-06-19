package com.fitrinurhidayat0078.movielog.navigation

const val KEY_ID_FILM = "idFilm"
sealed class Screen(val route: String) {
    data object Home: Screen("mainScreen")
    data object FormBaru: Screen("detailScreen")
    data object FormUbah: Screen("detailScreen/{$KEY_ID_FILM}") {
        fun withId(id: Long) = "detailScreen/$id"
    }
}