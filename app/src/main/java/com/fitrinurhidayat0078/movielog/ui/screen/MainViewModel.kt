package com.fitrinurhidayat0078.movielog.ui.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitrinurhidayat0078.movielog.database.FilmDao
import com.fitrinurhidayat0078.movielog.model.Film
import com.fitrinurhidayat0078.movielog.network.ApiStatus
import com.fitrinurhidayat0078.movielog.network.CreateProductRequest
import com.fitrinurhidayat0078.movielog.network.FilmApi
import com.fitrinurhidayat0078.movielog.network.MOVIE_PREFIX
import com.fitrinurhidayat0078.movielog.network.ProductResponse
import com.fitrinurhidayat0078.movielog.network.UploadResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

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
    private val uploadAdapter = moshi.adapter(UploadResponse::class.java)
    private val createProductAdapter = moshi.adapter(CreateProductRequest::class.java)

    val data: StateFlow<List<Film>> = dao.getFilm().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    var status = MutableStateFlow(ApiStatus.LOADING)
        private set

    fun retrieveData(userEmail: String) {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                val json = FilmApi.service.getFilm(
                    title = MOVIE_PREFIX,
                    userEmail = userEmail
                )

                val response = jsonAdapter.fromJson(json)

                if (response != null) {
                    val filmList = response
                        .filter { product ->
                            val ownerEmail = getOwnerEmail(product.description.orEmpty())

                            ownerEmail.isBlank() || ownerEmail == userEmail
                        }
                        .map { product ->
                            product.toFilm()
                        }
                    dao.deleteAll()
                    dao.insertAll(filmList)
                    status.value = ApiStatus.SUCCESS
                } else {
                    status.value = ApiStatus.FAILED
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gagal mengambil data film", e)
                status.value = ApiStatus.FAILED
            }
        }
    }

    fun saveData(
        userEmail: String,
        imageFile: File,
        judul: String,
        genre: String,
        ulasan: String,
        statusFilm: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageRequestBody = imageFile
                    .asRequestBody("image/jpeg".toMediaTypeOrNull())

                val imagePart = MultipartBody.Part.createFormData(
                    name = "file",
                    filename = imageFile.name,
                    body = imageRequestBody
                )

                val uploadJson = FilmApi.service.uploadImage(imagePart)
                val uploadResponse = uploadAdapter.fromJson(uploadJson)

                val imageUrl = uploadResponse?.location
                if (imageUrl.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        onError("Gagal upload gambar.")
                    }
                    return@launch
                }
                val productRequest = CreateProductRequest(
                    title = "$MOVIE_PREFIX $judul",
                    price = 1,
                    description = """
                        user_email: $userEmail
                        genre: $genre
                        ulasan: $ulasan
                        status: $statusFilm
                    """.trimIndent(),
                    categoryId = 1,
                    images = listOf(imageUrl)
                )
                val jsonBody = createProductAdapter.toJson(productRequest)
                val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
                FilmApi.service.addFilm(requestBody)

                retrieveData(userEmail)

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gagal menyimpan film", e)

                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Gagal menyimpan film.")
                }
            }
        }
    }

    private fun getOwnerEmail(description: String): String {
        return if (description.contains("user_email:")) {
            description.substringAfter("user_email:")
                .substringBefore("\n")
                .trim()
        } else {
            ""
        }
    }
}