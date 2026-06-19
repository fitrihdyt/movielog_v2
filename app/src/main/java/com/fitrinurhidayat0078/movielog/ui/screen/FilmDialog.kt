package com.fitrinurhidayat0078.movielog.ui.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.createBitmap
import coil.compose.AsyncImage
import com.fitrinurhidayat0078.movielog.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmDialog(
    dialogTitle: String,
    bitmap: Bitmap?,
    imageUrl: String,
    initialJudul: String,
    initialGenre: String,
    initialUlasan: String,
    initialStatus: String,
    onDismissRequest: () -> Unit,
    onPickImage: () -> Unit,
    onTakePhoto: () -> Unit,
    onSave: (
        bitmap: Bitmap?,
        judul: String,
        genre: String,
        ulasan: String,
        status: String
    ) -> Unit
) {
    var judul by remember(initialJudul) { mutableStateOf(initialJudul) }
    var genre by remember(initialGenre) { mutableStateOf(initialGenre) }
    var ulasan by remember(initialUlasan) { mutableStateOf(initialUlasan) }
    var status by remember(initialStatus) {
        mutableStateOf(initialStatus.ifBlank { "Belum ditonton" })
    }
    var expanded by remember { mutableStateOf(false) }

    val statusOptions = listOf(
        "Belum ditonton",
        "Sedang ditonton",
        "Sudah ditonton"
    )

    val hasImage = bitmap != null || imageUrl.isNotBlank()

    val isFormValid = hasImage &&
            judul.isNotBlank() &&
            genre.isNotBlank() &&
            ulasan.isNotBlank() &&
            status.isNotBlank()

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dialogTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Poster film",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                } else if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Poster film",
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.loading_img),
                        error = painterResource(id = R.drawable.broken_img),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada gambar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPickImage,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddPhotoAlternate,
                            contentDescription = "Upload gambar"
                        )
                        Text(
                            text = "Upload",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    OutlinedButton(
                        onClick = onTakePhoto,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = "Ambil gambar"
                        )
                        Text(
                            text = "Kamera",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                OutlinedTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    label = { Text(text = "Judul Film") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text(text = "Genre") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ulasan,
                    onValueChange = { ulasan = it },
                    label = { Text(text = "Ulasan") },
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = !expanded
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(text = "Status") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        },
                        modifier = Modifier
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(text = option)
                                },
                                onClick = {
                                    status = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text(text = "Batal")
                    }
                    Button(
                        onClick = {
                            onSave(
                                bitmap,
                                judul.trim(),
                                genre.trim(),
                                ulasan.trim(),
                                status.trim()
                            )
                        },
                        enabled = isFormValid
                    ) {
                        Text(text = "Simpan")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FilmDialogPreview() {
    val bitmap = createBitmap(
        width = 600,
        height = 600
    )
    FilmDialog(
        dialogTitle = "Tambah Film",
        bitmap = bitmap,
        imageUrl = "",
        initialJudul = "",
        initialGenre = "",
        initialUlasan = "",
        initialStatus = "Belum ditonton",
        onDismissRequest = {},
        onPickImage = {},
        onTakePhoto = {},
        onSave = { _, _, _, _, _ -> }
    )
}