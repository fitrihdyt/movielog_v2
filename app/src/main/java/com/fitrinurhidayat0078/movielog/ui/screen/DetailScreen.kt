package com.fitrinurhidayat0078.movielog.ui.screen

import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.fitrinurhidayat0078.movielog.R
import com.fitrinurhidayat0078.movielog.ui.theme.MovieLogTheme
import com.fitrinurhidayat0078.movielog.util.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavHostController,
    id: Long? = null
) {
    val context = LocalContext.current
    val factory = ViewModelFactory(context)
    val viewModel: DetailViewModel = viewModel(factory = factory)

    val radioOptions = listOf(
        stringResource(R.string.belum_ditonton),
        stringResource(R.string.sudah_ditonton)
    )

    var judul by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var ulasan by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(radioOptions[0]) }
    var poster by remember { mutableStateOf("") }

    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        if (id == null) return@LaunchedEffect

        val data = viewModel.getFilm(id) ?: return@LaunchedEffect

        judul = data.judul
        genre = data.genre
        ulasan = data.ulasan
        status = data.status
        poster = data.poster
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.kembali),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                title = {
                    if (id == null)
                        Text(text = stringResource(id = R.string.tambah_film))
                    else
                        Text(text = stringResource(id = R.string.edit_film))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(
                        onClick = {
                            if (
                                judul.isBlank() ||
                                genre.isBlank() ||
                                ulasan.isBlank() ||
                                status.isBlank()
                            ) {
                                Toast.makeText(
                                    context,
                                    R.string.invalid,
                                    Toast.LENGTH_LONG
                                ).show()
                                return@IconButton
                            }
                            if (id == null) {
                                viewModel.insert(
                                    judul = judul,
                                    genre = genre,
                                    ulasan = ulasan,
                                    status = status,
                                    poster = poster
                                )
                            } else {
                                viewModel.update(
                                    id = id,
                                    judul = judul,
                                    genre = genre,
                                    ulasan = ulasan,
                                    status = status,
                                    poster = poster
                                )
                            }
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = stringResource(R.string.simpan),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (id != null) {
                        DeleteAction {
                            showDialog.value = true
                        }
                    }
                }
            )
        }
    ) { padding ->
        FormFilm(
            title = judul,
            onTitleChange = { judul = it },
            genre = genre,
            onGenreChange = { genre = it },
            desc = ulasan,
            onDescChange = { ulasan = it },
            status = status,
            onStatusChange = { status = it },
            radioOptions = radioOptions,
            poster = poster,
            onPosterChange = { poster = it },
            modifier = Modifier.padding(padding)
        )
        if (id != null && showDialog.value) {
            DisplayAlertDialog(
                onDismissRequest = {
                    showDialog.value = false
                }
            ) {
                viewModel.delete(id)
                navController.popBackStack()
            }
        }
    }
}

@Composable
fun DeleteAction(delete: () -> Unit) {
    val expanded = remember { mutableStateOf(false) }

    IconButton(onClick = { expanded.value = true }) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.lainnya),
            tint = MaterialTheme.colorScheme.primary
        )
    }
    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = {
            expanded.value = false
        }
    ) {
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.hapus_film))
            },
            onClick = {
                expanded.value = false
                delete()
            }
        )
    }
}

@Composable
fun FormFilm(
    title: String,
    onTitleChange: (String) -> Unit,
    genre: String,
    onGenreChange: (String) -> Unit,
    desc: String,
    onDescChange: (String) -> Unit,
    status: String,
    onStatusChange: (String) -> Unit,
    radioOptions: List<String>,
    poster: String,
    onPosterChange: (String) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PosterPicker(
            poster = poster,
            onPosterChange = onPosterChange
        )
        OutlinedTextField(
            value = title,
            onValueChange = { onTitleChange(it) },
            label = { Text(text = stringResource(R.string.judul)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = genre,
            onValueChange = { onGenreChange(it) },
            label = { Text(text = stringResource(R.string.genre)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = desc,
            onValueChange = { onDescChange(it) },
            label = { Text(text = stringResource(R.string.ulasan)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray)
        ) {
            radioOptions.forEach { text ->
                StatusOption(
                    label = text,
                    isSelected = status == text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = status == text,
                            onClick = {
                                onStatusChange(text)
                            }
                        )
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun StatusOption(
    label: String,
    isSelected: Boolean,
    modifier: Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun PosterPicker(
    poster: String,
    onPosterChange: (String) -> Unit
) {
    val context = LocalContext.current

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            onPosterChange(uri.toString())
        }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (poster.isNotBlank()) {
            AsyncImage(
                model = poster.toUri(),
                contentDescription = stringResource(R.string.poster_film),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
            TextButton(
                onClick = {
                    onPosterChange("")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.hapus_poster))
            }
        }
        OutlinedButton(
            onClick = {
                pickMedia.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.pilih_poster))
        }
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun DetailScreenPreview() {
    MovieLogTheme {
        DetailScreen(rememberNavController())
    }
}