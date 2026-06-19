package com.fitrinurhidayat0078.movielog.ui.screen

import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.fitrinurhidayat0078.movielog.BuildConfig
import com.fitrinurhidayat0078.movielog.R
import com.fitrinurhidayat0078.movielog.model.Film
import com.fitrinurhidayat0078.movielog.model.User
import com.fitrinurhidayat0078.movielog.network.ApiStatus
import com.fitrinurhidayat0078.movielog.network.UserDataStore
import com.fitrinurhidayat0078.movielog.ui.theme.MovieLogTheme
import com.fitrinurhidayat0078.movielog.util.SettingsDataStore
import com.fitrinurhidayat0078.movielog.util.ViewModelFactory
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Maroon = Color(0xFF7A1F2B)
private val DarkMaroon = Color(0xFF3B0B14)

@Suppress("UNUSED_PARAMETER", "UNUSED_VALUE")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    var showSplash by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1800)
        showSplash = false
    }
    if (showSplash) {
        SplashScreen()
        return
    }

    val context = LocalContext.current
    val dataStore = remember { SettingsDataStore(context) }
    val userDataStore = remember { UserDataStore(context) }
    val factory = ViewModelFactory(context)
    val viewModel: MainViewModel = viewModel(factory = factory)

    val showList by dataStore.layoutFlow.collectAsState(true)
    val darkMode by dataStore.darkModeFlow.collectAsState(false)
    val userState by userDataStore.userFlow.collectAsState(initial = null)
    val ownedFilmIds by viewModel.ownedFilmIds.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    if (userState == null) {
        SplashScreen()
        return
    }

    val user = userState!!

    var showProfileDialog by remember { mutableStateOf(false) }
    var showFilmDialog by remember { mutableStateOf(false) }
    var filmToDelete by remember { mutableStateOf<Film?>(null) }
    var filmToEdit by remember { mutableStateOf<Film?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        val croppedImage = getCroppedImage(context.contentResolver, result)

        if (croppedImage != null) {
            bitmap = croppedImage
            showFilmDialog = true
            Log.d("IMAGE", "Gambar berhasil dipilih dan di-crop.")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Maroon,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                dataStore.saveLayout(!showList)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                if (showList) R.drawable.baseline_grid_view_24
                                else R.drawable.baseline_view_list_24
                            ),
                            contentDescription = stringResource(
                                if (showList) R.string.grid
                                else R.string.list
                            ),
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                dataStore.saveDarkMode(!darkMode)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                if (darkMode) R.drawable.baseline_light_mode_24
                                else R.drawable.baseline_mode_night_24
                            ),
                            contentDescription = stringResource(
                                if (darkMode) R.string.mode_terang
                                else R.string.mode_gelap
                            ),
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            if (user.email.isEmpty()) {
                                scope.launch(Dispatchers.IO) {
                                    signIn(context, userDataStore)
                                }
                            } else {
                                showProfileDialog = true
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.account_circle_24),
                            contentDescription = stringResource(id = R.string.profil),
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = Maroon,
                contentColor = Color.White,
                onClick = {
                    if (user.email.isEmpty()) {
                        Toast.makeText(
                            context,
                            "Silakan login terlebih dahulu.",
                            Toast.LENGTH_SHORT
                        ).show()

                        scope.launch(Dispatchers.IO) {
                            signIn(context, userDataStore)
                        }
                    } else {
                        filmToEdit = null
                        bitmap = null
                        showFilmDialog = true
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.tambah_film)
                )
            }
        }
    ) { innerPadding ->
        ScreenContent(
            showList = showList,
            modifier = Modifier.padding(innerPadding),
            viewModel = viewModel,
            userEmail = user.email,
            ownedFilmIds = ownedFilmIds,
            onLoginClick = {
                scope.launch(Dispatchers.IO) {
                    signIn(context, userDataStore)
                }
            },
            onEditClick = { film ->
                filmToEdit = film
                bitmap = null
                showFilmDialog = true
            },
            onDeleteClick = { film ->
                filmToDelete = film
            }
        )
    }
    if (showProfileDialog) {
        ProfileDialog(
            user = user,
            onDismissRequest = {
                showProfileDialog = false
            },
            onLogout = {
                showProfileDialog = false
                scope.launch(Dispatchers.IO) {
                    signOut(context, userDataStore)
                }
            }
        )
    }

    if (showFilmDialog) {
        val editingFilm = filmToEdit

        FilmDialog(
            dialogTitle = if (editingFilm == null) "Tambah Film" else "Ubah Film",
            bitmap = bitmap,
            imageUrl = editingFilm?.poster.orEmpty(),
            initialJudul = editingFilm?.judul.orEmpty(),
            initialGenre = editingFilm?.genre.orEmpty(),
            initialUlasan = editingFilm?.ulasan.orEmpty(),
            initialStatus = editingFilm?.status ?: "Belum ditonton",
            onDismissRequest = {
                showFilmDialog = false
                filmToEdit = null
                bitmap = null
            },
            onPickImage = {
                val options = CropImageContractOptions(
                    null,
                    CropImageOptions(
                        imageSourceIncludeGallery = true,
                        imageSourceIncludeCamera = false,
                        fixAspectRatio = true
                    )
                )
                launcher.launch(options)
            },
            onTakePhoto = {
                val options = CropImageContractOptions(
                    null,
                    CropImageOptions(
                        imageSourceIncludeGallery = false,
                        imageSourceIncludeCamera = true,
                        fixAspectRatio = true
                    )
                )
                launcher.launch(options)
            },
            onSave = { selectedBitmap, judul, genre, ulasan, status ->
                if (editingFilm == null) {
                    if (selectedBitmap == null) {
                        Toast.makeText(
                            context,
                            "Pilih gambar terlebih dahulu.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@FilmDialog
                    }

                    val imageFile = bitmapToFile(
                        context = context,
                        bitmap = selectedBitmap
                    )

                    viewModel.saveData(
                        userEmail = user.email,
                        imageFile = imageFile,
                        judul = judul,
                        genre = genre,
                        ulasan = ulasan,
                        statusFilm = status,
                        onSuccess = {
                            Toast.makeText(
                                context,
                                "Film berhasil disimpan.",
                                Toast.LENGTH_SHORT
                            ).show()
                            showFilmDialog = false
                            filmToEdit = null
                            bitmap = null
                        },
                        onError = { message ->
                            Toast.makeText(
                                context,
                                message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                } else {
                    val imageFile = selectedBitmap?.let {
                        bitmapToFile(
                            context = context,
                            bitmap = it
                        )
                    }
                    viewModel.updateData(
                        userEmail = user.email,
                        filmId = editingFilm.id,
                        imageFile = imageFile,
                        currentImageUrl = editingFilm.poster,
                        judul = judul,
                        genre = genre,
                        ulasan = ulasan,
                        statusFilm = status,
                        onSuccess = {
                            Toast.makeText(
                                context,
                                "Film berhasil diubah.",
                                Toast.LENGTH_SHORT
                            ).show()

                            showFilmDialog = false
                            filmToEdit = null
                            bitmap = null
                        },
                        onError = { message ->
                            Toast.makeText(
                                context,
                                message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            }
        )
    }

    val selectedFilm = filmToDelete

    if (selectedFilm != null) {
        DeleteFilmDialog(
            film = selectedFilm,
            onDismissRequest = {
                filmToDelete = null
            },
            onConfirmDelete = {
                viewModel.deleteData(
                    userEmail = user.email,
                    filmId = selectedFilm.id,
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Film berhasil dihapus.",
                            Toast.LENGTH_SHORT
                        ).show()

                        filmToDelete = null
                    },
                    onError = { message ->
                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        )
    }

    if (isProcessing) {
        ProcessingDialog()
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkMaroon,
                        Maroon
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Card(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.16f)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.35f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ML",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = "MovieLog",
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Catat, kelola, dan ulas film favoritmu.",
                color = Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(34.dp)
            )
            Text(
                text = "Memuat aplikasi...",
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ScreenContent(
    showList: Boolean,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    userEmail: String,
    ownedFilmIds: Set<Long>,
    onLoginClick: () -> Unit,
    onEditClick: (Film) -> Unit,
    onDeleteClick: (Film) -> Unit
) {
    if (userEmail.isBlank()) {
        LoginRequiredScreen(
            modifier = modifier,
            onLoginClick = onLoginClick
        )
        return
    }
    LaunchedEffect(userEmail) {
        viewModel.retrieveData(userEmail)
    }
    val data by viewModel.data.collectAsState()
    val status by viewModel.status.collectAsState()

    if (status == ApiStatus.LOADING) {
        LoadingScreen(modifier)
    } else if (status == ApiStatus.FAILED) {
        ErrorScreen(
            modifier = modifier,
            onRetry = {
                viewModel.retrieveData(userEmail)
            }
        )
    } else if (data.isEmpty()) {
        EmptyFilmScreen(modifier)
    } else {
        if (showList) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = 96.dp
                )
            ) {
                items(data) {
                    ListItem(
                        film = it,
                        canDelete = ownedFilmIds.contains(it.id),
                        onClick = {
                            onEditClick(it)
                        },
                        onDeleteClick = {
                            onDeleteClick(it)
                        }
                    )
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                columns = StaggeredGridCells.Fixed(2),
                verticalItemSpacing = 12.dp,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    top = 12.dp,
                    bottom = 96.dp
                )
            ) {
                items(data) {
                    GridItem(
                        film = it,
                        canDelete = ownedFilmIds.contains(it.id),
                        onClick = {
                            onEditClick(it)
                        },
                        onDeleteClick = {
                            onDeleteClick(it)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LoginRequiredScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.account_circle_24),
                    contentDescription = stringResource(id = R.string.profil),
                    tint = Maroon,
                    modifier = Modifier.size(72.dp)
                )
                Text(
                    text = "Login terlebih dahulu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Masuk dengan akun Google untuk melihat, menambah, dan mengelola daftar film kamu.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onLoginClick
                ) {
                    Text(text = "Login Google")
                }
            }
        }
    }
}

@Composable
fun EmptyFilmScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Belum ada film. Tekan tombol + untuk menambahkan film pertama kamu.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Maroon)
    }
}

@Composable
fun ProcessingDialog() {
    Dialog(
        onDismissRequest = {}
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = Maroon)
                Text(
                    text = "Memproses data...",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = R.string.error))
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Text(text = stringResource(id = R.string.try_again))
        }
    }
}

@Composable
fun DeleteIconButton(
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(
                color = Maroon.copy(alpha = 0.90f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = "Hapus",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ListItem(
    film: Film,
    canDelete: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = DividerDefaults.color.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (film.poster.isNotBlank()) {
                    AsyncImage(
                        model = film.poster.toUri(),
                        contentDescription = film.judul,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.loading_img),
                        error = painterResource(id = R.drawable.broken_img),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 22.dp,
                                    topEnd = 22.dp
                                )
                            )
                    )
                }
                if (canDelete) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        DeleteIconButton(onClick = onDeleteClick)
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = film.judul,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = film.ulasan,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = film.genre,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                        color = Maroon,
                        modifier = Modifier
                            .background(
                                color = Maroon.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                    Text(
                        text = film.status,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GridItem(
    film: Film,
    canDelete: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = DividerDefaults.color.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (film.poster.isNotBlank()) {
                    AsyncImage(
                        model = film.poster.toUri(),
                        contentDescription = film.judul,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.loading_img),
                        error = painterResource(id = R.drawable.broken_img),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp
                                )
                            )
                    )
                }
                if (canDelete) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        DeleteIconButton(onClick = onDeleteClick)
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = film.judul,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = film.ulasan,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = film.genre,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = Maroon,
                    modifier = Modifier
                        .background(
                            color = Maroon.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                )
                Text(
                    text = film.status,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DeleteFilmDialog(
    film: Film,
    onDismissRequest: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Hapus Film")
        },
        text = {
            Text(text = "Yakin ingin menghapus film \"${film.judul}\"?")
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete
            ) {
                Text(text = "Ya, hapus")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = "Batal")
            }
        }
    )
}

