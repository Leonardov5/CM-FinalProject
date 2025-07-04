package com.example.finalproject.ui.screens.tasks

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.finalproject.R
import com.example.finalproject.data.model.Trabalho
import com.example.finalproject.data.model.Utilizador
import com.example.finalproject.ui.components.tasks.LogWorkDialog
import com.example.finalproject.ui.theme.primaryLight
import com.example.finalproject.ui.viewmodels.tasks.TrabalhosViewModel
import com.example.finalproject.utils.formatDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrabalhosScreen(
    tarefaId: String,
    onBackPressed: () -> Unit,
    viewModel: TrabalhosViewModel = viewModel()
) {
    val context = LocalContext.current
    var showLogWorkDialog by remember { mutableStateOf(false) }
    var errorMessageId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(tarefaId) {
        viewModel.carregarTrabalhos(tarefaId)
        viewModel.loadUser()
    }

    LaunchedEffect(viewModel.errorRes) {
        if (viewModel.errorRes != null) {
            errorMessageId = viewModel.errorRes
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.works)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                windowInsets = WindowInsets(0)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showLogWorkDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_work),
                )
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                viewModel.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessageId != null -> {
                    Text(
                        text = stringResource(id = errorMessageId!!),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                    errorMessageId = null
                }
                viewModel.trabalhos.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.no_work_logs),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Informações da tarefa
                        item {
                            viewModel.tarefa?.let { tarefa ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = tarefa.nome,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        LinearProgressIndicator(
                                            progress = (tarefa.taxaConclusao / 100).toFloat().coerceIn(0f, 1f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )

                                        Text(
                                            text = "${(tarefa.taxaConclusao).toInt()}% " + stringResource(R.string.completed),
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Trabalhos
                        items(viewModel.trabalhos.sortedByDescending { it.data }) { trabalho ->
                            TrabalhoItem(
                                trabalho = trabalho,
                                utilizador = viewModel.obterUtilizador(trabalho.createdBy),
                                onDelete = {
                                    if (viewModel.isAdminUser || viewModel.isManager) {
                                        viewModel.eliminarTrabalho(
                                            trabalhoId = trabalho.id ?: "",
                                            onSuccess = { /* Já recarrega os trabalhos */ },
                                            onError = { /* Mostrar mensagem de erro */ }
                                        )
                                    }
                                },
                                isAdmin = viewModel.isAdminUser,
                                isManager = viewModel.isManager
                            )
                        }
                    }
                }
            }
        }
    }

    // Adicionar trabalho
    if (showLogWorkDialog) {
        LogWorkDialog(
            show = true,
            tarefaId = tarefaId,
            onDismiss = { showLogWorkDialog = false },
            onSuccess = {
                viewModel.carregarTrabalhos(tarefaId)
                showLogWorkDialog = false

                Toast.makeText(context, R.string.work_logged_successfully, Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun TrabalhoItem(
    trabalho: Trabalho,
    utilizador: Utilizador?,
    onDelete: () -> Unit,
    isAdmin: Boolean,
    isManager: Boolean
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Imagem do utilizador
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (utilizador != null && !utilizador.fotografia.isNullOrBlank()) {
                            val context = LocalContext.current
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(utilizador.fotografia)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            if (utilizador != null) {
                                Image(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(primaryLight),
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = utilizador?.nome ?: stringResource(id = R.string.unknown_user),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Nome do utilizador
                    Column {
                        Text(
                            text = utilizador?.nome ?: stringResource(id = R.string.unknown_user),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )

                        // Data do trabalho
                        Text(
                            text = formatDate(trabalho.data),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Eliminar trabalho
                if (isAdmin || isManager) {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.delete_work),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tempo dispensado
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(id = R.string.time) + ": ${trabalho.tempoDispensado} " + stringResource(id = R.string.minutes),
                    fontSize = 14.sp
                )
            }

            // Contribuição
            Text(
                text = stringResource(id = R.string.contribution) + ": ${(trabalho.contribuicao).toInt()}%",
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Local
            trabalho.local?.let { local ->
                if (local.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = local,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Confirmar eliminar trabalho
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(id = R.string.confirm_delete_work_title)) },
            text = { Text(stringResource(id = R.string.confirm_delete_work_text)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    },
                ) {
                    Text(text = stringResource(id = R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}
