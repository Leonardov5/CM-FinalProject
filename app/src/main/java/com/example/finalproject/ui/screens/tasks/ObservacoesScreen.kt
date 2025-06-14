package com.example.finalproject.ui.screens.tasks

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.finalproject.R
import com.example.finalproject.data.model.Observacao
import com.example.finalproject.data.model.User
import com.example.finalproject.ui.viewmodels.tasks.ObservacoesViewModel
import com.example.finalproject.utils.formatDate
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservacoesScreen(
    tarefaId: String,
    onBackPressed: () -> Unit,
    viewModel: ObservacoesViewModel = viewModel()
) {
    val context = LocalContext.current

    // Carregar observações e usuário quando a tela for iniciada
    LaunchedEffect(tarefaId) {
        viewModel.carregarObservacoes(tarefaId)
        viewModel.loadUser() // Carrega o usuário atual e verifica se é admin
    }

    var imagemAmpliada by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.observations)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                windowInsets = WindowInsets(0)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleAddObservacaoDialog() }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add_observation),
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
                viewModel.error != null -> {
                    Text(
                        text = viewModel.error ?: "Erro desconhecido",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                viewModel.observacoes.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.no_observations),
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
                        items(viewModel.observacoes) { observacao ->
                            ObservacaoItem(
                                observacao = observacao,
                                usuario = viewModel.obterUsuario(observacao.createdBy),
                                isAdmin = viewModel.isAdmin(),
                                onDelete = {
                                    viewModel.excluirObservacao(
                                        observacaoId = observacao.id ?: "",
                                        onSuccess = { /* Já recarrega as observações */ },
                                        onError = { /* Mostrar mensagem de erro */ }
                                    )
                                },
                                onImageClick = { imagemUrl ->
                                    imagemAmpliada = imagemUrl
                                },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog para adicionar nova observação
    if (viewModel.showAddObservacaoDialog) {
        AddObservacaoDialog(
            onDismiss = { viewModel.toggleAddObservacaoDialog() },
            onSave = { viewModel.salvarObservacao(
                onSuccess = { viewModel.toggleAddObservacaoDialog() },
                onError = { /* Mostrar mensagem de erro */ }
            )},
            viewModel = viewModel
        )
    }

    // Dialog para editar observação
    if (viewModel.showEditObservacaoDialog) {
        EditObservacaoDialog(
            onDismiss = { viewModel.cancelarEdicao() },
            onSave = { viewModel.salvarEdicaoObservacao(
                onSuccess = { /* Sucesso - dialog já será fechado */ },
                onError = { /* Mostrar mensagem de erro */ }
            )},
            viewModel = viewModel
        )
    }

    // Dialog para ampliar imagem
    imagemAmpliada?.let { url ->
        Dialog(onDismissRequest = { imagemAmpliada = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    IconButton(
                        onClick = { imagemAmpliada = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar"
                        )
                    }

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Imagem ampliada",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
fun ObservacaoItem(
    observacao: Observacao,
    usuario: User?,
    isAdmin: Boolean,
    onDelete: () -> Unit,
    onImageClick: (String) -> Unit,
    viewModel: ObservacoesViewModel
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
            // Cabeçalho com informações do usuário e data
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Avatar do usuário
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = usuario?.nome?.firstOrNull()?.toString() ?: "?",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Nome do usuário
                    Column {
                        Text(
                            text = usuario?.nome ?: "Usuário desconhecido",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )

                        // Data da observação
                        observacao.createdAt?.let { data ->
                            Text(
                                text = formatDate(data),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Ações de editar e excluir
                Row {
                    // Botão de editar (apenas para o criador ou admin)
                    if (viewModel.podeEditarObservacao(observacao)) {
                        IconButton(onClick = {
                            viewModel.iniciarEdicaoObservacao(observacao)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar observação",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Botão de excluir (apenas para admins ou criador da observação)
                    if (isAdmin || observacao.createdBy == viewModel.user?.id) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir observação",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Descrição da observação
            Text(
                text = observacao.observacao,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Imagens da observação
            if (observacao.anexos.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(observacao.anexos) { imagemUrl ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imagemUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Imagem da observação",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onImageClick(imagemUrl) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmação para excluir observação
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirmar exclusão") },
            text = { Text("Tem certeza que deseja excluir esta observação? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    },
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AddObservacaoDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    viewModel: ObservacoesViewModel
) {
    val context = LocalContext.current

    // Launcher para selecionar imagens
    val imagemLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Criar arquivo temporário a partir da URI
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("img", ".jpg", context.cacheDir)
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            viewModel.adicionarImagem(tempFile)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.add_observation),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo para descrição
                OutlinedTextField(
                    value = viewModel.novaObservacaoTexto,
                    onValueChange = { viewModel.novaObservacaoTexto = it },
                    label = { Text(stringResource(R.string.description)) },
                    placeholder = { Text(stringResource(R.string.add_observation_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botão para adicionar imagens
                Button(
                    onClick = { imagemLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Add Image",
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_image))
                }

                // Preview das imagens selecionadas
                if (viewModel.imagensTemporarias.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.selected_images),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.imagensTemporarias.size) { index ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(viewModel.imagensTemporarias[index])
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Selected image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Botão para remover imagem
                                IconButton(
                                    onClick = { viewModel.removerImagem(index) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove image",
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botões de ação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onSave,
                        enabled = viewModel.novaObservacaoTexto.isNotBlank()
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

@Composable
fun EditObservacaoDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    viewModel: ObservacoesViewModel
) {
    val context = LocalContext.current
    val observacao = viewModel.editandoObservacao ?: return

    // Lista de imagens que devem ser mantidas (inicialmente todas as imagens atuais)
    var imagensAManter by remember { mutableStateOf(observacao.anexos) }

    // Launcher para selecionar imagens
    val imagemLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Criar arquivo temporário a partir da URI
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("img", ".jpg", context.cacheDir)
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            viewModel.adicionarImagem(tempFile)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.edit_observation),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo para descrição
                OutlinedTextField(
                    value = viewModel.textoObservacaoEditada,
                    onValueChange = { viewModel.textoObservacaoEditada = it },
                    label = { Text(stringResource(R.string.description)) },
                    placeholder = { Text(stringResource(R.string.add_observation_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // Exibir imagens existentes
                if (observacao.anexos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.existing_images) + ":",
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(observacao.anexos) { imagemUrl ->
                            // Verificar se a imagem está na lista de imagens a manter
                            val mantida = imagemUrl in imagensAManter

                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (mantida) 2.dp else 1.dp,
                                        color = if (mantida) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(imagemUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Image of observation",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(if (mantida) 1f else 0.5f),  // Reduz a opacidade das imagens que serão removidas
                                    contentScale = ContentScale.Crop
                                )

                                // Botão para alternar entre manter/remover imagem
                                IconButton(
                                    onClick = {
                                        imagensAManter = if (mantida) {
                                            imagensAManter - imagemUrl
                                        } else {
                                            imagensAManter + imagemUrl
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (mantida) MaterialTheme.colorScheme.errorContainer
                                            else MaterialTheme.colorScheme.primaryContainer
                                        )
                                ) {
                                    Icon(
                                        imageVector = if (mantida) Icons.Default.Close else Icons.Default.Add,
                                        contentDescription = if (mantida) "Remove image" else "Restore image",
                                        tint = if (mantida) MaterialTheme.colorScheme.onErrorContainer
                                              else MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botão para adicionar imagens
                Button(
                    onClick = { imagemLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Add Image",
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_image))
                }

                // Preview das novas imagens selecionadas
                if (viewModel.imagensTemporarias.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.new_images) + ":",
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.imagensTemporarias.size) { index ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(viewModel.imagensTemporarias[index])
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = stringResource(id = R.string.selected_image),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                IconButton(
                                    onClick = { viewModel.removerImagem(index) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remover imagem",
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botões de ação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            viewModel.salvarEdicaoObservacao(
                                onSuccess = onSave, // Corrigido: passando o callback onSave ao invés de onSuccess
                                onError = { /* Mostrar erro */ },
                                imagensAtuais = imagensAManter
                            )
                        },
                        enabled = viewModel.textoObservacaoEditada.isNotBlank()
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}