@Composable
fun ProfileDialog(
    user: User,
    onDismissRequest: () -> Unit,
    onLogout: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (user.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = stringResource(id = R.string.profil),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.account_circle_24),
                        error = painterResource(id = R.drawable.account_circle_24),
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.account_circle_24),
                        contentDescription = stringResource(id = R.string.profil),
                        tint = Maroon,
                        modifier = Modifier.size(96.dp)
                    )
                }
                Text(
                    text = user.name.ifBlank { "-" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = user.email.ifBlank { "-" },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onLogout,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(text = stringResource(id = R.string.logout))
                }
                TextButton(
                    onClick = onDismissRequest
                ) {
                    Text(text = stringResource(id = R.string.tutup))
                }
            }
        }
    }
}

private suspend fun signIn(
    context: Context,
    dataStore: UserDataStore
) {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(
    result: GetCredentialResponse,
    dataStore: UserDataStore
) {
    val credential = result.credential

    if (
        credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)

            val user = User(
                name = googleId.displayName ?: "",
                email = googleId.id,
                photoUrl = googleId.profilePictureUri?.toString().orEmpty()
            )

            dataStore.saveData(user)

            Log.d("SIGN-IN", "User: $user")
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    } else {
        Log.e("SIGN-IN", "Error: unrecognized custom credential type.")
    }
}

private suspend fun signOut(
    context: Context,
    dataStore: UserDataStore
) {
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(ClearCredentialStateRequest())

        dataStore.clearData()

        Log.d("SIGN-IN", "User logout")
    } catch (e: Exception) {
        Log.e("SIGN-IN", "Logout error: ${e.message}")
    }
}

private fun getCroppedImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
): Bitmap? {
    if (!result.isSuccessful) {
        Log.e("IMAGE", "Error: ${result.error}")
        return null
    }

    val uri = result.uriContent ?: return null

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(resolver, uri)
    } else {
        val source = ImageDecoder.createSource(resolver, uri)

        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }
}

private fun bitmapToFile(
    context: Context,
    bitmap: Bitmap
): File {
    val file = File(
        context.cacheDir,
        "movielog_${System.currentTimeMillis()}.jpg"
    )
    FileOutputStream(file).use { outputStream ->
        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            80,
            outputStream
        )
    }
    return file
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun MainScreenPreview() {
    MovieLogTheme {
        MainScreen(rememberNavController())
    }
}