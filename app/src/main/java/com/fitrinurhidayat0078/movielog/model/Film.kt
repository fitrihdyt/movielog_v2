package com.fitrinurhidayat0078.movielog.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "film")
data class Film(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val judul: String,
    val genre: String,
    val ulasan: String,
    val status: String,
    val poster: String = ""
)